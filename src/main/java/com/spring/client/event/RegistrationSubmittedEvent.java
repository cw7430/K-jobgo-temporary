package com.spring.client.event;

import com.spring.client.dto.request.JoinRequestDTO;

public record RegistrationSubmittedEvent(    
		Long cmpId, 
	    String email, 
	    JoinRequestDTO payload
	    ) {}
