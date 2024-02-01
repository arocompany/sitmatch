package com.nex.requestSerpApiLog;

import com.nex.common.CommonCode;
import com.nex.search.entity.RequestSerpApiLogEntity;
import com.nex.search.repo.RequestSerpApiLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestSerpApiLogService {
    private final RequestSerpApiLogRepository requestSerpApiLogRepository;
    public RequestSerpApiLogEntity init(Integer tsiUno, String url, String nationCode, String engine, String keyword, Integer pageNo, String apiToken, String imageUrl){
        RequestSerpApiLogEntity param = new RequestSerpApiLogEntity();
        param.setTsiUno(tsiUno);
        param.setRslUrl(url);
        param.setRslNation(nationCode);
        param.setRslEngine(engine);
        param.setRslKeyword(keyword);
        param.setRslPageNo(pageNo);
        param.setRslApiToken(apiToken);
        param.setRslImageUrl(imageUrl);

        param.setRslStatus(CommonCode.RequestSerpApiLogInit);
        param.setRslDtInsert(Timestamp.valueOf(LocalDateTime.now()));
        return param;
    }

    public RequestSerpApiLogEntity success(RequestSerpApiLogEntity param, String result){
        param.setRslResult(result);
        param.setRslStatus(CommonCode.RequestSerpApiLogSuccess);
        param.setRslDtUpdate(Timestamp.valueOf(LocalDateTime.now()));
        return param;
    }

    public RequestSerpApiLogEntity fail(RequestSerpApiLogEntity param, String failReason){
        param.setRslFailReason(failReason);
        param.setRslStatus(CommonCode.RequestSerpApiLogFail);
        param.setRslDtUpdate(Timestamp.valueOf(LocalDateTime.now()));
        return param;
    }
    public RequestSerpApiLogEntity select(int uno){
        return requestSerpApiLogRepository.findById(uno);
    }

    public void save(RequestSerpApiLogEntity param){
        try{
            requestSerpApiLogRepository.save(param);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
