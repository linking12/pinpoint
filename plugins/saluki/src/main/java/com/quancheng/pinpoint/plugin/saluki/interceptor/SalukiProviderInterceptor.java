/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.pinpoint.plugin.saluki.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.quancheng.pinpoint.plugin.saluki.SalukiConstants;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.server.internal.ServerInvocation;

/**
 * @author shimingliu 2017年2月20日 下午6:31:44
 * @version SalukiProviderInterceptor.java, v 0.0.1 2017年2月20日 下午6:31:44 shimingliu
 */
public class SalukiProviderInterceptor extends SpanSimpleAroundInterceptor {

  public SalukiProviderInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
    super(traceContext, descriptor, SalukiProviderInterceptor.class);
  }

  @Override
  protected Trace createTrace(Object arg0, Object[] arg1) {
    if (RpcContext.getContext().getAttachment(SalukiConstants.META_DO_NOT_TRACE) != null) {
      return traceContext.disableSampling();
    }
    String transactionId =
        RpcContext.getContext().getAttachment(SalukiConstants.META_TRANSACTION_ID);
    if (transactionId == null) {
      return traceContext.newTraceObject();
    }
    long parentSpanID = NumberUtils.parseLong(
        RpcContext.getContext().getAttachment(SalukiConstants.META_PARENT_SPAN_ID), SpanId.NULL);
    long spanID = NumberUtils.parseLong(
        RpcContext.getContext().getAttachment(SalukiConstants.META_SPAN_ID), SpanId.NULL);
    short flags = NumberUtils
        .parseShort(RpcContext.getContext().getAttachment(SalukiConstants.META_FLAGS), (short) 0);
    TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);

    return traceContext.continueTraceObject(traceId);
  }

  @Override
  protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result,
      Throwable throwable) {
    recorder.recordApi(methodDescriptor);
    recorder.recordAttribute(SalukiConstants.SALUKI_ARGS_ANNOTATION_KEY, args);
    if (throwable == null) {
      recorder.recordAttribute(SalukiConstants.SALUKI_RESULT_ANNOTATION_KEY, result);
    } else {
      recorder.recordException(throwable);
    }

  }

  @Override
  protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
    ServerInvocation invocation = (ServerInvocation) target;
    recorder.recordServiceType(SalukiConstants.SALUKI_PROVIDER_SERVICE_TYPE);
    recorder.recordRpcName(invocation.getRpcName());
    recorder.recordEndPoint(invocation.getLocalAddressString());
    recorder.recordRemoteAddress(RpcContext.getContext().getAttachment(Constants.REMOTE_ADDRESS));
    if (!recorder.isRoot()) {
      String parentApplicationName =
          RpcContext.getContext().getAttachment(SalukiConstants.META_PARENT_APPLICATION_NAME);
      if (parentApplicationName != null) {
        short parentApplicationType = NumberUtils.parseShort(
            RpcContext.getContext().getAttachment(SalukiConstants.META_PARENT_APPLICATION_TYPE),
            ServiceType.UNDEFINED.getCode());
        recorder.recordParentApplication(parentApplicationName, parentApplicationType);
        recorder.recordAcceptorHost(invocation.getLocalAddressString());
      }
    }

  }

}
