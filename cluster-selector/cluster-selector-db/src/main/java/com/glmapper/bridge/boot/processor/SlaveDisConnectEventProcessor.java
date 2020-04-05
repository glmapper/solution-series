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
package com.glmapper.bridge.boot.processor;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.glmapper.bridge.boot.cluster.SlaveConnectionFactory;
import com.glmapper.bridge.boot.events.EventCenter;
import com.glmapper.bridge.boot.events.SlaveConnectionCloseEvent;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * slave 断连处理器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:18 PM
 * @since:
 **/
public class SlaveDisConnectEventProcessor implements ConnectionEventProcessor {

    private final ClusterService clusterService;

    public SlaveDisConnectEventProcessor(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Override
    public void onEvent(String remoteAddr, Connection conn) {
        String ip = conn.getRemoteIP();
        List<ServerEntity> slaves = clusterService.getSlaves();
        for (ServerEntity server : slaves) {
            if (server.getIp().equals(ip)) {
                LOGGER.info("[SlaveDisConnectEventProcessor] slave[{}:{}] connect closed.", ip,
                    conn.getRemotePort());
                SlaveConnectionFactory.remove(ip);
                EventCenter.post(new SlaveConnectionCloseEvent(ip));
                return;
            }
        }
    }
}
