package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "uploaded_files")
public class UploadedFile extends ResponseDto {

    @Id
    @Column(name = "identifier")
    private String identifier;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "date_created_on")
    private Timestamp dateCreatedOn;

    @Column(name = "status")
    private String status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created_by")
    private String createdBy;

    // Constructors, getters, setters, and other methods
    
    public UploadedFile() {
        // Generate a random identifier
        this.identifier = UUID.randomUUID().toString();
        
        // Set default status and created by values if needed
        this.status = Constants.INITIATED_CAPITAL;

        
        // Set current timestamp
        this.dateCreatedOn = new Timestamp(System.currentTimeMillis());
    }

}
