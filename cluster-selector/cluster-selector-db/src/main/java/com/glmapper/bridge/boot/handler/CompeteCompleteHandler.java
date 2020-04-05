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

import com.glmapper.bridge.boot.cluster.MasterStatusListener;
import com.glmapper.bridge.boot.events.CompeteFailEvent;
import com.glmapper.bridge.boot.events.CompeteSuccessEvent;
import com.glmapper.bridge.boot.mornitor.AbstractMonitor;
import com.glmapper.bridge.boot.mornitor.MonitorFactory;
import com.glmapper.bridge.boot.support.LocalServer;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * 完成竞选处理器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 4:05 PM
 * @since:
 **/
public class CompeteCompleteHandler extends AbstractEventHandler {

    private final static Logger      LOGGER            = LoggerFactory
                                                           .getLogger("CLUSTER-MONITOR-LOGGER");

    private ScheduledExecutorService service;

    private AtomicBoolean            isRunning         = new AtomicBoolean(false);

    private static final int         MONITOR_POOL_SIZE = 10;

    private final boolean            isStartMonitor;

    @Autowired
    private MasterStatusListener     masterStatusListener;

    public CompeteCompleteHandler(boolean isStartMonitor) {
        this.isStartMonitor = isStartMonitor;
    }

    /**
     * 竞选成功
     * @param event
     */
    @Subscribe
    public void onSuccess(CompeteSuccessEvent event) {
        synchronized (this) {
            // 如果之前是 slave, 先停止 slave 监视任务
            if (!LocalServer.isMaster()) {
                if (service != null) {
                    service.shutdownNow();
                }
                isRunning.set(false);
                LocalServer.setMaster(true);
                masterStatusListener.onLeaderStart();
                LOGGER.info("[CompeteCompleteHandler] local server change from slave to master.");
            }
            run();
        }
    }

    /**
     * 竞选失败
     * @param event
     */
    @Subscribe
    public void onFailed(CompeteFailEvent event) {
        synchronized (this) {
            //如果之前是 master, 先停止 master 监视任务
            if (LocalServer.isMaster()) {
                if (service != null) {
                    service.shutdownNow();
                }
                isRunning.set(false);
                LocalServer.setMaster(false);
                masterStatusListener.onStartFollowing();
                LOGGER.info("[CompeteCompleteHandler] local server change from master to slave.");
            }
            run();
        }
    }

    /**
     * 启动监视
     */
    private void run() {
        if (!isRunning.get()) {
            LOGGER.info("[CompeteCompleteHandler] begin to start monitor, isMaster: {}",
                LocalServer.isMaster());
            Collection<AbstractMonitor> monitors = MonitorFactory.getMonitors(LocalServer
                .isMaster());
            if (isStartMonitor && !CollectionUtils.isEmpty(monitors)) {
                service = Executors.newScheduledThreadPool(MONITOR_POOL_SIZE);
                for (AbstractMonitor monitor : monitors) {
                    String name = monitor.getName();
                    try {
                        service.scheduleWithFixedDelay(monitor, monitor.getInitialDelay(),
                            monitor.getDelay(), monitor.getTimeUnit());
                        LOGGER.info(
                            "[CompeteCompleteHandler] start monitor success, monitor is: {}", name);
                    } catch (Throwable e) {
                        LOGGER.error(
                            "[CompeteCompleteHandler] start monitor error, monitor is: {}", name);
                        return;
                    }
                }
            }
            isRunning.set(true);
        }
    }

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }
}
