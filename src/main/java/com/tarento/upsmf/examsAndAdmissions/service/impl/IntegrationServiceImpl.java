package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.upsmf.examsAndAdmissions.exception.InvalidRequestException;
import com.tarento.upsmf.examsAndAdmissions.model.User;
import com.tarento.upsmf.examsAndAdmissions.model.dto.UserCredentials;
import com.tarento.upsmf.examsAndAdmissions.model.dto.UserResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class IntegrationServiceImpl implements IntegrationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.user.searchUrl}")
    private String apiUrl;

    @Value("${api.user.listUrl}")
    private String listUserUrl;

    @Value("${api.user.loginUserUrl}")
    private String loginUserUrl;

    @Value("${api.user.details}")
    private String userInfoUrl;

    @Autowired
    private ObjectMapper mapper;


    private ResponseEntity<String> getUserDetailsFromKeycloak(ResponseEntity response, ObjectMapper mapper) throws Exception {
        String userContent = response.getBody().toString();
        // if error then error body will be sent
        if (userContent.startsWith("{")) {
            JsonNode createUserResponseNode = mapper.readTree(userContent);
            if (createUserResponseNode != null && createUserResponseNode.has("errorMessage")) {
                throw new RuntimeException("User exists with same username");
            }
        }
        ObjectNode requestNode = mapper.createObjectNode();
        requestNode.put("userName", userContent);
        JsonNode payload = requestNode;
        JsonNode payloadRoot = mapper.createObjectNode();
        ((ObjectNode) payloadRoot).put("request", payload);
        ResponseEntity<String> getUsersResponse = searchUsers(payloadRoot);
        return getUsersResponse;
    }

    @Override
    public ResponseEntity<String> getUsersFromKeycloak(JsonNode payload) throws Exception {

        List<UserResponseDto> childNodes = new ArrayList<>();
        int i = 0;
        ResponseEntity<String> response = restTemplate.exchange(
                listUserUrl,
                HttpMethod.POST,
                new HttpEntity<>(payload),
                String.class
        );
        if (response.getStatusCode() == HttpStatus.OK) {
            String getUsersResponseBody = response.getBody();
            ArrayNode getUsersJsonNode = (ArrayNode) mapper.readTree(getUsersResponseBody);
            if (getUsersJsonNode.size() > 0) {
                for (JsonNode node : getUsersJsonNode) {
                    if (node.path("attributes") != null && !node.path("attributes").isEmpty()
                            && node.path("attributes").path("module") != null && !node.path("attributes").path("module").isEmpty()
                            && node.get("attributes").path("module").get(0).textValue().equalsIgnoreCase("grievance")) {
                        log.info("Grievance user node found || {} -- {}", i++, node);
                        //User user = createUserWithApiResponse(node);
                        //childNodes.add(createUserResponse(user));
                    }
                }
            }
        }
        JsonNode userResponse = mapper.createObjectNode();
        ((ObjectNode) userResponse).put("count", i);
        ArrayNode nodes = mapper.valueToTree(childNodes);
        ((ObjectNode) userResponse).put("result", nodes);
        return ResponseEntity.ok(mapper.writeValueAsString(userResponse));
    }

    @Override
    public ResponseEntity<String> searchUsers(JsonNode payload) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(payload),
                String.class
        );
        return response;
    }

    @Override
    public ResponseEntity<String> login(JsonNode payload) throws Exception {
        String userName = payload.get("username").asText(null);
        String password = payload.get("password").asText(null);
        if(userName == null || password == null) {
            return ResponseEntity.badRequest().body("Invalid Request");
        }
        ObjectNode request = mapper.createObjectNode();
        ObjectNode root = mapper.createObjectNode();
        root.put("username", userName);
        root.put("password", password);
        request.put("request", root);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<String> response = restTemplate.exchange(
                    loginUserUrl, HttpMethod.POST,
                    new HttpEntity<>(root, headers), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseBody = mapper.readTree(response.getBody());
                if(responseBody.has("userRepresentation")) {
                    User user = mapper.treeToValue(responseBody.get("userRepresentation"), User.class);
                }
                return ResponseEntity.ok().body(response.getBody());
            }
            throw new RuntimeException("Error in logging user.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in logging user.");
        }
    }

    @Override
    public ResponseEntity<String> getUserById(String id) throws Exception {
        if(id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid Request");
        }
        ObjectNode request = mapper.createObjectNode();
        ObjectNode root = mapper.createObjectNode();
        root.put("userName", id);
        request.put("request", root);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.POST,
                    new HttpEntity<>(root, headers), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseBody = mapper.readTree(response.getBody());
                User user = mapper.treeToValue(responseBody, User.class);
                return ResponseEntity.ok().body(response.getBody());
            }
            throw new RuntimeException("Error in geting user info.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in geting user info.");
        }
    }


    /**
     * API to change password
     * sample body -
     * {
     * "credentials": [
     * {
     * "type": "password",
     * "value": "ka09eF$299",
     * "temporary": "false"
     * }
     * ]
     * }
     * }
     *
     * @param userCredentials
     */
    public void changePassword(UserCredentials userCredentials) {
        // validate Request
        validateChangePasswordRequest(userCredentials);
    }

    private void validateChangePasswordRequest(UserCredentials userCredentials) {
        if (userCredentials == null) {
            throw new InvalidRequestException("Invalid Request");
        }

    }
}
