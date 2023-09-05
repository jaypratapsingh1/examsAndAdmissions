package com.tarento.upsmf.examsAndAdmissions.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.model.InstituteList;
import com.tarento.upsmf.examsAndAdmissions.service.impl.NotificationServiceImpl;
import lombok.extern.slf4j.Slf4j; // Import Lombok's @Slf4j annotation
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private NotificationServiceImpl notificationService;

    @KafkaListener(groupId = "workflowNotificationTopic-consumer", topics = "${kafka.topics.workflow.notification}")
    public void processMessage(ConsumerRecord<String, String> data) {
        InstituteList wfRequest = null;
        try {
            String message = String.valueOf(data.value());
            wfRequest = mapper.readValue(message, InstituteList.class);
            log.info("Received data in notification consumer : {}", mapper.writeValueAsString(wfRequest));
            notificationService.sendEmailNotification(wfRequest);
        } catch (Exception ex) {
            log.error("Error while deserialization the object value", ex);
        }
    }
}
