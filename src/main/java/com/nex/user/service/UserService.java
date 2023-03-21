package com.nex.user.service;

import com.nex.common.EncryptUtil;
import com.nex.user.entity.AutoEntity;
import com.nex.user.entity.AutoKeywordInterface;
import com.nex.user.entity.UserEntity;
import com.nex.user.repo.AutoRepository;
import com.nex.user.repo.UserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AutoRepository autoRepository;
    private final EncryptUtil encryptUtil = new EncryptUtil();

    public String memberSignUp(UserEntity userInfo) {
        if(userRepository.countByUserId(userInfo.getUserId()) > 0) { // 아이디 중복 여부 확인
            return "중복된 아이디 입니다.";
        }
        if(userRepository.countByEmail(userInfo.getEmailId(), userInfo.getEmailDomain()) > 0) { // 이메일 중복 여부 확인
            return "중복된 이메일 입니다.";
        }
        if(userRepository.countByPhoneNum(userInfo.getPhoneNum1(), userInfo.getPhoneNum2(), userInfo.getPhoneNum3()) > 0) { // 핸드폰 중복 여부 확인
            return "중복된 핸드폰 번호 입니다.";
        }
        final UserEntity user = UserEntity.builder()
                .userId(userInfo.getUserId())
                .userPw(encryptUtil.getEncrypt(userInfo.getUserPw(), encryptUtil.getSalt()))
                .userNm(userInfo.getUserNm())
                .orgNm(userInfo.getOrgNm())
                .userPosition(userInfo.getUserPosition())
                .emailId(userInfo.getEmailId())
                .emailDomain(userInfo.getEmailDomain())
                .phoneNum1(userInfo.getPhoneNum1())
                .phoneNum2(userInfo.getPhoneNum2())
                .phoneNum3(userInfo.getPhoneNum3())
                .bizDetail(userInfo.getBizDetail())
                .useYn("Y")
                .userClfCd("10")
                .pwModifyDt(Timestamp.valueOf(LocalDateTime.now()))
                .lstLoginDt(Timestamp.valueOf(LocalDateTime.now()))
                .fstDmlDt(Timestamp.valueOf(LocalDateTime.now()))
                .lstDmlDt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        userRepository.save(user);
        return "success";
    }

    public String modifyUserInfo(UserEntity userInfo) {
        UserEntity user = userRepository.findByUserUno(userInfo.getUserUno());
//            if(StringUtils.isNotBlank(userInfo.getUserId())) user.setUserId((userInfo.getUserId()));      // 아이디 변경 불가
        if(StringUtils.isNotBlank(userInfo.getUserPw())) {
            user.setUserPw(encryptUtil.getEncrypt(userInfo.getUserPw(), encryptUtil.getSalt()));
            user.setPwModifyDt((Timestamp.valueOf(LocalDateTime.now())));                                               // 비밀번호 변경 시간 업데이트
        }
        if(StringUtils.isNotBlank(userInfo.getUserNm())) user.setUserNm((userInfo.getUserNm()));
        if(StringUtils.isNotBlank(userInfo.getOrgNm())) user.setOrgNm((userInfo.getOrgNm()));
        if(StringUtils.isNotBlank(userInfo.getUserPosition())) user.setUserPosition((userInfo.getUserPosition()));
        if(StringUtils.isNotBlank(userInfo.getEmailId())) user.setEmailId((userInfo.getEmailId()));
        if(StringUtils.isNotBlank(userInfo.getEmailDomain())) user.setEmailDomain((userInfo.getEmailDomain()));
        if(StringUtils.isNotBlank(userInfo.getPhoneNum1())) user.setPhoneNum1((userInfo.getPhoneNum1()));
        if(StringUtils.isNotBlank(userInfo.getPhoneNum2())) user.setPhoneNum2((userInfo.getPhoneNum2()));
        if(StringUtils.isNotBlank(userInfo.getPhoneNum3())) user.setPhoneNum3((userInfo.getPhoneNum3()));
        if(StringUtils.isNotBlank(userInfo.getBizDetail())) user.setBizDetail((userInfo.getBizDetail()));
        user.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));

        userRepository.save(user);

        return "Success";
    }

    public UserEntity deleteCounselor(Long userUno) {
        UserEntity user = userRepository.findByUserUno(userUno);

        user.setUserId("-");
        user.setUserPw("-");
        user.setUserNm("-");
        user.setOrgNm("-");
        user.setUserPosition("-");
        user.setEmailId("-");
        user.setEmailDomain("-");
        user.setPhoneNum1("-");
        user.setPhoneNum2("-");
        user.setPhoneNum3("-");
        user.setBizDetail("-");
        user.setUserClfCd("-");
        user.setUseYn("N");
        user.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));

        return userRepository.save(user);
    }

    public String modifyPassword(String userId, String userPw) {
        UserEntity user = userRepository.findByUserId(userId);
        user.setUserPw(userPw);
        modifyUserInfo(user);

        return "Success";
    }

    public UserEntity getUserInfoByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public Map<String, Object> getAutoKeyword(String auto_user_id){
        Map<String,Object> outMap = new HashMap<>();
        List<AutoKeywordInterface> autoKeyword_list = autoRepository.auto_list_select(auto_user_id);

        outMap.put("autoKeyword_list", autoKeyword_list);

        return outMap;
    }
}
