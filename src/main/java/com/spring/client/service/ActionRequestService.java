package com.spring.client.service;

public interface ActionRequestService {
	// 사용자
	Long requestWithdraw(Long cmpId, String reason, String requestedBy);
	Long requestJobCancel(Long cmpId, Long jobId, String reason, String requestedBy);
	//관리자
	void approveWithdraw(Long reqId, String adminId, String note);    // cmp_info.is_del=1, del_dt=now
	void rejectWithdraw(Long reqId, String adminId, String note);
	void approveJobCancel(Long reqId, String adminId, String note);   // 해당 job 상태 CANCELLED, 취소로그 insert
	void rejectJobCancel(Long reqId, String adminId, String note);


}
