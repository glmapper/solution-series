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

import com.glmapper.bridge.boot.enums.PurposePoolEnum;
import com.glmapper.bridge.boot.handler.AbstractEventHandler;
import com.glmapper.bridge.boot.manager.ExecutorManager;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * 事件中心
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 12:39 PM
 * @since:
 **/
public class EventCenter {

    private static final Logger             LOGGER   = LoggerFactory.getLogger(EventCenter.class);

    private static final ThreadPoolExecutor EXECUTOR = ExecutorManager
                                                         .getThreadPoolByPurpose(PurposePoolEnum.EVENT);

    private static final AsyncEventBus      ASYNC_EVENT_BUS;

    static {
        SubscriberExceptionHandler handler = new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                LOGGER.error("[EventCenter] dispatch event error", exception);
            }
        };
        ASYNC_EVENT_BUS = new AsyncEventBus(EXECUTOR, handler);
    }

    /**
     * 注册事件处理器
     *
     * @param handler
     */
    public static void register(AbstractEventHandler handler) {
        Method[] methods = handler.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                ASYNC_EVENT_BUS.register(handler);
                return;
            }
        }
    }

    /**
     * 投递事件
     * @param event
     */
    public static void post(ArkSchedulerEvent event) {
        ASYNC_EVENT_BUS.post(event);
    }
}
