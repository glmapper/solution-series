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
package com.glmapper.bridge.boot.cluster;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.sofa.common.profile.StringUtil;
import com.glmapper.bridge.boot.processor.MasterConnectEventProcessor;
import com.glmapper.bridge.boot.processor.MasterDisConnectEventProcessor;
import com.glmapper.bridge.boot.utils.NetworkAddressUtil;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:10 PM
 * @since:
 **/
public class SlaveClient {

    private static final Logger                            LOGGER           = LoggerFactory
                                                                                .getLogger(SlaveClient.class);
    private static final Map<String, SyncUserProcessor<?>> PROCESSOR_MAP    = Maps.newHashMap();

    private RpcClient                                      rpcClient        = null;

    /**
     * master 的 连接
     */
    private AtomicReference<Connection>                    connectionRef    = new AtomicReference<>();

    private static final int                               CALLBACK_TIMEOUT = 3000;

    private static final int                               CONNECT_TIMEOUT  = 5000;

    private final int                                      port;

    public SlaveClient(int port) {
        this.port = port;
    }

    public void init() {
        synchronized (this) {
            if (rpcClient == null) {
                LOGGER.info("[SlaveClient] begin init");
                rpcClient = new RpcClient();
                rpcClient.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                    new MasterConnectEventProcessor());
                rpcClient.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                    new MasterDisConnectEventProcessor());
                for (SyncUserProcessor<?> processor : PROCESSOR_MAP.values()) {
                    rpcClient.registerUserProcessor(processor);
                }
                rpcClient.init();
                LOGGER.info("[SlaveClient] init end");
            }
        }
    }

    /**
     * 连接指定的 Server
     *
     * @param ip    server的IP
     * @throws RuntimeException
     */
    public void connectMaster(String ip) throws RuntimeException {
        synchronized (this) {
            try {
                String serverHost = NetworkAddressUtil.genConnectHost(ip, port);
                Connection connection = connectionRef.get();
                if (connection != null && connection.isFine()) {
                    //如果当前连接不是 master, 断开重新连接
                    if (!StringUtil.equals(
                        serverHost,
                        NetworkAddressUtil.genConnectHost(connection.getRemoteIP(),
                            connection.getRemotePort()))) {
                        connection.close();
                    } else {
                        return;
                    }
                }
                LOGGER.info("[SlaveClient] begin connect, host: {}", serverHost);
                connection = rpcClient.getConnection(serverHost, CONNECT_TIMEOUT);
                connectionRef.set(connection);
            } catch (Exception e) {
                LOGGER.error("[SlaveClient] connect master failed", e);
                throw new RuntimeException("[SlaveClient] connect master failed", e);
            }
        }
    }

    /**
     * 是否保持连接
     * @return
     */
    public boolean isConnected() {
        Connection connection = connectionRef.get();
        return connection != null && connection.isFine();
    }

    /**
     * 获取当前连接, 如果没有则返回null
     * @return
     */
    public Connection getConnection() {
        Connection connection = connectionRef.get();
        if (isConnected()) {
            return connection;
        }
        return null;
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        Connection connection = connectionRef.get();
        if (connection != null) {
            connection.close();
            connectionRef.set(null);
        }
    }

    /**
     * 远程调用
     *
     * @param appRequest
     * @param callback
     */
    public void invoke(Object appRequest, InvokeCallback callback) throws RemotingException {
        rpcClient.invokeWithCallback(connectionRef.get(), appRequest, callback, CALLBACK_TIMEOUT);
    }
}
