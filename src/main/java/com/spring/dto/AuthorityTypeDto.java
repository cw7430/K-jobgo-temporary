package com.spring.dto;

import com.spring.entity.AuthorityType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthorityTypeDto {
    private int authorityId;
    private String authorityName;

    public AuthorityTypeDto(AuthorityType authorityType) {
        this.authorityId = authorityType.getAuthorityId();
        this.authorityName = authorityType.getAuthorityName();
    }
}
