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
package com.glmapper.bridge.boot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Random;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 12:30 PM
 * @since:
 **/
public class TimeUtil {

    /**
     * 日志
     */
    private static final Logger LOGGER       = LoggerFactory.getLogger(TimeUtil.class);

    /**
     * 随机延时上限
     */
    public static final Integer RANDOM_DELAY = 1000;

    /**
     * DB超时时间
     */
    public static Integer       DB_TIMEOUT   = 3000;

    /**
     * 判断是否超时
     *
     * @param date      要判断的时间
     * @param timeout   指定的超时范围
     * @return
     */
    public static boolean isTimeout(Date date, int timeout) {
        return System.currentTimeMillis() - date.getTime() > timeout;
    }

    public static boolean isTimeout(Date source, Date target, int timeout) {
        return target.getTime() - source.getTime() > timeout;
    }

    /**
     * 随机延时
     *
     * @param delayScope
     */
    public static void randomDelay(int delayScope) {
        Random random = new Random();
        delay(random.nextInt(delayScope));
    }

    /**
     * 固定延时
     *
     * @param delay
     */
    public static void delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[TimeUtil] delay ％s error", delay), e);
        }
    }
}
