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

import com.alipay.remoting.BizContext;
import com.alipay.remoting.Connection;
import com.alipay.remoting.util.StringUtils;
import com.glmapper.bridge.boot.cluster.MasterServer;
import com.glmapper.bridge.boot.cluster.SlaveClient;
import com.glmapper.bridge.boot.cluster.SlaveConnectionFactory;
import com.glmapper.bridge.boot.events.CompeteFailEvent;
import com.glmapper.bridge.boot.events.EventCenter;
import com.glmapper.bridge.boot.request.MasterDegradeRequest;
import com.glmapper.bridge.boot.response.RemotingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * master 降级处理器, 当 master 无法连接数据库时, 由新 master 通知老 master 降级为 slave
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:18 PM
 * @since:
 **/
@Component
public class MasterDegradeProcessor extends AbstractProcessor<MasterDegradeRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Autowired
    private SlaveClient         slaveClient;

    @Override
    public String interest() {
        return MasterDegradeRequest.class.getName();
    }

    @Override
    public MasterServer.ServerTypeEnum getType() {
        return MasterServer.ServerTypeEnum.SLAVE_SERVER;
    }

    @Override
    public Object handleRequest(BizContext bizCtx, MasterDegradeRequest request) throws Exception {
        //发送竞选 master 失败事件
        EventCenter.post(CompeteFailEvent.getInstance());
        RemotingResponse response = new RemotingResponse();
        try {
            //关闭所有 slave 的连接
            Collection<Connection> connections = SlaveConnectionFactory.getConnections();
            if (!CollectionUtils.isEmpty(connections)) {
                for (Connection connection : connections) {
                    //当前连接不关闭，由 slave 关闭
                    if (!StringUtils.equals(connection.getRemoteIP(), bizCtx.getRemoteHost())) {
                        connection.close();
                    }
                }
            }
            //向新的 master 建立长连接
            slaveClient.connectMaster(request.getIp());
            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            LOGGER.error("[MasterDegradeProcessor] connect master failed.", e);
            response.setSuccess(false);
            response.setErrorMsg(e.getMessage());
            return response;
        }
    }

}
