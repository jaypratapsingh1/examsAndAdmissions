package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "hall_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class HallTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String studentEnrollmentNumber;
    private String examRegistrationNumber;
    private Date dateOfBirth;
    private byte[] hallTicketData;  // Can store the actual hall ticket data or a link to it
}
