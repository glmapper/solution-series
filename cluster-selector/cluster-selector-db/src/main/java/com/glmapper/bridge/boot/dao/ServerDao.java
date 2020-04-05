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
package com.glmapper.bridge.boot.dao;

import com.glmapper.bridge.boot.model.ServerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;

import java.util.Date;
import java.util.List;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 10:47 AM
 * @since:
 **/
@Mapper
public interface ServerDao {

    /**
     * 添加server
     *
     * @param server
     * @return
     * @throws DataAccessException
     */
    int insert(ServerEntity server) throws DataAccessException;

    /**
     * 判断是否存在
     *
     * @param hostName
     * @param ip
     * @return
     * @throws DataAccessException
     */
    Integer getIdByHostOrIp(@Param("hostName") String hostName, @Param("ip") String ip)
                                                                                       throws DataAccessException;

    /**
     * 查询指定IP的server
     *
     * @param ip
     * @return
     * @throws DataAccessException
     */
    ServerEntity getByIP(String ip) throws DataAccessException;

    /**
     * 查询指定zone和状态的master
     *
     * @param isMaster
     * @param status
     * @return
     * @throws DataAccessException
     */
    ServerEntity getMaster(@Param("isMaster") int isMaster, @Param("status") String status)
                                                                                           throws DataAccessException;

    /**
     * 查询指定zone的slave
     *
     * @param isMaster
     * @return
     * @throws DataAccessException
     */
    List<ServerEntity> getSlaves(@Param("isMaster") int isMaster) throws DataAccessException;

    /**
     * 修改指定server的状态, heartbeat和gmtModify为乐观锁
     *
     * @param status
     * @param hostName
     * @param heartbeat
     * @param gmtModify
     * @return
     * @throws DataAccessException
     */
    int updateStatus(@Param("status") String status, @Param("hostName") String hostName,
                     @Param("heartbeat") Date heartbeat, @Param("gmtModify") Date gmtModify)
                                                                                            throws DataAccessException;

    /**
     * 修改指定server的状态和master标识
     *
     * @param status
     * @param isMaster
     * @param masterLock
     * @param hostName
     * @param heartbeat
     * @param gmtModify
     * @return
     * @throws DataAccessException
     */
    int updateStatusAndIsMaster(@Param("status") String status, @Param("isMaster") int isMaster,
                                @Param("masterLock") String masterLock,
                                @Param("hostName") String hostName,
                                @Param("heartbeat") Date heartbeat,
                                @Param("gmtModify") Date gmtModify) throws DataAccessException;

    /**
     * 修改server信息
     *
     * @param isMaster
     * @param ip
     * @param status
     * @param masterLock
     * @param hostName
     * @return
     * @throws DataAccessException
     */
    int update(@Param("hostName") String hostName, @Param("isMaster") int isMaster,
               @Param("ip") String ip, @Param("status") String status,
               @Param("masterLock") String masterLock, @Param("id") int id)
                                                                           throws DataAccessException;

    /**
     * 设置master
     *
     * @param isMaster
     * @param masterLock
     * @param status
     * @param hostName
     * @return
     * @throws DataAccessException
     */
    int setMaster(@Param("isMaster") int isMaster, @Param("masterLock") String masterLock,
                  @Param("status") String status, @Param("hostName") String hostName)
                                                                                     throws DataAccessException;

    /**
     * 更新心跳
     *
     * @param hostName
     * @return
     * @throws DataAccessException
     */
    int heartbeat(String hostName) throws DataAccessException;

    /**
     * 修改指定server的状态
     *
     * @param status
     * @param ip
     * @throws DataAccessException
     */
    void updateStatusByIp(@Param("status") String status, @Param("ip") String ip);
}
