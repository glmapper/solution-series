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
package com.glmapper.bridge.boot.manager;

import com.alipay.remoting.NamedThreadFactory;
import com.glmapper.bridge.boot.enums.PurposePoolEnum;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * 线程池管理器
 *
 * @author: leishu (glmapper_2018@163.com) 2019/12/12 6:12 PM
 * @since:
 **/
public class ExecutorManager {

    private final static Logger                   LOGGER                 = LoggerFactory
                                                                             .getLogger(ExecutorManager.class);

    private static final ScheduledExecutorService SCHEDULER              = new ScheduledThreadPoolExecutor(
                                                                             1,
                                                                             new NamedThreadFactory(
                                                                                 "ArkSchedulerTask"));

    /**
     * 数据库心跳调度线程池
     */
    private static final ScheduledExecutorService DB_HEARTBEAT_SCHEDULER = Executors
                                                                             .newSingleThreadScheduledExecutor();

    /**
     * 默认的通用的线程池
     */
    private static final ThreadPoolExecutor       THREAD_POOL_EXECUTOR;

    /**
     * 用于处理集群事件的线程池
     */
    private static final ThreadPoolExecutor       CLUSTER_POOL_EXECUTOR;

    /**
     * 用于处理事件中心的线程池
     */
    private static final ThreadPoolExecutor       EVENT_CENTER_EXECUTOR;

    static {

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2000);
        CLUSTER_POOL_EXECUTOR = new ThreadPoolExecutor(10, 50, 30, TimeUnit.MINUTES, workQueue) {
            /**
             * @see ThreadPoolExecutor#afterExecute(Runnable, Throwable)
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    LOGGER
                        .error(String.format("ThreadPoolUncaughtException:%s", t.getMessage()), t);
                }
            }
        };

        LinkedBlockingQueue<Runnable> commonQueue = new LinkedBlockingQueue<>(2000);
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(200, 200, 300L, TimeUnit.SECONDS,
            commonQueue, new NamedThreadFactory("ArkSchedulerProcessor"),
            new ThreadPoolExecutor.CallerRunsPolicy());

        LinkedBlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<>(5000);
        EVENT_CENTER_EXECUTOR = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
            eventQueue, new ThreadFactoryBuilder().setNameFormat("EVENT-CENTER-%d").build());
    }

    public static ThreadPoolExecutor getThreadPoolByPurpose(PurposePoolEnum purpose) {
        switch (purpose) {
            case CLUSTER:
                return CLUSTER_POOL_EXECUTOR;
            case EVENT:
                return EVENT_CENTER_EXECUTOR;
            case COMMON:
            default:
                return THREAD_POOL_EXECUTOR;
        }
    }

    public static void startScheduler(Runnable task) {
        SCHEDULER.scheduleWithFixedDelay(task, 5, 5, TimeUnit.MINUTES);
    }

    public static void stopScheduler() {
        SCHEDULER.shutdown();
    }

    public static ScheduledExecutorService getDbHeartbeatScheduler() {
        return DB_HEARTBEAT_SCHEDULER;
    }

    public static void stopDbHeartbeatScheduler() {
        DB_HEARTBEAT_SCHEDULER.shutdown();
    }

}
