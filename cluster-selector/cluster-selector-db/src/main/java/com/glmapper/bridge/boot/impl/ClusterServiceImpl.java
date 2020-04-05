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
package com.glmapper.bridge.boot.impl;

import com.glmapper.bridge.boot.constants.Constants;
import com.glmapper.bridge.boot.dao.ServerDao;
import com.glmapper.bridge.boot.enums.ServerStatusEnum;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 10:46 AM
 * @since:
 **/
@Service
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ServerDao serverDao;

    @Override
    public int insert(ServerEntity entity) {
        serverDao.insert(entity);
        return entity.getId();
    }

    @Override
    public Integer getIdByHostOrIp(String hostname, String ip) {
        return serverDao.getIdByHostOrIp(hostname, ip);
    }

    @Override
    public ServerEntity getByIP(String ip) {
        return serverDao.getByIP(ip);
    }

    @Override
    public ServerEntity getMaster() {
        return serverDao.getMaster(1, ServerStatusEnum.RUNNING.name());
    }

    @Override
    public List<ServerEntity> getSlaves() {
        return serverDao.getSlaves(0);
    }

    @Override
    public void heartbeat(String hostName) {
        serverDao.heartbeat(hostName);
    }

    @Override
    public void online(int id, String hostName, String ip, String masterLock) {
        serverDao.update(hostName, 0, ip, ServerStatusEnum.RUNNING.name(), masterLock, id);
    }

    @Override
    public boolean updateStatus(ServerStatusEnum status, String hostName, Date heartbeat,
                                Date gmtModify) {
        return serverDao.updateStatus(status.name(), hostName, heartbeat, gmtModify) > 0;
    }

    @Override
    public void reject(String ip) {
        serverDao.updateStatusByIp(ServerStatusEnum.REJECT.name(), ip);
    }

    @Override
    public void running(String ip) {
        serverDao.updateStatusByIp(ServerStatusEnum.RUNNING.name(), ip);
    }

    @Override
    public void setMaster(String hostname) {
        serverDao.setMaster(1, Constants.MASTER_LOCK, ServerStatusEnum.RUNNING.name(), hostname);
    }

    @Override
    public boolean updateMasterDBTimeout(String hostname, Date heartbeat, Date gmtModify) {
        return serverDao.updateStatusAndIsMaster(ServerStatusEnum.DB_TIMEOUT.name(), 0,
            Constants.SLAVE_SERVER_PREFIX + hostname, hostname, heartbeat, gmtModify) > 0;
    }
}
