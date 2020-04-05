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
package com.glmapper.bridge.boot.model;

import java.util.Date;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 10:49 AM
 * @since:
 **/
public class ServerEntity {

    private int    id;

    private String hostName;

    private String ip;

    private int    isMaster;

    private String status;

    private String masterLock;

    private Date   heartbeat;

    private Date   gmtSqlServerTime;

    private Date   gmtModify;

    private Date   gmtCreate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getIsMaster() {
        return isMaster;
    }

    public void setIsMaster(int isMaster) {
        this.isMaster = isMaster;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMasterLock() {
        return masterLock;
    }

    public void setMasterLock(String masterLock) {
        this.masterLock = masterLock;
    }

    public Date getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Date heartbeat) {
        this.heartbeat = heartbeat;
    }

    public Date getGmtSqlServerTime() {
        return gmtSqlServerTime;
    }

    public void setGmtSqlServerTime(Date gmtSqlServerTime) {
        this.gmtSqlServerTime = gmtSqlServerTime;
    }

    public Date getGmtModify() {
        return gmtModify;
    }

    public void setGmtModify(Date gmtModify) {
        this.gmtModify = gmtModify;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
}
