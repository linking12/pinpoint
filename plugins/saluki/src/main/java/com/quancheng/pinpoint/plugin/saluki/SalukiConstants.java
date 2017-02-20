/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.pinpoint.plugin.saluki;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author shimingliu 2017年2月20日 下午1:07:38
 * @version SalukiConstants.java, v 0.0.1 2017年2月20日 下午1:07:38 shimingliu
 */
public interface SalukiConstants {

    ServiceType   SALUKI_PROVIDER_SERVICE_TYPE = ServiceTypeFactory.of(1110, "SALUKI_PROVIDER", RECORD_STATISTICS);
    ServiceType   SALUKI_CONSUMER_SERVICE_TYPE = ServiceTypeFactory.of(9110, "SALUKI_CONSUMER", RECORD_STATISTICS);
    AnnotationKey SALUKI_ARGS_ANNOTATION_KEY   = AnnotationKeyFactory.of(90, "saluki.args");
    AnnotationKey SALUKI_RESULT_ANNOTATION_KEY = AnnotationKeyFactory.of(91, "saluki.result");

    String        META_DO_NOT_TRACE            = "_SALUKI_DO_NOT_TRACE";
    String        META_TRANSACTION_ID          = "_SALUKI_TRASACTION_ID";
    String        META_SPAN_ID                 = "_SALUKI_SPAN_ID";
    String        META_PARENT_SPAN_ID          = "_SALUKI_PARENT_SPAN_ID";
    String        META_PARENT_APPLICATION_NAME = "_SALUKI_PARENT_APPLICATION_NAME";
    String        META_PARENT_APPLICATION_TYPE = "_SALUKI_PARENT_APPLICATION_TYPE";
    String        META_FLAGS                   = "_SALUKI_FLAGS";

}
