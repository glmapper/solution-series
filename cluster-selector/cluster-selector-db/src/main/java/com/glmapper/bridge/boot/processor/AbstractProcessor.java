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

import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.glmapper.bridge.boot.cluster.MasterServer;
import com.glmapper.bridge.boot.enums.PurposePoolEnum;
import com.glmapper.bridge.boot.manager.ExecutorManager;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.Executor;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:15 PM
 * @since:
 **/
public abstract class AbstractProcessor<T> extends SyncUserProcessor<T> implements InitializingBean {

    /**
     * 获取server类型
     *
     * @return
     */
    public abstract MasterServer.ServerTypeEnum getType();

    @Override
    public void afterPropertiesSet() throws Exception {
        MasterServer.registerProcessor(this);
    }

    @Override
    public Executor getExecutor() {
        return ExecutorManager.getThreadPoolByPurpose(PurposePoolEnum.CLUSTER);
    }

}
