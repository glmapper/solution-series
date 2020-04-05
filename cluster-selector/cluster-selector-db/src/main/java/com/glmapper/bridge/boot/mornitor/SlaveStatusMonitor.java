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
package com.glmapper.bridge.boot.mornitor;

import com.alipay.remoting.util.StringUtils;
import com.glmapper.bridge.boot.cluster.SlaveConnectionFactory;
import com.glmapper.bridge.boot.enums.ServerStatusEnum;
import com.glmapper.bridge.boot.events.CompeteFailEvent;
import com.glmapper.bridge.boot.events.CompeteMasterEvent;
import com.glmapper.bridge.boot.events.EventCenter;
import com.glmapper.bridge.boot.events.SlaveChangeEvent;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import com.glmapper.bridge.boot.support.LocalServer;
import com.glmapper.bridge.boot.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * 对于 master 来说，需要去检测 slave 的状态
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 4:13 PM
 * @since:
 **/
@Component
public class SlaveStatusMonitor extends AbstractMonitor {

    private final static Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Autowired
    private ClusterService      clusterService;

    @Override
    public void handle() throws Exception {
        //监测 slave 状态
        //首先检测自己是否还是 master, 如果不是则修改当前角色信息, 不再执行 master 的功能
        ServerEntity master = clusterService.getMaster();
        // 如果 master 不存在，则马上进行参与 master 竞选
        if (master == null) {
            EventCenter.post(CompeteMasterEvent.getInstance());
            return;
        } else if (!StringUtils.equals(master.getHostName(), LocalServer.HOSTNAME)) {
            // 不在是 master 了，发布一个竞选失败的事件，取消自己 master 的能力
            EventCenter.post(CompeteFailEvent.getInstance());
            return;
        }

        // 还是 master
        // 先去拿到所有的 slaves
        List<ServerEntity> slaves = clusterService.getSlaves();
        // 是不是有 slaves
        if (!CollectionUtils.isEmpty(slaves)) {
            // 开始遍历所有的 slaves
            for (ServerEntity slave : slaves) {
                // 客户端机器更新的最后心跳时间
                Date heartbeat = slave.getHeartbeat();
                // 当前数据库的数据
                Date sqlServerTime = slave.getGmtSqlServerTime();
                Date gmtModify = slave.getGmtModify();
                String hostName = slave.getHostName();
                String ip = slave.getIp();
                ServerStatusEnum slaveStatus = ServerStatusEnum.valueOf(slave.getStatus());
                // 是否连接 DB 超时
                boolean isTimeout = TimeUtil.isTimeout(heartbeat, sqlServerTime,
                    TimeUtil.DB_TIMEOUT);
                switch (slaveStatus) {
                // 正常运行状态
                    case RUNNING:
                        // 是否超时
                        if (isTimeout) {
                            //如果超时,但是存在长连接, 修改状态为 DB_TIMEOUT
                            ServerStatusEnum status = ServerStatusEnum.DB_TIMEOUT;
                            //修改状态成功后发送 slaveChangeEvent 事件
                            if (clusterService.updateStatus(status, hostName, heartbeat, gmtModify)) {
                                LOGGER
                                    .info(
                                        "[SlaveStatusMonitor] slave: {}, status change from: {} to: {}",
                                        ip, slaveStatus.name(), status.name());
                                EventCenter.post(new SlaveChangeEvent(ip, status));
                            }
                        }
                        break;
                    // DB 超时
                    case DB_TIMEOUT:
                        if (!isTimeout) {
                            //如果没有超时,修改状态为 RUNNING
                            clusterService.running(ip);
                            LOGGER.info(
                                "[SlaveStatusMonitor] slave: {} status change from: {} to: {}", ip,
                                slaveStatus.name(), ServerStatusEnum.RUNNING.name());
                            EventCenter.post(new SlaveChangeEvent(ip, ServerStatusEnum.RUNNING));
                        }
                        break;
                    // 剔除
                    case REJECT:
                        //如果有心跳并且存在长连接, 修改状态为 RUNNING, 修改成功发送 SlaveChangeEvent
                        if (!isTimeout && SlaveConnectionFactory.containsIP(ip)) {
                            clusterService.running(ip);
                            LOGGER.info(
                                "[SlaveStatusMonitor] slave: {} status change from: {} to: {}", ip,
                                slaveStatus.name(), ServerStatusEnum.RUNNING.name());
                            EventCenter.post(new SlaveChangeEvent(ip, ServerStatusEnum.RUNNING));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public ServerRoleEnum getRole() {
        return ServerRoleEnum.MASTER;
    }

    @Override
    public long getInitialDelay() {
        return 2;
    }

    @Override
    public long getDelay() {
        return 1;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

}
