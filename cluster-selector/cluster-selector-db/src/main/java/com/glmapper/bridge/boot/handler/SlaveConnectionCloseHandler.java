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
package com.glmapper.bridge.boot.handler;

import com.glmapper.bridge.boot.enums.ServerStatusEnum;
import com.glmapper.bridge.boot.events.EventCenter;
import com.glmapper.bridge.boot.events.SlaveChangeEvent;
import com.glmapper.bridge.boot.events.SlaveConnectionCloseEvent;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import com.glmapper.bridge.boot.support.LocalServer;
import com.glmapper.bridge.boot.utils.TimeUtil;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 *
 * slave 断开连接事件处理器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 2:32 PM
 * @since:
 **/
public class SlaveConnectionCloseHandler extends AbstractEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Autowired
    private ClusterService      clusterService;

    @Subscribe
    @AllowConcurrentEvents
    public void onEvent(SlaveConnectionCloseEvent event) {
        // 是否是 master
        if (LocalServer.isMaster()) {
            try {
                String ip = event.getIp();
                ServerEntity server = clusterService.getByIP(ip);
                if (server != null) {
                    Date heartbeat = server.getHeartbeat();
                    //如果 server 已处于 RUNNING 状态但已经超时, 修改其状态为并发送 DB_TIMEOUT 的 slaveChangeEvent
                    if (ServerStatusEnum.valueOf(server.getStatus()) == ServerStatusEnum.RUNNING) {
                        if (!TimeUtil.isTimeout(heartbeat, TimeUtil.DB_TIMEOUT)) {
                            return;
                        }
                        if (clusterService.updateStatus(ServerStatusEnum.DB_TIMEOUT,
                            server.getHostName(), server.getHeartbeat(), server.getGmtModify())) {
                            LOGGER
                                .info(
                                    "[SlaveConnectionCloseHandler] slave [{}] status change from [{}] to [{}]",
                                    ip, ServerStatusEnum.RUNNING.name(),
                                    ServerStatusEnum.DB_TIMEOUT.name());
                            EventCenter.post(new SlaveChangeEvent(ip, ServerStatusEnum.DB_TIMEOUT));
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[SlaveConnectionCloseHandler] handle failed.", e);
            }
        } else {
            LOGGER.info("[SlaveConnectionCloseHandler] local server is not master, do nothing.");
        }
    }
}
