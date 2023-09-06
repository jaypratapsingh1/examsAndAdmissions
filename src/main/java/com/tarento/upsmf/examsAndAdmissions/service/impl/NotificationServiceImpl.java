package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.config.Configuration;
import com.tarento.upsmf.examsAndAdmissions.consumer.NotificationConsumer;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.InstituteList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tarento.upsmf.examsAndAdmissions.model.notification.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl {

	public static final String EMAILTEMPLATE = "emailtemplate";
	private static final String MAIL_SUBJECT = "Your request is #state";
	private static final String STATE_NAME_TAG = "#state";
	Logger logger = LogManager.getLogger(NotificationConsumer.class);

	@Autowired
	private Configuration configuration;

	@Autowired
	private RequestServiceImpl requestService;
	private static final String WORK_FLOW_EVENT_NAME = "workflow_service_notification";
	/**
	 * Send notification to the user based on state of application
	 *
	 * @param instituteList notification request
	 */

	public void sendEmailNotification(InstituteList instituteList) {
		logger.info("Notification status, {}", instituteList.isApprove());

		if (instituteList.isApprove()) {
			logger.info("Enters in the email notification block");
			List<String> emailList =instituteList.getInstitutes().stream().map(Institute::getEmail).collect(Collectors.toList());

			if (!emailList.isEmpty()) {
				HashMap<String, Object> params = new HashMap<>();
				NotificationRequest request = new NotificationRequest();
				request.setDeliveryType("message");
				request.setIds(new ArrayList<>(emailList));
				request.setMode("email");
				Template template = new Template();
				template.setId(EMAILTEMPLATE);
				String emailBody = configuration.getMailBody();
				params.put("body", emailBody);
				template.setParams(params);
				Config config = new Config();
				config.setSubject(MAIL_SUBJECT.replace(STATE_NAME_TAG, instituteList.getState()));
				config.setSender(configuration.getSenderMail());
				Map<String, Object> req = new HashMap<>();
				request.setTemplate(template);
				request.setConfig(config);
				Map<String, List<NotificationRequest>> notificationMap = new HashMap<>();
				notificationMap.put("notifications", Arrays.asList(request));
				req.put("request", notificationMap);
				sendNotification(req);
			} else {
				logger.warn("Email address not found in the update field values.");
			}
		}
	}
		/**
         * Post to the Notification service
         * @param request
         */
	public void sendNotification(Map<String, Object> request) {
		StringBuilder builder = new StringBuilder();
		builder.append(configuration.getNotifyServiceHost()).append(configuration.getNotifyServicePath());
		try {
			requestService.fetchResultUsingPost(builder, request, Map.class, null);
		} catch (Exception e) {
			logger.error("Exception while posting the data in notification service: ", e);
		}

	}
}
