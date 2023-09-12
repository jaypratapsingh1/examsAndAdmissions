package com.tarento.upsmf.examsAndAdmissions.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.upsmf.examsAndAdmissions.model.dto.UserDto;
import com.tarento.upsmf.examsAndAdmissions.service.impl.IntegrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping("/info")
    public ResponseEntity getUsersById(@RequestParam("id") String id) throws RuntimeException{
        try {
            return integrationService.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

}
