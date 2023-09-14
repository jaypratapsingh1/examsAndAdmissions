package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@RedisHash("User")
public class User {

    private String id;

    private String createdTimestamp;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private boolean emailVerified;

    private int status;

    private String phoneNumber;

    private List<String> roles;

    private List<String> departments;

    private Map<String, List<String>> attributes;

    private boolean enabled;

    private boolean totp;

}