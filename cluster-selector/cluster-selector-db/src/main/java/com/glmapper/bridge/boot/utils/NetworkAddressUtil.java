/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.glmapper.bridge.boot.utils;

import com.glmapper.bridge.boot.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: leishu (glmapper_2018@163.com) 2020/3/29 6:19 PM
 * @since:
 **/
public class NetworkAddressUtil {
    private static final Logger  LOGGER                = LoggerFactory
                                                           .getLogger(NetworkAddressUtil.class);
    private static final char    COLON                 = ':';
    private static final String  ENABLED_IP_RANGE      = Constants.PREFIX + ".enabledIpRange";
    private static final String  IP_RANGE_CONFIG       = System.getProperty(ENABLED_IP_RANGE);
    private static final String  BIND_NET_INTER        = Constants.PREFIX + ".bindNetworkInterface";
    private static final String  BIND_NET_INTER_CONFIG = System.getProperty(BIND_NET_INTER);
    private static List<IpRange> IP_RANGES;
    private static String        NETWORK_ADDRESS;
    private static String        HOSTNAME;

    static {
        IP_RANGES = new CopyOnWriteArrayList<>();
        if (StringUtils.isEmpty(IP_RANGE_CONFIG) && StringUtils.isEmpty(BIND_NET_INTER_CONFIG)) {
            // 默认支持所有的ip端口
            IP_RANGES.add(new IpRange("0", "255"));
        } else {
            String[] ipRanges = IP_RANGE_CONFIG.split(",");
            for (String ipRange : ipRanges) {
                if (StringUtils.isEmpty(ipRange)) {
                    continue;
                }

                if (ipRange.indexOf(COLON) > -1) {
                    String[] ranges = ipRange.split(":");
                    IP_RANGES.add(new IpRange(ranges[0], ranges[1]));
                } else {
                    IP_RANGES.add(new IpRange(ipRange));
                }
            }
        }

        NETWORK_ADDRESS = getNetworkAddress();
        HOSTNAME = getHostName();
    }

    /**
     * @return 获得本机唯一 IP 地址
     */
    public static String getLocalIP() {
        return NETWORK_ADDRESS;
    }

    /**
     * @return
     */
    public static String getLocalHostName() {
        return HOSTNAME;
    }

    /**
     * 获得本地的网络地址
     *
     * 在有超过一块网卡时有问题，这里每次只取了第一块网卡绑定的IP地址
     * 当存在这种情况的时候，就需要配置 rpc_enabled_ip_range 参数，用以限制IP范围
     *
     * @return 本地的 IP 地址
     */
    private static String getNetworkAddress() {
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (netInterfaces.hasMoreElements()) {
                boolean useNi = false;
                NetworkInterface ni = netInterfaces.nextElement();
                if (!StringUtils.isEmpty(BIND_NET_INTER_CONFIG)) {
                    if (BIND_NET_INTER_CONFIG.equals(ni.getDisplayName())
                        || BIND_NET_INTER_CONFIG.equals(ni.getName())) {
                        useNi = true;
                    } else {
                        continue;
                    }
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(COLON) == -1
                        && (useNi || ipEnabled(ip.getHostAddress()))) {
                        return ip.getHostAddress();
                    }
                }
            }

            LOGGER.error(
                "Cannot get the valid IP address, enabledIpRange: {}, bindNetworkInterface: {}.",
                IP_RANGE_CONFIG, BIND_NET_INTER_CONFIG);
            return "";
        } catch (Exception e) {
            LOGGER.warn("get network address error, ", e);
            return "";
        }
    }

    /**
     * 判断 IP 是否符合要求
     *
     * @param ip ip地址，例如 10.128.12.111
     * @return 是否符合要求
     */
    private static boolean ipEnabled(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }

        // 如果IP_RANGES为空，返回true
        if (IP_RANGES.isEmpty()) {
            return true;
        }

        // 遍历IP_RANGES
        for (IpRange ipRange : IP_RANGES) {
            if (ipRange.isEnabled(ip)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取本机主机名
     *
     * @return
     */
    private static String getHostName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            if (inetAddress != null) {
                return inetAddress.getHostName();
            }
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to get local host error.", e);
        }
        return Constants.EMPTY;
    }

    /**
     * 组装建立连接的host
     *
     * @param ip
     * @param port
     * @return
     */
    public static String genConnectHost(String ip, int port) {
        return ip + ":" + port;
    }

    /**
     * IP 范围
     */
    private static class IpRange {
        private long start;
        private long end;

        public IpRange(String ip) {
            start = parseStart(ip);
            end = parseEnd(ip);
        }

        public IpRange(String startIp, String endIp) {
            start = parseStart(startIp);
            end = parseEnd(endIp);
        }

        private long parseStart(String ip) {
            int[] starts = { 0, 0, 0, 0 };
            return parse(starts, ip);
        }

        private long parseEnd(String ip) {
            int[] ends = { 255, 255, 255, 255 };
            return parse(ends, ip);
        }

        private long parse(int[] segments, String ip) {
            String[] ipSegments = ip.split("\\.");
            for (int i = 0; i < ipSegments.length; i++) {
                segments[i] = Integer.parseInt(ipSegments[i]);
            }
            long ret = 0;
            for (int i : segments) {
                ret += ret * 255L + i;
            }
            return ret;
        }

        public boolean isEnabled(String ip) {
            String[] ipSegments = ip.split("\\.");
            long ipInt = 0;
            for (String ipSegment : ipSegments) {
                ipInt += ipInt * 255L + Integer.parseInt(ipSegment);
            }
            return ipInt >= start && ipInt <= end;
        }
    }
}