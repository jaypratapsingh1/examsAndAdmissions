package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentRedirectRequest implements Serializable {
    
    private String endpoint;
    private String returnUrl;
    private String paymode;
    private String secret;
    private String merchantId;
    private MandatoryFields mandatoryFields;
    private String optionalFields;

}
