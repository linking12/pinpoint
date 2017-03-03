/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.web.alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.aliyun.mns.client.AsyncCallback;
import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.BatchSendException;
import com.aliyun.mns.common.utils.ServiceSettings;
import com.aliyun.mns.model.ErrorMessageResult;
import com.aliyun.mns.model.Message;
import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;

/**
 * @author minwoo.jung
 */
public class AliyYunMessageSender implements AlarmMessageSender {

    private static final String ALIYUN_ALAM_MESSAGE_QUEUE = "alarm";

    private final Logger        logger                    = LoggerFactory.getLogger(this.getClass());

    private CloudQueue          cloudQueue;

    @Autowired
    private Properties          properties;

    @PostConstruct
    public void init() {
        String accessKeyId = properties.getProperty("mns.accesskeyid");
        String accessKeySecret = properties.getProperty("mns.accesskeysecret");
        String accountEndpoint = properties.getProperty("mns.accountendpoint");
        ServiceSettings.setMNSAccessKeyId(accessKeyId);
        ServiceSettings.setMNSAccessKeySecret(accessKeySecret);
        ServiceSettings.setMNSAccountEndpoint(accountEndpoint);
        CloudAccount account = new CloudAccount(ServiceSettings.getMNSAccessKeyId(),
                                                ServiceSettings.getMNSAccessKeySecret(),
                                                ServiceSettings.getMNSAccountEndpoint());
        MNSClient client = account.getMNSClient();
        cloudQueue = client.getQueueRef(ALIYUN_ALAM_MESSAGE_QUEUE);
    }

    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {
        sendAlam2Aliyun(checker, sequenceCount);
    }

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        sendAlam2Aliyun(checker, sequenceCount);
    }

    private void sendAlam2Aliyun(AlarmChecker checker, int sequenceCount) {
        List<Message> asyncAliyunMsgs = new ArrayList<Message>();
        for (String message : checker.getSmsMessage()) {
            Message aliyunMessage = new Message();
            aliyunMessage.setMessageBody(message);
            asyncAliyunMsgs.add(aliyunMessage);
        }
        cloudQueue.asyncBatchPutMessage(asyncAliyunMsgs, new AsyncCallback<List<Message>>() {

            @Override
            public void onSuccess(List<Message> result) {
                for (Message putMsg : result) {
                    logger.info("PutMessage has MsgId:" + putMsg.getMessageId());
                }
            }

            @Override
            public void onFail(Exception e) {
                if (e instanceof BatchSendException) {
                    List<Message> messages = ((BatchSendException) e).getMessages();
                    for (Message msg : messages) {
                        if (msg.isErrorMessage()) {
                            ErrorMessageResult errorMessageDetail = msg.getErrorMessageDetail();
                            logger.error("PutMessage Fail." + " ErrorCode: " + errorMessageDetail.getErrorCode()
                                         + " ErrorMessage: " + errorMessageDetail.getErrorMessage());
                        }
                    }
                } else {
                    logger.error(e.getMessage(), e);
                }
            }

        });
    }

}
