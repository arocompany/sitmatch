package com.nex.nations.service;

import com.nex.nations.entity.NationCodeEntity;
import com.nex.nations.repository.NationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleState;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NationService {
    private final NationCodeRepository nationCodeRepository;

    public List<NationCodeEntity> nationList(){
        return nationCodeRepository.findAll();
    }

    public NationCodeEntity nationUpdate(int ncUno, int ncIsActive){
        return nationCodeRepository.nationUpdate(ncUno, ncIsActive);
    }
}
