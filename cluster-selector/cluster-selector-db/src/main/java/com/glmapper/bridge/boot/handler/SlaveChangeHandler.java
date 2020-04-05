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
import com.glmapper.bridge.boot.events.SlaveChangeEvent;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import com.glmapper.bridge.boot.support.LocalServer;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import static com.glmapper.bridge.boot.enums.ServerStatusEnum.DB_TIMEOUT;

/**
 * Slave 状态变更处理器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 2:32 PM
 * @since:
 **/
public class SlaveChangeHandler extends AbstractEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Autowired
    private ClusterService      clusterService;

    @Subscribe
    @AllowConcurrentEvents
    public void onSlaveChange(SlaveChangeEvent event) {
        //监听 slave 变化
        if (LocalServer.isMaster()) {
            String ip = null;
            try {
                ip = event.getIp();
                if (StringUtils.isEmpty(ip)) {
                    return;
                }
                ServerStatusEnum status = event.getStatus();
                if (status == null) {
                    ServerEntity target = clusterService.getByIP(ip);
                    if (target == null) {
                        LOGGER.info("[SlaveChangeHandler] not found ip: {} ", ip);
                        return;
                    }
                    status = ServerStatusEnum.valueOf(target.getStatus());
                }
                LOGGER.info("[SlaveChangeHandler] handle slave, ip: {}, status: {}", ip, status);
                // 如果状态是超时，则更新数据库状态为剔除
                if (status == DB_TIMEOUT) {
                    slaveOffline(ip);
                }
            } catch (Throwable t) {
                LOGGER.error("[SlaveChangeHandler] handle ip: {} failed", ip, t);
            }
        }
    }

    /**
     * 服务器下线
     * @param ip
     */
    private void slaveOffline(String ip) {
        clusterService.reject(ip);
        LOGGER.info("[SlaveChangeHandler] server: {} is rejected", ip);
    }
}