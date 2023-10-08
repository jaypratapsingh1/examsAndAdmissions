package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Transaction {

    private String responseCode;
    private String uniqueRefNumber;
    private Double serviceTaxAmount;
    private Double processingFeeAmount;
    private Double totalAmount;
    private Double transactionAmount;
    private Date transactionDate;
    private String interchangeValue;
    private String tdr;
    private String paymentMode;
    private Integer subMerchantId;
    private String referenceNo;
    private Long entityId;
    private String rs;
    private String tps;
    private String mandatoryFields;
    private String optionalFields;
    private String rsv;
    private String module;
    private String transaction_status;

}
