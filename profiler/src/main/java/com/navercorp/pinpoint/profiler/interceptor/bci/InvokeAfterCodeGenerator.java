/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.interceptor.bci;

import java.lang.reflect.Method;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;

/**
 * @author Jongho Moon
 *
 */
public class InvokeAfterCodeGenerator extends InvokeCodeGenerator {
    private final int interceptorId;
    private final Method interceptorMethod;
    private final InstrumentClass targetClass;
    private final ExecutionPolicy policy;
    private final boolean hasBefore;
    private final boolean catchClause;
    
    public InvokeAfterCodeGenerator(int interceptorId, Class<?> interceptorClass, Method interceptorMethod, InstrumentClass targetClass, InstrumentMethod targetMethod, ExecutionPolicy policy, boolean hasBefore, boolean catchCluase) {
        super(interceptorId, interceptorClass, targetMethod, policy);
        
        this.interceptorId = interceptorId;
        this.interceptorMethod = interceptorMethod;
        this.targetClass = targetClass;
        this.policy = policy;
        this.hasBefore = hasBefore;
        this.catchClause = catchCluase;
    }

    public String generate() {
        CodeBuilder builder = new CodeBuilder();
        
        builder.begin();

        // try {
        //     if (_$PINPOINT$_groupInvocation13 != null) {
        //         (($INTERCEPTOR_TYPE)_$PINPOINT$_holder13.getInterceptor.before($ARGUMENTS);
        //         _$PINPOINT$_groupInvocation13.leave(ExecutionPolicy.POLICY);
        //     } else {
        //         InterceptorInvokerHelper.logSkipByExecutionPolicy(_$PINPOINT$_holder13, ExecutionPolicy.POLICY);
        //     }
        // } catch (Throwable t) {
        //     InterceptorInvokerHelper.handleException(t);
        // }
        //
        // throw e;
        
        builder.append("try { ");

        if (!hasBefore) {
            builder.format("%1$s = %2$s.findInterceptor(%3$d); ", getInterceptorInstanceVar(), getInterceptorRegistryClassName(), interceptorId);
        } else if (policy != null) {
            builder.format("if (%1$s.canLeave(%2$s)) { ", getInterceptorGroupInvocationVar(), getExecutionPolicy());
        }
        
        if (interceptorMethod != null) {
            builder.format("((%1$s)%2$s.getInterceptor()).after(", getInterceptorType(), getInterceptorInstanceVar());
            appendArguments(builder);
            builder.format(");");
        }
        
        if (policy != null) {
            builder.format(" %1$s.leave(%2$s);", getInterceptorGroupInvocationVar(), getExecutionPolicy());
            builder.format(" } else { %1$s.logSkipAfterByExecutionPolicy(%2$s, %3$s, %4$s); }", getInterceptorInvokerHelperClassName(), getInterceptorInstanceVar(), getInterceptorGroupInvocationVar(), getExecutionPolicy());
        }
        
        builder.format("} catch (java.lang.Throwable _$PINPOINT_EXCEPTION$_) { %1$s.handleException(_$PINPOINT_EXCEPTION$_); }", getInterceptorInvokerHelperClassName());
        
        if (catchClause) {
            builder.append(" throw $e;");
        }
        
        builder.end();
        
        return builder.toString();
        
    }

    private String getReturnValue() {
        if (catchClause) {
            return "null";
        }
        
        if (!targetMethod.isConstructor()) {
            if ("void".equals(targetMethod.getReturnType())) {
                return "null";
            }
        }

        return "($w)$_";
    }
        
    private String getException() {
        if (catchClause) {
            return "$e";
        }
        
        return "null";
    }

    private void appendArguments(CodeBuilder builder) {
        switch (type) {
        case SIMPLE:
            appendSimpleAfterArguments(builder);
            break;
        case STATIC:
            appendStaticAfterArguments(builder);
            break;
        case CUSTOM:
            appendCustomAfterArguments(builder);
            break;
        }
    }

    private void appendSimpleAfterArguments(CodeBuilder builder) {
        builder.format("%1$s, %2$s, %3$s, %4$s", getTarget(), getArguments(), getReturnValue(), getException());
    }

    private void appendStaticAfterArguments(CodeBuilder builder) {
        builder.format("%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s, %7$s", getTarget(), targetClass.getName(), targetMethod.getName(), getParameterTypes(), getArguments(), getReturnValue(), getException());
    }
    
    private void appendCustomAfterArguments(CodeBuilder builder) {
        Class<?>[] paramTypes = interceptorMethod.getParameterTypes();
        
        if (paramTypes.length == 0) {
            return;
        }
        
        builder.append(getTarget());
        
        if (paramTypes.length >= 2) {
            builder.append(", ");
            builder.append(getReturnValue());
        }
        
        if (paramTypes.length >= 3) {
            builder.append(", ");
            builder.append(getException());
        }
        
        int i = 0;
        int argNum = targetMethod.getParameterTypes().length;
        int interceptorArgNum = paramTypes.length - 3;
        int matchNum = Math.min(argNum, interceptorArgNum);
        
        for (; i < matchNum; i++) {
            builder.append(", $" + (i + 1));
        }
        
        for (; i < interceptorArgNum; i++) {
            builder.append(", null");
        }
    }
}