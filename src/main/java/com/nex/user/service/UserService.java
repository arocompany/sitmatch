package com.nex.user.service;

import com.nex.common.EncryptUtil;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.user.entity.*;
import com.nex.user.repo.AutoRepository;
import com.nex.user.repo.LoginHistRepository;
import com.nex.user.repo.UserRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AutoRepository autoRepository;
    private final LoginHistRepository loginHistRepository;
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
                .crawling_limit("0")
                .percent_limit(1)
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

    public void loginHistInsert(int userUno, String userId) {
        LoginHistEntity lhe = new LoginHistEntity();
        lhe.setUserUno(userUno);
        lhe.setUserId(userId);
        lhe.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        loginHistRepository.save(lhe);
    }

    public List<LoginHistDto> getUserHistory2(String fromDate, String toDate2) {
        return userRepository.userHistCntList(fromDate, toDate2);
    }

    public List<LoginExcelDto> userHistExcel(String fromDate, String toDate2) throws IOException {
        return userRepository.userHistExcel(fromDate, toDate2);
    }

    public void excelUserHistory(HttpServletResponse response,List<LoginExcelDto> loginExcelDtoList) throws IOException {

        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("사용자 접속 이력");
        Row row;
        Cell cell;
        int rowNum = 0;

        row=sheet.createRow(rowNum++);
        cell=row.createCell(0);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(2);
        cell.setCellValue("방문날짜");

        cell=row.createCell(3);
        cell.setCellValue("방문횟수");

        for(int i=0; i<loginExcelDtoList.size(); i++) {
            row=sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(loginExcelDtoList.get(i).getUserId());
            log.info("getUserId"+loginExcelDtoList.get(i).getUserId());

            cell = row.createCell(1);
            cell.setCellValue(loginExcelDtoList.get(i).getUserNm());
            log.info("getUserNm "+loginExcelDtoList.get(i).getUserNm());

            cell = row.createCell(2);
            cell.setCellValue(loginExcelDtoList.get(i).getDate());
            log.info("getDate"+loginExcelDtoList.get(i).getDate());

            cell = row.createCell(3);
            cell.setCellValue(loginExcelDtoList.get(i).getCnt());
            log.info("getCnt"+loginExcelDtoList.get(i).getCnt());

        }

        String fileName = "사용자 접속 현황";
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();
        // wb.close();

    }

    public void prcuseExcel(HttpServletResponse response
                            , List<SearchInfoExcelDto> searchInfoExcelDtoList
                            , List<TraceHistExcelDto> traceHistExcelDtoList
                            , List<SearchResultExcelDto> searchResultExcelDtoList
                            , List<noticeListExcelDto> noticeListExcelDtoList
                            , List<LoginExcelDto> loginExcelDtoList) throws IOException {
        
        log.info("prcuseExcel 진입");

        Workbook wb = new XSSFWorkbook();

        Sheet infoSheet = wb.createSheet("이력관리");

        Row row;
        Cell cell;

        int rowNum = 0;

        row = infoSheet.createRow(rowNum++);
        cell=row.createCell(0);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(1);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(2);
        cell.setCellValue("방문 날짜");

        cell=row.createCell(3);
        cell.setCellValue("방문 횟수");

        for(int i=0; i<searchInfoExcelDtoList.size(); i++) {
            row = infoSheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(searchInfoExcelDtoList.get(i).getUserNm());

            cell = row.createCell(1);
            cell.setCellValue(searchInfoExcelDtoList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(searchInfoExcelDtoList.get(i).getDate());

            cell = row.createCell(3);
            cell.setCellValue(searchInfoExcelDtoList.get(i).getCnt());
        }

        rowNum = 0;
        Sheet traceSheet = wb.createSheet("모니터링");
        row = traceSheet.createRow(rowNum++);

        cell =row.createCell(0);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(1);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(2);
        cell.setCellValue("방문 날짜");

        cell=row.createCell(3);
        cell.setCellValue("방문 횟수");

        for(int i=0; i<traceHistExcelDtoList.size(); i++) {
            row=traceSheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(traceHistExcelDtoList.get(i).getUserNm());

            cell = row.createCell(1);
            cell.setCellValue(traceHistExcelDtoList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(traceHistExcelDtoList.get(i).getDate());

            cell = row.createCell(3);
            cell.setCellValue(traceHistExcelDtoList.get(i).getCnt());
        }

        rowNum = 0;
        Sheet resultSheet = wb.createSheet("검색결과");
        row = resultSheet.createRow(rowNum++);

        cell =row.createCell(0);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(1);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(2);
        cell.setCellValue("방문 날짜");

        cell=row.createCell(3);
        cell.setCellValue("방문 횟수");

        for(int i=0; i<searchResultExcelDtoList.size(); i++) {
            row=resultSheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(searchResultExcelDtoList.get(i).getUserNm());

            cell = row.createCell(1);
            cell.setCellValue(searchResultExcelDtoList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(searchResultExcelDtoList.get(i).getDate());

            cell = row.createCell(3);
            cell.setCellValue(searchResultExcelDtoList.get(i).getCnt());
        }

        rowNum = 0;
        Sheet noticeSheet = wb.createSheet("재확산 자동추적");
        row = noticeSheet.createRow(rowNum++);

        cell =row.createCell(0);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(1);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(2);
        cell.setCellValue("방문 날짜");

        cell=row.createCell(3);
        cell.setCellValue("방문 횟수");

        for(int i=0; i<noticeListExcelDtoList.size(); i++) {
            row = noticeSheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(noticeListExcelDtoList.get(i).getUserNm());

            cell = row.createCell(1);
            cell.setCellValue(noticeListExcelDtoList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(noticeListExcelDtoList.get(i).getDate());

            cell = row.createCell(3);
            cell.setCellValue(noticeListExcelDtoList.get(i).getCnt());
        }

        rowNum = 0;
        Sheet loginHistory = wb.createSheet("사용자 접속 이력");
        row = loginHistory.createRow(rowNum++);

        cell=row.createCell(0);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(2);
        cell.setCellValue("방문날짜");

        cell=row.createCell(3);
        cell.setCellValue("방문횟수");

        for(int i=0; i<loginExcelDtoList.size(); i++) {
            row=loginHistory.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(loginExcelDtoList.get(i).getUserId());
            log.info("getUserId"+loginExcelDtoList.get(i).getUserId());

            cell = row.createCell(1);
            cell.setCellValue(loginExcelDtoList.get(i).getUserNm());
            log.info("getUserNm "+loginExcelDtoList.get(i).getUserNm());

            cell = row.createCell(2);
            cell.setCellValue(loginExcelDtoList.get(i).getDate());
            log.info("getDate"+loginExcelDtoList.get(i).getDate());

            cell = row.createCell(3);
            cell.setCellValue(loginExcelDtoList.get(i).getCnt());
            log.info("getCnt"+loginExcelDtoList.get(i).getCnt());

        }

        String fileName = "사용자 현황";
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");


        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();

    }

}
