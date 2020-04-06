package com.glmapper.bridge.boot.hooks;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: guolei.sgl (glmapper_2018@163.com) 2020/4/6 10:39 AM
 * @since:
 **/
public class ProducerTestHook implements SendMessageHook {

    public static final Logger LOGGER = LoggerFactory.getLogger(ProducerTestHook.class);

    @Override
    public String hookName() {
        return ProducerTestHook.class.getName();
    }

    @Override
    public void sendMessageBefore(SendMessageContext sendMessageContext) {
        LOGGER.info("execute sendMessageBefore. sendMessageContext:{}", sendMessageContext);
    }

    @Override
    public void sendMessageAfter(SendMessageContext sendMessageContext) {
        LOGGER.info("execute sendMessageAfter. sendMessageContext:{}", sendMessageContext);
    }
}
