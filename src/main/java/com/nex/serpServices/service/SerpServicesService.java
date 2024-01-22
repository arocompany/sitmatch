package com.nex.serpServices.service;

import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.repo.SerpServicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SerpServicesService {
    private final SerpServicesRepository serpServicesRepository;

    public List<SerpServicesEntity> serpServicesList() {
        return serpServicesRepository.findAll(Sort.by(Sort.Direction.ASC,"ss_uno"));
    }
}
