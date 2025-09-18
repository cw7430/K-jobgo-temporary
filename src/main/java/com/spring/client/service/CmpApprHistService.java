package com.spring.client.service;

public interface CmpApprHistService {
	void updateLatestRejectComment(Long cmpId, String newReason, String adminName);
}
