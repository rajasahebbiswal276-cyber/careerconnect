package com.careerconnect.dto;

import com.careerconnect.entity.Company;

public record CompanyResponse(Long id, String name, String description, String website, String location,
                              boolean approved, String ownerEmail) {

    public static CompanyResponse from(Company c) {
        return new CompanyResponse(c.getId(), c.getName(), c.getDescription(), c.getWebsite(),
                c.getLocation(), c.isApproved(), c.getOwner().getEmail());
    }
}
