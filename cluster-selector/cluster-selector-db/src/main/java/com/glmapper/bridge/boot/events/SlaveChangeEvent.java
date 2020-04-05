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
package com.glmapper.bridge.boot.events;

import com.glmapper.bridge.boot.enums.ServerStatusEnum;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *
 * slave 状态变更事件
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 3:21 PM
 * @since:
 **/
public class SlaveChangeEvent implements ArkSchedulerEvent, Delayed {
    /**
     * slave的IP
     */
    private String           ip;

    /**
     * slave状态
     */
    private ServerStatusEnum status;

    /**
     * 延迟时间，单位ms
     */
    private long             delayMs;

    /**
     * 创建时间
     */
    private long             createTimestamp;

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(createTimestamp + delayMs - System.currentTimeMillis(),
            TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == this) {
            return 0;
        }
        if (o instanceof SlaveChangeEvent) {
            SlaveChangeEvent other = (SlaveChangeEvent) o;
            if (this.createTimestamp < other.createTimestamp) {
                return -1;
            } else if (this.createTimestamp > other.createTimestamp) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    public SlaveChangeEvent(String ip, ServerStatusEnum status) {
        this.ip = ip;
        this.status = status;
        this.delayMs = 0;
        this.createTimestamp = System.currentTimeMillis();
    }

    public String getIp() {
        return ip;
    }

    public ServerStatusEnum getStatus() {
        return status;
    }

}
