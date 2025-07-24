package com.spring.page.dto;

import com.spring.dto.response.ProfileAdminResponseDto;
import com.spring.profile.masked.dto.MaskedProfileDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProfilePage {

    private int currentPage;
    private int totalPages;
    private int pageSize;
    private int totalItems;
    private int startPage;
    private int endPage;

    private List<ProfileAdminResponseDto> profiles;
    private List<MaskedProfileDto> maskedProfiles;

    public ProfilePage(int currentPage, int totalItems, int pageSize, List<ProfileAdminResponseDto> profiles) {
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.profiles = profiles;
        calculatePagination();
    }

    public ProfilePage(int currentPage, int totalItems, int pageSize, List<MaskedProfileDto> maskedProfiles, boolean isMasked) {
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.maskedProfiles = maskedProfiles;
        calculatePagination();
    }

    public void setProfileList(List<MaskedProfileDto> maskedList) {
        this.maskedProfiles = maskedList;
    }

    public List<MaskedProfileDto> getProfileList() {
        return this.maskedProfiles;
    }

    private void calculatePagination() {
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int pageBlock = 10;
        this.startPage = (currentPage - 1) / pageBlock * pageBlock + 1;
        this.endPage = Math.min(startPage + pageBlock - 1, totalPages);
    }
}
