/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.pinpoint.plugin.saluki;

import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author shimingliu 2017年2月20日 下午1:06:07
 * @version SalukiConfiguration.java, v 0.0.1 2017年2月20日 下午1:06:07 shimingliu
 */
public class SalukiConfiguration {

    private final boolean      salukiEnabled;
    private final List<String> salukiBootstrapMains;

    public SalukiConfiguration(ProfilerConfig config){
        this.salukiEnabled = config.readBoolean("profiler.saluki.enable", true);
        this.salukiBootstrapMains = config.readList("profiler.saluki.bootstrap.main");
    }

    public boolean isSalukiEnabled() {
        return salukiEnabled;
    }

    public List<String> getSalukiBootstrapMains() {
        return salukiBootstrapMains;
    }

}
