package com.glmapper.bridge.boot.cluster;

import com.glmapper.bridge.boot.enums.ServerStatusEnum;
import com.glmapper.bridge.boot.model.ServerEntity;
import com.glmapper.bridge.boot.service.ClusterService;
import com.glmapper.bridge.boot.support.LocalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: leishu (glmapper_2018@163.com) 2020/4/3 5:11 PM
 * @since:
 **/
@Component
public class MasterStatusListener {
    private final static Logger  MONITOR_LOGGER = LoggerFactory.getLogger("CLUSTER-MONITOR-LOGGER");

    @Autowired
    private ClusterService       clusterService;

    private static AtomicBoolean MASTER_READY   = new AtomicBoolean(false);

    public String getLeaderIp() {
        String leaderIp = null;
        try {
            ServerEntity master = clusterService.getMaster();
            if (master != null
                && ServerStatusEnum.valueOf(master.getStatus()) == ServerStatusEnum.RUNNING) {
                leaderIp = master.getIp();
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get master ip.", t);
        } finally {
            // 如果为空说明没有正常状态的 master 存在，直接抛出异常
            if (StringUtils.isEmpty(leaderIp)) {
                throw new RuntimeException("there is no available master leader exist.");
            }
        }
        return leaderIp;
    }

    public void onLeaderStart() {
        MASTER_READY.set(true);
        MONITOR_LOGGER.info("Selected as leader, leader address : {} ", LocalServer.IP);
    }

    public void onStartFollowing() {
        MONITOR_LOGGER.info("Selected as follower.");
    }

    public boolean isMasterStatusReady() {
        return MASTER_READY.get();
    }
}
