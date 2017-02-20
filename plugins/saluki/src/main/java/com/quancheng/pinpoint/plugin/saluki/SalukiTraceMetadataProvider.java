/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.pinpoint.plugin.saluki;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author shimingliu 2017年2月20日 上午11:50:33
 * @version SalukiTraceMetadataProvider.java, v 0.0.1 2017年2月20日 上午11:50:33 shimingliu
 */
public class SalukiTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(SalukiConstants.SALUKI_PROVIDER_SERVICE_TYPE);
        context.addServiceType(SalukiConstants.SALUKI_CONSUMER_SERVICE_TYPE);
        context.addAnnotationKey(SalukiConstants.SALUKI_ARGS_ANNOTATION_KEY);
        context.addAnnotationKey(SalukiConstants.SALUKI_RESULT_ANNOTATION_KEY);
    }

}
