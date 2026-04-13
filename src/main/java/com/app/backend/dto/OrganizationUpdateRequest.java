package com.app.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class OrganizationUpdateRequest {

    @Size(max = 64)
    private String orgCode;

    @NotBlank
    @Size(max = 128)
    private String orgName;

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}
