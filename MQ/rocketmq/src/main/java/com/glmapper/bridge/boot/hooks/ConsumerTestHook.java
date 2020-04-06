package com.glmapper.bridge.boot.hooks;

import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: guolei.sgl (glmapper_2018@163.com) 2020/4/6 10:39 AM
 * @since:
 **/
public class ConsumerTestHook implements ConsumeMessageHook {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConsumerTestHook.class);

    @Override
    public String hookName() {
        return ConsumerTestHook.class.getName();
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext consumeMessageContext) {
        LOGGER.info("execute consumeMessageBefore. consumeMessageContext: {}",
            consumeMessageContext);
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext consumeMessageContext) {
        LOGGER
            .info("execute consumeMessageAfter. consumeMessageContext: {}", consumeMessageContext);
    }
}
