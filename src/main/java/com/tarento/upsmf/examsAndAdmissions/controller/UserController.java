package com.tarento.upsmf.examsAndAdmissions.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.upsmf.examsAndAdmissions.model.Department;
import com.tarento.upsmf.examsAndAdmissions.model.User;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CreateUserDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.UserDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.UserResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.impl.IntegrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
@RequestMapping("/api/v1/user")
public class UserController {


    @Autowired
    private IntegrationServiceImpl integrationService;

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody JsonNode body) {
        try {
            ResponseEntity<String> userDto = integrationService.login(body);
            return userDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity createUser(@RequestBody CreateUserDto userRequest) {
        try {
            ResponseEntity<User> user =  integrationService.createUser(userRequest);
            if(user.getStatusCode() == HttpStatus.OK) {
                return createUserResponse(user.getBody());
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @GetMapping("/info")
    public ResponseEntity getUsersById(@RequestParam("id") String id) throws RuntimeException{
        try {
            return integrationService.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @GetMapping("/roles")
    public ResponseEntity getRolesById(@RequestParam("id") String id) throws RuntimeException{
        try {
            return integrationService.getRolesById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    private ResponseEntity<UserResponseDto> createUserResponse(User body) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("role", body.getRoles());
        attributes.put("departmentName", body.getDepartments());
        attributes.put("phoneNumber", Arrays.asList(body.getPhoneNumber()));
        boolean enabled = false;
        if(body.getStatus() == 1) {
            enabled = true;
        }
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .email(body.getEmail())
                .emailVerified(body.isEmailVerified())
                .enabled(enabled)
                .firstName(body.getFirstName())
                .lastName(body.getLastName())
                .id(body.getId())
                .keycloakId(body.getId())
                .username(body.getUsername())
                .attributes(attributes)
                .build();
        return ResponseEntity.ok().body(userResponseDto);
    }

}
