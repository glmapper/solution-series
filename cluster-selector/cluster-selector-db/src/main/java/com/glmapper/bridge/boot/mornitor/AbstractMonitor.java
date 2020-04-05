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
package com.glmapper.bridge.boot.mornitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.TimeUnit;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 4:09 PM
 * @since:
 **/
public abstract class AbstractMonitor implements Runnable, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMonitor.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        MonitorFactory.register(this);
    }

    /**
     * 获取名字
     *
     * @return
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s] monitor failed", getName()), e);
        }
    }

    /**
     * 执行业务
     */
    public abstract void handle() throws Exception;

    /**
     * 是否是master的检测器
     *
     * @return
     */
    public abstract ServerRoleEnum getRole();

    /**
     * 获取延时时间
     *
     * @return
     */
    public abstract long getInitialDelay();

    /**
     * 获取间隔时间
     *
     * @return
     */
    public abstract long getDelay();

    /**
     * 获取时间单位
     *
     * @return
     */
    public abstract TimeUnit getTimeUnit();

    /**
     * 角色枚举
     *
     * @author qian.lqlq
     * @version $Id: AbstractMonitor.java, v 0.1 2017年8月24日 下午5:43:22 qian.lqlq Exp $
     */
    enum ServerRoleEnum {
        MASTER, SLAVE, ALL;
    }

}
