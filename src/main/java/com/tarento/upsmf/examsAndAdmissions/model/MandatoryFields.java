package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MandatoryFields implements Serializable {

    private String referenceNo;
    private String submerchantId;
    private String transactionAmount;
    private String invoiceId;
    private String invoiceDate;
    private String invoiceTime;
    private String merchantId;
    private String payerType;
    private String payerId;
    private String transactionId;
    private String transactionDate;
    private String transactionTime;
    private String transactionStatus;
    private String refundId;
    private String refundDate;
    private String refundTime;
    private String refundStatus;

}
