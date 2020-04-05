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

import com.alipay.remoting.InvokeCallback;
import com.glmapper.bridge.boot.cluster.SlaveClient;
import com.glmapper.bridge.boot.enums.PurposePoolEnum;
import com.glmapper.bridge.boot.request.MasterDegradeRequest;
import com.glmapper.bridge.boot.events.NotifyMasterDegradeEvent;
import com.glmapper.bridge.boot.manager.ExecutorManager;
import com.glmapper.bridge.boot.response.RemotingResponse;
import com.glmapper.bridge.boot.support.LocalServer;
import com.glmapper.bridge.boot.utils.TimeUtil;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 通知 master 降级处理器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 2:32 PM
 * @since:
 **/
public class NotifyMasterDegradeHandler extends AbstractEventHandler {

    private static final Logger LOGGER      = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    private static final int    RETRY_TIMES = 3;

    @Autowired
    private SlaveClient         slaveClient;

    @Subscribe
    public void onEvent(NotifyMasterDegradeEvent event) {
        for (int count = 0; count < RETRY_TIMES; count++) {
            if (slaveClient.isConnected()) {
                final AtomicBoolean isSuccess = new AtomicBoolean(false);
                //通知老的 master 改变角色, 并向新 master 建立长连接
                try {
                    slaveClient.invoke(new MasterDegradeRequest(LocalServer.IP),
                        new InvokeCallback() {
                            @Override
                            public void onResponse(Object response) {
                                RemotingResponse result = (RemotingResponse) response;
                                if (result.isSuccess()) {
                                    slaveClient.disConnect();
                                    isSuccess.set(true);
                                } else {
                                    LOGGER
                                        .info(
                                            "[NotifyMasterDegradeHandler] request handle failed, resultMsg: {}",
                                            result.getErrorMsg());
                                }
                            }

                            @Override
                            public void onException(Throwable e) {
                                LOGGER
                                    .error(
                                        "[NotifyMasterDegradeHandler] request handle failed, resultMsg: {}",
                                        e.getMessage(), e);
                            }

                            @Override
                            public Executor getExecutor() {
                                return ExecutorManager
                                    .getThreadPoolByPurpose(PurposePoolEnum.CLUSTER);
                            }
                        });
                } catch (Throwable e) {
                    LOGGER.error("[NotifyMasterDegradeHandler] notify failed", e);
                }
                //如果执行成功不再重试，否则随机延时进行重试，最多重试3次
                if (isSuccess.get()) {
                    break;
                } else {
                    TimeUtil.randomDelay(TimeUtil.RANDOM_DELAY);
                }
            } else {
                break;
            }
        }
    }

}
