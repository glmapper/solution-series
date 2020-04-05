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

import com.alipay.remoting.Connection;
import com.alipay.remoting.util.StringUtils;
import com.glmapper.bridge.boot.cluster.SlaveClient;
import com.glmapper.bridge.boot.events.CompeteMasterEvent;
import com.glmapper.bridge.boot.events.CompeteSuccessEvent;
import com.glmapper.bridge.boot.events.EventCenter;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import com.glmapper.bridge.boot.support.LocalServer;
import com.glmapper.bridge.boot.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 对于 slave 来说，需要持续对 master 状态进行检测
 *
 * @author qian.lqlq
 * @version $Id: MasterStatusMonitor.java, v 0.1 2017年8月10日 下午4:51:03 qian.lqlq Exp $
 */
@Component
public class MasterStatusMonitor extends AbstractMonitor {

    private final static Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Autowired
    private ClusterService      clusterService;

    @Autowired
    private SlaveClient         slaveClient;

    @Override
    public void handle() throws Exception {
        //判断 master 是否 DB 超时, 如果超时或不存在则竞争 master
        ServerEntity master = clusterService.getMaster();
        if (master == null) {
            LOGGER.info("[MasterStatusMonitor] no master, begin compete.");
            EventCenter.post(CompeteMasterEvent.getInstance());
            return;
        } else if (StringUtils.equals(master.getHostName(), LocalServer.HOSTNAME)) {
            LOGGER.info("[MasterStatusMonitor] master is self, post success event.");
            EventCenter.post(CompeteSuccessEvent.getInstance());
            return;
        }

        Date heartbeat = master.getHeartbeat();
        Date sqlServerTime = master.getGmtSqlServerTime();
        boolean isTimeout = TimeUtil.isTimeout(heartbeat, sqlServerTime, TimeUtil.DB_TIMEOUT);
        if (isTimeout) {
            String hostName = master.getHostName();
            LOGGER.info("[MasterStatusMonitor] master[{}] is timeout, begin modify master status.",
                hostName);
            //将master的状态改为DB_TIMEOUT, 同时改为非master, heartbeat和gmtModify为乐观锁, 防止其他slave同时修改和此时更新心跳时间戳
            boolean isSuccess = clusterService.updateMasterDBTimeout(hostName, heartbeat,
                master.getGmtModify());
            //成功修改 master 状态后开始竞争 master, 否则不参与竞争
            if (isSuccess) {
                LOGGER
                    .info("[MasterStatusMonitor] modify master status success, begin compete master.");
                EventCenter.post(CompeteMasterEvent.getInstance());
            }
        } else {
            //判断slave是否连接上了新的master, 如果没有连接或连的不是当前master, 则发送竞选事件连接新master
            //当slave处于DB_TIMEOUT状态时, master发生变更后, 可能无法及时感知
            Connection connection = slaveClient.getConnection();
            if (connection != null && connection.isFine()) {
                String masterIp = master.getIp();
                String connectionIp = connection.getRemoteIP();
                if (StringUtils.equals(connectionIp, masterIp)) {
                    return;
                } else {
                    LOGGER.info(
                        "[MasterStatusMonitor] now connect: {}, is not the current master: {}",
                        connectionIp, masterIp);
                    slaveClient.disConnect();
                }
            } else {
                LOGGER.info("[MasterStatusMonitor] now no connection");
            }
            EventCenter.post(CompeteMasterEvent.getInstance());
        }
    }

    @Override
    public ServerRoleEnum getRole() {
        return ServerRoleEnum.SLAVE;
    }

    @Override
    public long getInitialDelay() {
        return 1;
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
