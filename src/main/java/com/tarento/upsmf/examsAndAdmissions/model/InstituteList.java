package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstituteList {

    @Getter
    private List<Institute> institutes;
    private boolean approve; // Approve flag

    public InstituteList() {
        this.institutes = new ArrayList<>();
        this.approve = false; // Initialize to not approved
    }

    public void addInstitute(Institute institute) {
        institutes.add(institute);
    }

    public boolean isApprove() {
        return true;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

    public CharSequence getState() {
        if (approve) {
            return "approve";
        } else {
            return "reject";
        }
    }
}