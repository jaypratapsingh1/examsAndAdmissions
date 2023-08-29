package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRedirectResponse implements Serializable {

    private String redirectUrl;
    private String referenceNo;
    private Debug debug;

    private class Debug {
        private String receivedPayload;
        private String pipedMandatoryParams;
        private String plainQueryString;
        private String encryptedQueryString;

        public Debug() {
        }

        public String getReceivedPayload() {
            return receivedPayload;
        }

        public void setReceivedPayload(String receivedPayload) {
            this.receivedPayload = receivedPayload;
        }

        public String getPipedMandatoryParams() {
            return pipedMandatoryParams;
        }

        public void setPipedMandatoryParams(String pipedMandatoryParams) {
            this.pipedMandatoryParams = pipedMandatoryParams;
        }

        public String getPlainQueryString() {
            return plainQueryString;
        }

        public void setPlainQueryString(String plainQueryString) {
            this.plainQueryString = plainQueryString;
        }

        public String getEncryptedQueryString() {
            return encryptedQueryString;
        }

        public void setEncryptedQueryString(String encryptedQueryString) {
            this.encryptedQueryString = encryptedQueryString;
        }
    }
}
