package com.tarento.upsmf.examsAndAdmissions.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface IntegrationService {
    ResponseEntity<String> getUsersFromKeycloak(JsonNode payload) throws Exception;

    ResponseEntity<String> searchUsers(JsonNode payload) throws Exception;

    ResponseEntity<String> login(JsonNode body) throws Exception;
}
