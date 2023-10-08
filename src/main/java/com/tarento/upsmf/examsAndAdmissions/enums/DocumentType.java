package com.tarento.upsmf.examsAndAdmissions.enums;

public enum DocumentType {
    HALL_TICKET, STUDENT_CERTIFICATE; // Add other document types as needed

    public String getFolderName() {
        switch(this) {
            case HALL_TICKET:
                return "hall_tickets";
            case STUDENT_CERTIFICATE:
                return "student_certificates";
            // Handle other document types accordingly
            default:
                throw new IllegalArgumentException("Unexpected document type: " + this);
        }
    }
}
