package com.nex.serpServices.service;

import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.repo.SerpServicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SerpServicesService {
    private final SerpServicesRepository serpServicesRepository;

    public List<SerpServicesEntity> serpServicesList() {
        return serpServicesRepository.findAll();
    }

    public List<SerpServicesEntity> serpServicesIsSsActiveList(int ssActive) {
        return serpServicesRepository.findBySsIsActive(ssActive);
    }

    public String serviceCodeUpdate(int ssUno, int ssIsActive) {
        try {
            int data = serpServicesRepository.serviceCodeUpdate(ssUno, ssIsActive);

            if(data > 0){
                return "success";
            }

        } catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }

        return "fail";
    }



}
