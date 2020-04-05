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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/12/10 4:10 PM
 * @since:
 **/
public class MonitorFactory {

    /**
     * 检测器集合
     */
    private static final Multimap<Boolean, AbstractMonitor> MONITOR_MAP = ArrayListMultimap
                                                                            .create();

    /**
     * 注册检测器
     *
     * @param monitor
     */
    public static void register(AbstractMonitor monitor) {
        switch (monitor.getRole()) {
            case ALL:
                MONITOR_MAP.put(true, monitor);
                MONITOR_MAP.put(false, monitor);
                break;
            case MASTER:
                MONITOR_MAP.put(true, monitor);
                break;
            case SLAVE:
                MONITOR_MAP.put(false, monitor);
                break;
            default:
                break;
        }
    }

    /**
     * 获取检测器
     *
     * @param isMaster
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Collection<AbstractMonitor> getMonitors(boolean isMaster) {
        if (MONITOR_MAP.containsKey(isMaster)) {
            return MONITOR_MAP.get(isMaster);
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
