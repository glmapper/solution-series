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
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * slave 连接工厂
 * 
 * @author qian.lqlq
 * @version $Id: SlaveConnectionFactory.java, v 0.1 2017年8月9日 下午4:43:11 qian.lqlq Exp $
 */
public class SlaveConnectionFactory {

    /**
     * slave连接信息
     */
    private static final Map<String, Connection> MAP = Maps.newConcurrentMap();

    /**
     * 注册连接
     * 
     * @param connection
     */
    public static void register(Connection connection) {
        MAP.put(connection.getRemoteIP(), connection);
    }

    /**
     * 判断该IP的连接是否存在
     * 
     * @param ip
     * @return
     */
    public static boolean containsIP(String ip) {
        return MAP.containsKey(ip);
    }

    /**
     * 移除连接
     * 
     * @param ip
     */
    public static void remove(String ip) {
        if (MAP.containsKey(ip)) {
            MAP.remove(ip);
        }
    }

    public static Collection<Connection> getConnections() {
        return MAP.values();
    }
}
