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
import com.glmapper.bridge.boot.events.SlaveChangeEvent;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * slave 建立连接事件监听器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:18 PM
 * @since:
 **/
public class SlaveConnectEventProcessor implements ConnectionEventProcessor {

    private static final Logger  LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    private final ClusterService clusterService;

    public SlaveConnectEventProcessor(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Override
    public void onEvent(String remoteAddr, Connection conn) {
        String ip = conn.getRemoteIP();
        List<ServerEntity> slaves = clusterService.getSlaves();
        for (ServerEntity server : slaves) {
            if (server.getIp().equals(ip)) {
                LOGGER.info("[SlaveConnectEventProcessor] slave[{}:{}] connected", ip,
                    conn.getRemotePort());
                SlaveConnectionFactory.register(conn);
                //无论是新上线的slave还是连接断开后重新连接上的, 都发送 slave 变更事件
                EventCenter.post(new SlaveChangeEvent(ip, null));
                return;
            }
        }
    }
}
