package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDto {

    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private UserRepresentation userRepresentation;
    List<RoleRepresentation> roleRepresentationList;
}
