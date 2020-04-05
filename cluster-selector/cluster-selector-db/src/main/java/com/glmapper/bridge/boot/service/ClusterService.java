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
package com.glmapper.bridge.boot.service;

import com.glmapper.bridge.boot.enums.ServerStatusEnum;
import com.glmapper.bridge.boot.model.ServerEntity;

import java.util.Date;
import java.util.List;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 10:42 AM
 * @since:
 **/
public interface ClusterService {

    /**
     * 添加server信息
     * @param server server
     * @return
     */
    int insert(ServerEntity server);

    /**
     * 根据主机名或ip判断是否存在
     *
     * @param hostName
     * @param ip
     * @return
     */
    Integer getIdByHostOrIp(String hostName, String ip);

    /**
     * 根据IP查询server
     * @param ip
     * @return
     */
    ServerEntity getByIP(String ip);

    /**
     * 获取master
     *
     * @return
     */
    ServerEntity getMaster();

    /**
     * 获取指定zone的所有slave
     *
     * @return
     */
    List<ServerEntity> getSlaves();

    /**
     * 更新心跳时间戳
     *
     * @param hostName
     */
    void heartbeat(String hostName);

    /**
     * server上线
     * @param id
     * @param hostName
     * @param ip
     * @param masterLock
     */
    void online(int id, String hostName, String ip, String masterLock);

    /**
     * 修改server状态, heartbeat和gmtModify为乐观锁
     *
     * @param status
     * @param hostname
     * @param heartbeat
     * @param gmtModify
     * @return
     */
    boolean updateStatus(ServerStatusEnum status, String hostname, Date heartbeat, Date gmtModify);

    /**
     * 将指定机器的状态改为 REJECT
     *
     * @param ip
     */
    void reject(String ip);

    /**
     * 将指定机器的状态改为 RUNNING
     *
     * @param ip
     */
    void running(String ip);

    /**
     * 设置 master
     *
     * @param hostName
     */
    void setMaster(String hostName);

    /**
     * 修改master为DB超时状态
     *
     * @param hostName
     * @param heartbeat
     * @param gmtModify
     * @return
     */
    boolean updateMasterDBTimeout(String hostName, Date heartbeat, Date gmtModify);
}
