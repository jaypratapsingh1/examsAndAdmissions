package com.tarento.upsmf.examsAndAdmissions.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Configuration {

    @Value("${notify.service.host}")
    private String notifyServiceHost;

    @Value("${notify.service.path}")
    private String notifyServicePath;

    @Value("${hub.notification.rootOrg}")
    private String hubRootOrg;

    @Value("${kafka.topics.workflow.notification}")
    private String workFlowNotificationTopic;
    @Value("${notification.sender.mail}")
    private String senderMail;
    @Value("${notification.email.body}")
    private String mailBody;

    public String getMailBody() {
        return mailBody;
    }


    public String getNotifyServiceHost() {
        return notifyServiceHost;
    }

    public void setNotifyServiceHost(String notifyServiceHost) {
        this.notifyServiceHost = notifyServiceHost;
    }

    public String getNotifyServicePath() {
        return notifyServicePath;
    }

    public void setNotifyServicePath(String notifyServicePath) {
        this.notifyServicePath = notifyServicePath;
    }

    public String getHubRootOrg() {
        return hubRootOrg;
    }

    public void setHubRootOrg(String hubRootOrg) {
        this.hubRootOrg = hubRootOrg;
    }

    public String getWorkFlowNotificationTopic() {
        return workFlowNotificationTopic;
    }

    public void setWorkFlowNotificationTopic(String workFlowNotificationTopic) {
        this.workFlowNotificationTopic = workFlowNotificationTopic;
    }
    public String getSenderMail() {
        return senderMail;
    }

    public void setSenderMail(String senderMail) {
        this.senderMail = senderMail;
    }

}
