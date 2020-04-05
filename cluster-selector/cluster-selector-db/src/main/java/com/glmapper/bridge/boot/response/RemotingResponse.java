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
package com.glmapper.bridge.boot.response;

import java.io.Serializable;

/**
 * @author: leishu (glmapper_2018@163.com) 2019/9/21 11:16 AM
 * @since:
 **/
public class RemotingResponse<T> implements Serializable {
    /**
     * data for invoke side
     */
    private T       data;
    /**
     * mark current invoke is successfully, default false
     */
    private boolean success = false;
    /**
     * error info
     */
    private String  errorMsg;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "RemotingResponse{" + "data=" + data + ", success=" + success + ", errorMsg='"
               + errorMsg + '\'' + '}';
    }
}
