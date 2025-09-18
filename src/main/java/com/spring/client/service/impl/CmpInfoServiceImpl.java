package com.spring.client.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.service.CmpInfoService;

@Service
public class CmpInfoServiceImpl implements CmpInfoService {

    private final CmpInfoRepository cmpInfoRepository;

    @Autowired
    public CmpInfoServiceImpl(CmpInfoRepository cmpInfoRepository) {
        this.cmpInfoRepository = cmpInfoRepository;
    }

    @Override
    public boolean existsByBizEmail(String bizEmail) {
        return cmpInfoRepository.existsByBizEmail(bizEmail);
    }
}
