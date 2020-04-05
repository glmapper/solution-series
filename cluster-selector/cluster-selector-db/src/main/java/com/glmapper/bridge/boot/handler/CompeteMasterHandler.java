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

import com.alipay.sofa.common.profile.StringUtil;
import com.glmapper.bridge.boot.cluster.MasterServer;
import com.glmapper.bridge.boot.cluster.SlaveClient;
import com.glmapper.bridge.boot.events.CompeteFailEvent;
import com.glmapper.bridge.boot.events.CompeteMasterEvent;
import com.glmapper.bridge.boot.events.CompeteSuccessEvent;
import com.glmapper.bridge.boot.events.EventCenter;
import com.glmapper.bridge.boot.events.NotifyMasterDegradeEvent;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import com.glmapper.bridge.boot.support.LocalServer;
import com.glmapper.bridge.boot.utils.TimeUtil;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 *
 * 竞选 Master 处理器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:02 PM
 * @since:
 **/
public class CompeteMasterHandler extends AbstractEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Autowired
    private ClusterService      clusterService;

    @Autowired
    private MasterServer        masterServer;

    @Autowired
    private SlaveClient         slaveClient;

    @Subscribe
    public void onEvent(CompeteMasterEvent event) {
        synchronized (this) {
            boolean isMaster = false;
            boolean needCompete = true;
            LOGGER.info("[CompeteMasterHandler] begin to compete master...");
            while (true) {
                try {
                    // 初始化服务端
                    masterServer.init();
                    // 初始化 客户端
                    slaveClient.init();
                    // 从 db 中查询当前 master 信息
                    ServerEntity master = clusterService.getMaster();
                    //如果不存在正常运行的 master, 则竞争 master, 利用数据库的唯一索引, 如果竞争失败则抛异常
                    if (master == null) {
                        LOGGER.info("[CompeteMasterHandler] no master, begin compete.");
                        clusterService.setMaster(LocalServer.HOSTNAME);
                        isMaster = true;
                        //通知老的 master 降级为 slave (防止老 master 无法连接 DB 时感知不到角色变化和新的 master)
                        EventCenter.post(NotifyMasterDegradeEvent.getInstance());
                        break;
                    } else {
                        if (!StringUtil.equals(master.getHostName(), LocalServer.HOSTNAME)) {
                            LOGGER
                                .info("[CompeteMasterHandler] master exist, begin to connect master.");
                            isMaster = false;
                            // 向 master 建立长连接
                            try {
                                slaveClient.connectMaster(master.getIp());
                            } catch (Exception e) {
                                LOGGER
                                    .error(
                                        "[CompeteMasterHandler] connect master failed. begin to check master status and compete.",
                                        e);
                                // 说明当前 master 状态有问题
                                // 存在一种情况，对 master 做了 kill -9 强制关闭了进程，导致不会去修改 DB 中的状态，此时 DB 中的 master 实际上已经是不可用状态了
                                // 这里在连接抛出异常时，尝试去 check 下 master 的心跳是否超时了。
                                Date heartbeat = master.getHeartbeat();
                                Date sqlServerTime = master.getGmtSqlServerTime();
                                boolean isTimeout = TimeUtil.isTimeout(heartbeat, sqlServerTime,
                                    TimeUtil.DB_TIMEOUT);
                                // 如果超时了
                                if (isTimeout) {
                                    String hostName = master.getHostName();
                                    LOGGER
                                        .info(
                                            "[CompeteMasterHandler] master[{}] is timeout, begin modify master status.",
                                            hostName);
                                    //将 master 的状态改为 DB_TIMEOUT, 同时改为非 master, heartbeat和gmtModify为乐观锁, 防止其他slave同时修改和此时更新心跳时间戳
                                    boolean isSuccess = clusterService.updateMasterDBTimeout(
                                        hostName, heartbeat, master.getGmtModify());
                                    //成功修改 master 状态后开始竞争 master, 否则不参与竞争
                                    if (isSuccess) {
                                        LOGGER
                                            .info("[CompeteMasterHandler] modify master status success, begin compete master.");
                                        EventCenter.post(CompeteMasterEvent.getInstance());
                                        // 发送竞选事件参与竞选
                                        break;
                                    }
                                    // 这里抛出让外层 catch 到，然后随机延时之后再竞选
                                    throw e;
                                }
                            }
                        } else {
                            // 如果本身已经成为 master, 则什么也不做
                            needCompete = false;
                            LOGGER
                                .info("[CompeteMasterHandler] master is local server, do nothing");
                        }
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.error("[CompeteMasterHandler] compete master failed", e);
                    //如果没有抢到 master, 随机延时一段时间再去抢
                    TimeUtil.randomDelay(TimeUtil.RANDOM_DELAY);
                }
            }
            if (needCompete) {
                LOGGER.info("[CompeteMasterHandler] compete master finish, isMaster: {}", isMaster);
                EventCenter.post(isMaster ? CompeteSuccessEvent.getInstance() : CompeteFailEvent
                    .getInstance());
            }
        }
    }
}
