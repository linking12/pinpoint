/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.pinpoint.plugin.saluki;

import java.security.ProtectionDomain;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author shimingliu 2017年2月20日 上午11:50:08
 * @version SalukiPlugin.java, v 0.0.1 2017年2月20日 上午11:50:08 shimingliu
 */
public class SalukiPlugin implements ProfilerPlugin, TransformTemplateAware {

  private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

  private TransformTemplate transformTemplate;

  @Override
  public void setup(ProfilerPluginSetupContext context) {
    SalukiConfiguration config = new SalukiConfiguration(context.getConfig());
    if (!config.isSalukiEnabled()) {
      logger.info("SalukiPlugin disabled");
      return;
    }
    this.addApplicationTypeDetector(context);
    this.addTransformers();
  }

  private void addTransformers() {
    transformTemplate.transform(
        "com.quancheng.saluki.core.grpc.client.internal.unary.GrpcHystrixCommand",
        new TransformCallback() {

          @Override
          public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
              String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
              byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target =
                instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            List<InstrumentMethod> methods = target.getDeclaredMethods(new MethodFilter() {

              @Override
              public boolean accept(InstrumentMethod method) {
                return method.getName().equals("run");
              }
            });
            if (methods.size() == 1) {
              methods.get(0).addInterceptor(
                  "com.quancheng.pinpoint.plugin.saluki.interceptor.SalukiConsumerInterceptor");
            }
            return target.toBytecode();
          }
        });
    transformTemplate.transform("com.quancheng.saluki.core.grpc.server.internal.ServerInvocation",
        new TransformCallback() {

          @Override
          public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
              String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
              byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target =
                instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target
                .getDeclaredMethod("invoke", "com.google.protobuf.Message",
                    "io.grpc.stub.StreamObserver")
                .addInterceptor(
                    "com.quancheng.pinpoint.plugin.saluki.interceptor.SalukiProviderInterceptor");

            return target.toBytecode();
          }
        });
  }

  /**
   * Pinpoint profiler agent uses this detector to find out the service type of current application.
   */
  private void addApplicationTypeDetector(ProfilerPluginSetupContext context) {
    context.addApplicationTypeDetector(new SalukiProviderDetector());
  }

  @Override
  public void setTransformTemplate(TransformTemplate transformTemplate) {
    this.transformTemplate = transformTemplate;
  }

}
