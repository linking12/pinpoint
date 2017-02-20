/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.pinpoint.plugin.saluki.interceptor;

import java.net.InetSocketAddress;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.quancheng.pinpoint.plugin.saluki.SalukiConstants;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.RpcContext;

/**
 * @author shimingliu 2017年2月20日 下午6:31:27
 * @version SalukiConsumerInterceptor.java, v 0.0.1 2017年2月20日 下午6:31:27 shimingliu
 */
public class SalukiConsumerInterceptor implements AroundInterceptor {

    private final MethodDescriptor descriptor;
    private final TraceContext     traceContext;

    public SalukiConsumerInterceptor(TraceContext traceContext, MethodDescriptor descriptor){
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        Trace trace = this.getTrace(target);
        if (trace == null) {
            return;
        }
        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(SalukiConstants.SALUKI_CONSUMER_SERVICE_TYPE);
            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            RpcContext.getContext().setAttachment(SalukiConstants.META_TRANSACTION_ID, nextId.getTransactionId());
            RpcContext.getContext().setAttachment(SalukiConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            RpcContext.getContext().setAttachment(SalukiConstants.META_PARENT_SPAN_ID,
                                                  Long.toString(nextId.getParentSpanId()));
            RpcContext.getContext().setAttachment(SalukiConstants.META_PARENT_APPLICATION_TYPE,
                                                  Short.toString(traceContext.getServerTypeCode()));
            RpcContext.getContext().setAttachment(SalukiConstants.META_PARENT_APPLICATION_NAME,
                                                  traceContext.getApplicationName());
            RpcContext.getContext().setAttachment(SalukiConstants.META_FLAGS, Short.toString(nextId.getFlags()));
        } else {
            RpcContext.getContext().setAttachment(SalukiConstants.META_DO_NOT_TRACE, "1");
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = this.getTrace(target);
        if (trace == null) {
            return;
        }
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            if (throwable == null) {
                String endPoint = getProviderServer();
                recorder.recordEndPoint(endPoint);
                recorder.recordDestinationId(endPoint);
                recorder.recordAttribute(SalukiConstants.SALUKI_ARGS_ANNOTATION_KEY, args[3]);
                recorder.recordAttribute(SalukiConstants.SALUKI_RESULT_ANNOTATION_KEY, result);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getProviderServer() {
        Object obj = RpcContext.getContext().get(Constants.PROVIDER_ADDRESS);
        if (obj instanceof InetSocketAddress) {
            InetSocketAddress provider = (InetSocketAddress) obj;
            return provider.toString();
        } else {
            return new InetSocketAddress(0).toString();
        }

    }

    private Trace getTrace(Object target) {
        return traceContext.currentTraceObject();
    }

}
