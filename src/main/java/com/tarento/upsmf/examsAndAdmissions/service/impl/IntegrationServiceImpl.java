package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.upsmf.examsAndAdmissions.exception.InvalidRequestException;
import com.tarento.upsmf.examsAndAdmissions.model.Department;
import com.tarento.upsmf.examsAndAdmissions.model.User;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CreateUserDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.UserCredentials;
import com.tarento.upsmf.examsAndAdmissions.model.dto.UserResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.IntegrationService;
import com.tarento.upsmf.examsAndAdmissions.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class IntegrationServiceImpl implements IntegrationService {

    public static final String ROLE = "role";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.user.searchUrl}")
    private String apiUrl;

    @Value("${api.user.listUrl}")
    private String listUserUrl;

    @Value("${api.user.loginUserUrl}")
    private String loginUserUrl;

    @Value("${api.user.createUrl}")
    private String createUserUrl;

    @Value("${api.user.details}")
    private String userInfoUrl;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JavaMailSender javaMailSender;


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

    @Override
    public ResponseEntity getRolesById(String id) {
        try {
            List<String> roles = redisUtil.getRolesByUserId(id);
            ObjectNode objectNode = mapper.createObjectNode();
            if (roles != null && !roles.isEmpty()) {
                objectNode.put("response", mapper.writeValueAsString(roles));
                return ResponseEntity.ok().body(mapper.writeValueAsString(objectNode));
            }
            objectNode.put("response", mapper.writeValueAsString(Collections.EMPTY_LIST));
            return ResponseEntity.ok().body(objectNode);
        } catch (Exception e) {
            log.error("Error in processing request", e);
            return ResponseEntity.internalServerError().body("Unable to get roles.");
        }
    }

    @Override
    public ResponseEntity<User> createUser(CreateUserDto user) throws Exception {
        // check for department
        String module = user.getAttributes().get("module");
        if (module != null) {
            user.getAttributes().put("module", module);
        } else {
            user.getAttributes().put("module", "exams");
        }
        String departmentName = user.getAttributes().get("departmentName");
        List<Department> departmentList = new ArrayList<>();
        if (departmentName != null) {
            departmentList = Department.getById(Integer.valueOf(departmentName));
            if (departmentList != null && !departmentList.isEmpty()) {
                user.getAttributes().put("departmentName", departmentList.get(0).getCode());
            }
        }
        String generatePassword = validateAndCreateDefaultPassword(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JsonNode jsonNodeObject = mapper.convertValue(user, JsonNode.class);
        JsonNode root = mapper.createObjectNode();
        ((ObjectNode) root).put("request", jsonNodeObject);
        log.info("Create user Request - {}", root);
        ResponseEntity response = restTemplate.exchange(createUserUrl, HttpMethod.POST,
                new HttpEntity<>(root, headers), String.class);
        log.info("Create user Response - {}", response);
        if (response.getStatusCode() == HttpStatus.OK) {
            String userContent = response.getBody().toString();
            JsonNode responseNode = null;
            try {
                responseNode = mapper.readTree(userContent);
            } catch (JsonParseException jp) {
                log.error("Error while parsing success response", jp);
            }
            if (responseNode != null) {
                if (responseNode.has("errorMessage")) {
                    throw new RuntimeException(responseNode.get("errorMessage").textValue());
                }
            }
            ObjectNode requestNode = mapper.createObjectNode();
            requestNode.put("userName", userContent);
            JsonNode payload = requestNode;
            JsonNode payloadRoot = mapper.createObjectNode();
            ((ObjectNode) payloadRoot).put("request", payload);
            ResponseEntity<String> getUsersResponse = searchUsers(payloadRoot);
            if (getUsersResponse.getStatusCode() == HttpStatus.OK) {
                String getUsersResponseBody = getUsersResponse.getBody();
                JsonNode getUsersJsonNode = mapper.readTree(getUsersResponseBody);
                if (getUsersJsonNode.size() > 0) {
                    JsonNode userContentData = getUsersJsonNode;
                    User newUser = createUserWithApiResponse(userContentData);
                    // todo send mail
                    sendMail(newUser, generatePassword);
                    return new ResponseEntity<>(newUser, HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                // Handle error cases here
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendMail(User newUser, String generatePassword) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Dear ").append(newUser.getFirstName()).append(", We are writing to inform you that your account has been created by the administrator. Please use following email and Password to login. \\n Email:")
                    .append(newUser.getEmail()).append("  Password: ").append(generatePassword).append(" Kindly do not share the credentials with anyone. Regards UPSMF Team");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setText(stringBuilder.toString());
            message.setSubject("Account Login Credentials");
            message.setTo(newUser.getEmail());
            message.setFrom("upsmf.otp@upsmfac.org");
            javaMailSender.send(message);
        } catch (Exception e) {
            log.info("Error in send email");
        }
    }

    private String validateAndCreateDefaultPassword(CreateUserDto user) {
        if (user != null) {
            if (user.getCredentials() != null && !user.getCredentials().isEmpty()) {
                boolean autoCreate = true;
                String existingPassword = null;
                for (UserCredentials credentials : user.getCredentials()) {
                    if (credentials.getType() != null && credentials.getType().equalsIgnoreCase("password")) {
                        if (credentials.getValue() != null && !credentials.getValue().isBlank()) {
                            autoCreate = false;
                            existingPassword = credentials.getValue();
                        }
                    }
                }
                if (autoCreate) {
                    return generatePassword(user);
                }
                return existingPassword;
            } else {
                // generate random password and set in user
                return generatePassword(user);
            }
        }
        throw new RuntimeException("Error while generating password");
    }

    private String generatePassword(CreateUserDto user) {
        String randomPassword = generateRandomPassword();
        UserCredentials userCredential = UserCredentials.builder().type("password").value(randomPassword).temporary(false).build();
        user.setCredentials(Collections.singletonList(userCredential));
        return randomPassword;
    }

    private String generateRandomPassword() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        log.debug("random password - {}", generatedString);
        return generatedString;
    }

    private User createUserWithApiResponse(JsonNode userContent) throws Exception {
        List<String> rolesArray = new ArrayList<>();
        List<String> departmentArray = new ArrayList<>();

        JsonNode rolesNode = userContent.path("attributes").path(ROLE);
        JsonNode departmentNode = userContent.path("attributes").path("departmentName");
        if (rolesNode.isArray()) {
            for (int i = 0; i < rolesNode.size(); i++) {
                rolesArray.add(rolesNode.get(i).asText());
            }
        }

        if (userContent.path("attributes").has("departmentName") && departmentNode.isArray() && !departmentNode.isEmpty()) {
            for (int i = 0; i < departmentNode.size(); i++) {
                departmentArray.add(departmentNode.get(i).asText());
            }
        }

        return User.builder()
                .id(userContent.path("id").asText())
                .firstName(userContent.path("firstName").asText())
                .lastName(userContent.path("lastName").asText())
                .username(userContent.path("username").asText())
                .phoneNumber(userContent.path("attributes").path("phoneNumber").get(0).asText())
                .email(userContent.path("email").asText())
                .emailVerified(userContent.path("emailVerified").asBoolean())
                .status(userContent.path("enabled").asInt())
                .roles(rolesArray)
                .departments(departmentArray)
                .build();

    }
}
