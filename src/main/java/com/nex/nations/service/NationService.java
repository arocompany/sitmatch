package com.nex.nations.service;

import com.nex.nations.entity.NationCodeEntity;
import com.nex.nations.repository.NationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public String nationUpdate(int ncUno, int ncIsActive){
        try{
            int data = nationCodeRepository.nationUpdate(ncUno, ncIsActive);

            log.info("result == {}", data);

            if(data > 0){
                return "success";
            }
        }catch(Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return "fail";
    }


    public String nationAllUpdate(int ncIsActive){
        try{
            int data = nationCodeRepository.nationAllUpdate(ncIsActive);

            log.info("result == {}", data);

            if(data > 0){
                return "success";
            }
        }catch(Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return "fail";
    }
}
