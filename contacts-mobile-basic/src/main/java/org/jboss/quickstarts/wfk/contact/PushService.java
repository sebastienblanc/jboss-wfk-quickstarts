/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.contact;

import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PushService {

    private static final Logger logger = Logger.getLogger(PushService.class.getName());

    private JavaSender javaSender;

    public PushService() {
        javaSender = new SenderClient("https://quickstartsups-sblanc.rhcloud.com");
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public void sendMessage(PushNotification pushNotification) {
        UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
                .pushApplicationId("ce517535-be7d-4c12-8668-34783009cc83")
                .masterSecret("246739a2-0050-4cd3-9383-c572d25161bf")
                .aliases(Arrays.asList(pushNotification.getReceiver()))
                .alert(pushNotification.getMessage() + " From " + pushNotification.getAuthor())
                .build();

        javaSender.send(unifiedMessage,new PushServiceMessageResponseCallback());

    }

    /**
     * Simple, stateless innerclass, that implements logger for the callbacks of the
     * MessageResponseCallback class.
     */
    private static class PushServiceMessageResponseCallback implements MessageResponseCallback {
        @Override
        public void onComplete(int statusCode) {
            logger.info("Message submitted");
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(Level.SEVERE, "An error occurred", throwable);
        }

    }


}
