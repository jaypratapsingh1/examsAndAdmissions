package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tarento.upsmf.examsAndAdmissions.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;



import java.util.HashMap;
import java.util.Map;

@Service
public class RequestServiceImpl {

	Logger log = LogManager.getLogger(RequestServiceImpl.class);

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Configuration configuration;

	/**
	 *
	 * @param uri
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Object fetchResultUsingPost(StringBuilder uri, Object request, Class objectType,HashMap<String, String> headersValue) {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		StringBuilder str = new StringBuilder(this.getClass().getCanonicalName()).append(".fetchResult:")
				.append(System.lineSeparator());
		str.append("URI: ").append(uri.toString()).append(System.lineSeparator());
		try {
			str.append("Request: ").append(mapper.writeValueAsString(request)).append(System.lineSeparator());
			String message = str.toString();
			log.info(message);
			HttpHeaders headers = new HttpHeaders();
			if (!ObjectUtils.isEmpty(headersValue)) {
				for (Map.Entry<String, String> map : headersValue.entrySet()) {
					headers.set(map.getKey(), map.getValue());
				}
			}
			headers.set(Constants.ROOT_ORG_CONSTANT, configuration.getHubRootOrg());
			HttpEntity<Object> entity = new HttpEntity<>(request, headers);
			response = restTemplate.postForObject(uri.toString(), entity, objectType);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
		} catch (Exception e) {
			log.error("Exception occured while calling the exteranl service: ", e);
		}
		return response;
	}
}
