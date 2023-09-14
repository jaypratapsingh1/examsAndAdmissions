package com.tarento.upsmf.examsAndAdmissions.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.upsmf.examsAndAdmissions.model.User;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CreateUserDto;
import org.springframework.http.ResponseEntity;

public interface IntegrationService {
    ResponseEntity<String> getUsersFromKeycloak(JsonNode payload) throws Exception;

    ResponseEntity<String> searchUsers(JsonNode payload) throws Exception;

    ResponseEntity<String> login(JsonNode body) throws Exception;

    ResponseEntity<String> getUserById(String id) throws Exception;

    ResponseEntity getRolesById(String id);

    ResponseEntity<User> createUser(CreateUserDto user) throws Exception;
}
