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

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.rpc.RpcServer;
import com.glmapper.bridge.boot.processor.AbstractProcessor;
import com.glmapper.bridge.boot.processor.SlaveConnectEventProcessor;
import com.glmapper.bridge.boot.processor.SlaveDisConnectEventProcessor;
import com.glmapper.bridge.boot.service.ClusterService;
import com.glmapper.bridge.boot.support.LocalServer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:10 PM
 * @since:
 **/
public class MasterServer {

    private static final Logger                                          LOGGER        = LoggerFactory
                                                                                           .getLogger(MasterServer.class);

    private static final Map<ServerTypeEnum, List<AbstractProcessor<?>>> PROCESSOR_MAP = Maps
                                                                                           .newConcurrentMap();
    /**
     * 供 slave 连接的 Server，仅 master 上会启动
     */
    private RpcServer                                                    slaveServer;

    @Autowired
    private ClusterService                                               clusterService;

    private final int                                                    port;

    public MasterServer(int port) {
        this.port = port;
    }

    public void init() {
        synchronized (this) {
            initSlaveServer();
        }
    }

    /**
     * 启动 slave 连接的 Server
     */
    public void initSlaveServer() {
        if (slaveServer == null) {
            try {
                slaveServer = new RpcServer(LocalServer.IP, port, true);
                slaveServer.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                    new SlaveDisConnectEventProcessor(clusterService));
                slaveServer.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                    new SlaveConnectEventProcessor(clusterService));
                Collection<AbstractProcessor<?>> processors = PROCESSOR_MAP
                    .get(ServerTypeEnum.SLAVE_SERVER);
                if (!CollectionUtils.isEmpty(processors)) {
                    for (AbstractProcessor<?> processor : processors) {
                        slaveServer.registerUserProcessor(processor);
                    }
                }
                boolean isSuccess = slaveServer.start();
                if (isSuccess) {
                    LOGGER.info("[MasterServer] slaveServer started");
                } else {
                    throw new RuntimeException("start slaveServer failed");
                }
            } catch (Exception e) {
                slaveServer = null;
                throw new RuntimeException("[MasterServer] start slaveServer error", e);
            }
        }
    }

    /**
     * 注册处理器
     *
     * @param processor
     */
    public static void registerProcessor(AbstractProcessor<?> processor) {
        ServerTypeEnum serverType = processor.getType();
        synchronized (serverType) {
            if (!PROCESSOR_MAP.containsKey(serverType)) {
                PROCESSOR_MAP.put(serverType, Lists.newArrayList());
            }
            PROCESSOR_MAP.get(processor.getType()).add(processor);
        }
    }

    /**
     * 服务器角色枚举
     */
    public enum ServerTypeEnum {
        /**
         * 供 slave 连接的 server，集群内部 slaveClient 通过 9202 端口与 slaveServer 建立长连接
         */
        SLAVE_SERVER
    }
}
