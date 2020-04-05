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
import com.glmapper.bridge.boot.events.CompeteMasterEvent;
import com.glmapper.bridge.boot.events.EventCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * master 断开连接事件监听器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:18 PM
 * @since:
 **/
public class MasterDisConnectEventProcessor implements ConnectionEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Override
    public void onEvent(String remoteAddr, Connection conn) {
        LOGGER.info("[MasterDisConnectEventProcessor] master connection closed. ip: {}",
            conn.getRemoteIP());
        //如果和 master 之间的连接断开, 则开始竞争 master
        EventCenter.post(CompeteMasterEvent.getInstance());
    }

}
