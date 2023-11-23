package com.nex.user.service;

import com.nex.Chart.dto.*;
import com.nex.Chart.entity.LoginHistEntity;
import com.nex.common.EncryptUtil;
import com.nex.user.entity.*;
import com.nex.user.repo.AutoRepository;
import com.nex.Chart.repo.LoginHistRepository;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public List<LoginHistDto> getUserHistory(String toDate) {
        return userRepository.userHistList(toDate);
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



        String fileName = "기간별 사용자 현황";
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
                , List<NoticeListExcelDto> noticeListExcelDtoList
                , List<LoginExcelDto> loginExcelDtoList
                , List<DateKeywordExcelDto> dateSearchInfoResultExcelList
                , List<MonitoringHistExcelDto> dateMonitoringExcelList
                , List<DeleteReqHistExcelDto> dateMonitoringReqExcelList
                , List<DeleteComptHistExcelDto> dateMonitoringComptExcelList
                , List<AllTimeCntExcelDto> dateAlltimeMonitoringExcelList) throws IOException {

            log.info("prcuseExcel 진입");

            Workbook wb = new XSSFWorkbook();
            Sheet infoSheet = wb.createSheet("화면별 접속 현황");

            Row row;
            Cell cell;
            int rowNum = 0;

            row = infoSheet.createRow(rowNum++);
            cell=row.createCell(0);
            cell.setCellValue("화면명");

            cell=row.createCell(1);
            cell.setCellValue("사용자 이름");

            cell=row.createCell(2);
            cell.setCellValue("사용자 아이디");

            cell=row.createCell(3);
            cell.setCellValue("방문 날짜");

            cell=row.createCell(4);
            cell.setCellValue("방문 횟수");

            for(int i=0; i<searchInfoExcelDtoList.size(); i++) {
                row = infoSheet.createRow(rowNum++);

                if(searchInfoExcelDtoList.get(i).equals(searchInfoExcelDtoList.get(0))) {
                    cell = row.createCell(0);
                    cell.setCellValue("이력관리");

                    cell = row.createCell(1);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getCnt());
                } else {
                    cell = row.createCell(0);
                    cell.setCellValue("");

                    cell = row.createCell(1);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(searchInfoExcelDtoList.get(i).getCnt());
                }
            }

            for(int i=0; i<traceHistExcelDtoList.size(); i++) {
                row=infoSheet.createRow(rowNum++);

                if(traceHistExcelDtoList.get(i).equals(traceHistExcelDtoList.get(0))) {
                    cell = row.createCell(0);
                    cell.setCellValue("모니터링");

                    cell = row.createCell(1);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getCnt());
                } else {
                    cell = row.createCell(0);
                    cell.setCellValue("");

                    cell = row.createCell(1);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(traceHistExcelDtoList.get(i).getCnt());
                }
            }

            for(int i=0; i<searchResultExcelDtoList.size(); i++) {
                row=infoSheet.createRow(rowNum++);

                if(searchResultExcelDtoList.get(i).equals(searchResultExcelDtoList.get(0))) {
                    cell = row.createCell(0);
                    cell.setCellValue("검색결과");

                    cell = row.createCell(1);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getCnt());
                } else {
                    cell = row.createCell(0);
                    cell.setCellValue("");

                    cell = row.createCell(1);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(searchResultExcelDtoList.get(i).getCnt());
                }
            }

            for(int i=0; i<noticeListExcelDtoList.size(); i++) {
                row = infoSheet.createRow(rowNum++);

                if(noticeListExcelDtoList.get(i).equals(noticeListExcelDtoList.get(0))) {
                    cell = row.createCell(0);
                    cell.setCellValue("재확산 자동추적");

                    cell = row.createCell(1);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getCnt());
                } else {
                    cell = row.createCell(0);
                    cell.setCellValue("");

                    cell = row.createCell(1);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getUserNm());

                    cell = row.createCell(2);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getUserId());

                    cell = row.createCell(3);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getDate());

                    cell = row.createCell(4);
                    cell.setCellValue(noticeListExcelDtoList.get(i).getCnt());
                }
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

            rowNum = 0;
            Sheet keywordHistory = wb.createSheet("기간별 검색 키워드 현황");
            row = keywordHistory.createRow(rowNum++);

            cell=row.createCell(0);
            cell.setCellValue("사용자 이름");

            cell=row.createCell(1);
            cell.setCellValue("사용자 아이디");

            cell=row.createCell(2);
            cell.setCellValue("검색 횟수");

            cell=row.createCell(3);
            cell.setCellValue("키워드 결과 갯수");

            cell=row.createCell(4);
            cell.setCellValue("검색 날짜");


            for(int i=0; i<dateSearchInfoResultExcelList.size(); i++){
                row = keywordHistory.createRow(rowNum++);

                cell = row.createCell(0);
                cell.setCellValue(dateSearchInfoResultExcelList.get(i).getUserNm());

                cell = row.createCell(1);
                cell.setCellValue(dateSearchInfoResultExcelList.get(i).getUserId());

                cell = row.createCell(2);
                cell.setCellValue(dateSearchInfoResultExcelList.get(i).getKeywordCnt());

                cell = row.createCell(3);
                cell.setCellValue(dateSearchInfoResultExcelList.get(i).getResultCnt());

                cell = row.createCell(4);
                cell.setCellValue(dateSearchInfoResultExcelList.get(i).getResultDate());

            }

            rowNum = 0;
            Sheet monitoringCnt = wb.createSheet("기간별 모니터링 현황");
            row = monitoringCnt.createRow(rowNum++);

            cell=row.createCell(0);
            cell.setCellValue("화면명");

            cell=row.createCell(1);
            cell.setCellValue("사용자 이름");

            cell=row.createCell(2);
            cell.setCellValue("사용자 아이디");

            cell=row.createCell(3);
            cell.setCellValue("요청 갯수");

            cell=row.createCell(4);
            cell.setCellValue("요청 날짜");

            for(int i=0; i<dateMonitoringExcelList.size(); i++) {
                row = monitoringCnt.createRow(rowNum++);

                if(dateMonitoringExcelList.get(i).equals(dateMonitoringExcelList.get(0))) {
                    cell = row.createCell(0);
                    cell.setCellValue("모니터링한 갯수");

                    cell = row.createCell(1);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getUserNm());
                    cell = row.createCell(2);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getUserId());
                    cell = row.createCell(3);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getMonitoringCnt());
                    cell = row.createCell(4);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getMonitoringDate());
                } else {
                    cell = row.createCell(0);
                    cell.setCellValue("");

                    cell = row.createCell(1);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getUserNm());
                    cell = row.createCell(2);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getUserId());
                    cell = row.createCell(3);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getMonitoringCnt());
                    cell = row.createCell(4);
                    cell.setCellValue(dateMonitoringExcelList.get(i).getMonitoringDate());
                }
            }

            for(int i=0; i<dateMonitoringReqExcelList.size(); i++) {
                row = monitoringCnt.createRow(rowNum++);
                if(dateMonitoringReqExcelList.get(i).equals(dateMonitoringReqExcelList.get(0))){
                    cell = row.createCell(0);
                    cell.setCellValue("삭제 요청한 갯수");

                    cell = row.createCell(1);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getUserNm());
                    cell = row.createCell(2);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getUserId());
                    cell = row.createCell(3);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getDeleteRequestCnt());
                    cell = row.createCell(4);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getDeleteRequestDate());
                } else {
                    cell = row.createCell(0);
                    cell.setCellValue("");

                    cell = row.createCell(1);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getUserNm());
                    cell = row.createCell(2);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getUserId());
                    cell = row.createCell(3);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getDeleteRequestCnt());
                    cell = row.createCell(4);
                    cell.setCellValue(dateMonitoringReqExcelList.get(i).getDeleteRequestDate());
                }
            }

            for(int i=0; i<dateMonitoringComptExcelList.size(); i++){
                row = monitoringCnt.createRow(rowNum++);
                if(dateMonitoringComptExcelList.get(i).equals(dateMonitoringComptExcelList.get(0))) {
                    cell = row.createCell(0);
                    cell.setCellValue("삭제 완료된 갯수");

                    cell = row.createCell(1);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getUserNm());
                    cell = row.createCell(2);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getUserId());
                    cell = row.createCell(3);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getDeleteComptCnt());
                    cell = row.createCell(4);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getDeleteComptDate());
                } else {
                    cell = row.createCell(0);
                    cell.setCellValue("");

                    cell = row.createCell(1);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getUserNm());
                    cell = row.createCell(2);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getUserId());
                    cell = row.createCell(3);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getDeleteComptCnt());
                    cell = row.createCell(4);
                    cell.setCellValue(dateMonitoringComptExcelList.get(i).getDeleteComptDate());
                }
            }

            rowNum = 0;
            Sheet allTimeMonitoringCnt = wb.createSheet("기간별 24시 모니터링 현황");
            row = allTimeMonitoringCnt.createRow(rowNum++);

            cell = row.createCell(0);
            cell.setCellValue("사용자 이름");

            cell = row.createCell(1);
            cell.setCellValue("사용자 아이디");

            cell = row.createCell(2);
            cell.setCellValue("24시 모니터링한 갯수");

            cell = row.createCell(3);
            cell.setCellValue("24시 모니터링한 갯수");

            cell = row.createCell(4);
            cell.setCellValue("24시 모니터링한 날짜");

            for(int i=0; i<dateAlltimeMonitoringExcelList.size(); i++) {
                row = allTimeMonitoringCnt.createRow(rowNum++);
                cell = row.createCell(0);
                cell.setCellValue(dateAlltimeMonitoringExcelList.get(i).getUserName());

                cell = row.createCell(1);
                cell.setCellValue(dateAlltimeMonitoringExcelList.get(i).getUserId());

                cell = row.createCell(2);
                cell.setCellValue(dateAlltimeMonitoringExcelList.get(i).getMonitoringCnt());

                cell = row.createCell(3);
                cell.setCellValue(dateAlltimeMonitoringExcelList.get(i).getMonitoringResultCnt());

                cell = row.createCell(4);
                cell.setCellValue(dateAlltimeMonitoringExcelList.get(i).getMonitoringDate());

            }

            String fileName = "사용자 현황";
            fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

            response.setContentType("ms-vnd/excel");
            response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");

            ServletOutputStream out = response.getOutputStream();
            wb.write(out);
            out.flush();

    }

    public void connectExcel(HttpServletResponse response
            , List<SearchInfoExcelDto> searchInfoExcelDtoList
            , List<TraceHistExcelDto> traceHistExcelDtoList
            , List<SearchResultExcelDto> searchResultExcelDtoList
            , List<NoticeListExcelDto> noticeListExcelDtoList
            , List<LoginExcelDto> loginExcelDtoList
            , List<SearchInfoExcelDto> userKeywordCntExcelList
            , List<MonitoringHistExcelDto> userMonitoringExcelList
            , List<DeleteReqHistExcelDto> userDeleteReqExcelList
            , List<DeleteComptHistExcelDto> userDeleteComptExcelList
            , List<AllTimeCntExcelDto> userAllTimeCntExcelList) throws IOException {

        log.info("connectExcel 진입");

        Workbook wb = new XSSFWorkbook();

        Sheet infoSheet = wb.createSheet("화면별 접속기록");

        Row row;
        Cell cell;

        int rowNum = 0;

        row = infoSheet.createRow(rowNum++);
        cell=row.createCell(0);
        cell.setCellValue("화면명");

        cell=row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(2);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(3);
        cell.setCellValue("방문 날짜");

        cell=row.createCell(4);
        cell.setCellValue("방문 횟수");

        for(int i=0; i<searchInfoExcelDtoList.size(); i++) {
            row = infoSheet.createRow(rowNum++);

            if(searchInfoExcelDtoList.get(i).equals(searchInfoExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("이력관리");

                cell = row.createCell(1);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchInfoExcelDtoList.get(i).getCnt());
            }

        }

        for(int i=0; i<traceHistExcelDtoList.size(); i++) {
            row=infoSheet.createRow(rowNum++);

            if(traceHistExcelDtoList.get(i).equals(traceHistExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("모니터링");

                cell = row.createCell(1);
                cell.setCellValue(traceHistExcelDtoList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(traceHistExcelDtoList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(traceHistExcelDtoList.get(i).getDate());

                cell = row.createCell(4);
                cell.setCellValue(traceHistExcelDtoList.get(i).getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(traceHistExcelDtoList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(traceHistExcelDtoList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(traceHistExcelDtoList.get(i).getDate());

                cell = row.createCell(4);
                cell.setCellValue(traceHistExcelDtoList.get(i).getCnt());

            }

        }

        for(int i=0; i<searchResultExcelDtoList.size(); i++) {
            row = infoSheet.createRow(rowNum++);

            if(searchResultExcelDtoList.get(i).equals(searchResultExcelDtoList.get(0))) {
                cell=row.createCell(0);
                cell.setCellValue("검색결과");

                cell = row.createCell(1);
                cell.setCellValue(searchResultExcelDtoList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchResultExcelDtoList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchResultExcelDtoList.get(i).getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchResultExcelDtoList.get(i).getCnt());
            } else {
                cell=row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(searchResultExcelDtoList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchResultExcelDtoList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchResultExcelDtoList.get(i).getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchResultExcelDtoList.get(i).getCnt());
            }
        }

        for(int i=0; i<noticeListExcelDtoList.size(); i++) {
            row = infoSheet.createRow(rowNum++);

            cell=row.createCell(0);
            cell.setCellValue("재확산 자동추적");

            cell = row.createCell(1);
            cell.setCellValue(noticeListExcelDtoList.get(i).getUserNm());
            log.info("noticeListExcelDtoList: "+noticeListExcelDtoList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(noticeListExcelDtoList.get(i).getUserId());

            cell = row.createCell(3);
            cell.setCellValue(noticeListExcelDtoList.get(i).getDate());

            cell = row.createCell(4);
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

        rowNum = 0;
        Sheet userSearchCntList = wb.createSheet("사용자별 검색 키워드 현황");
        row = userSearchCntList.createRow(rowNum++);

        cell=row.createCell(0);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(1);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(2);
        cell.setCellValue("키워드 검색 횟수");

        cell=row.createCell(3);
        cell.setCellValue("검색 갯수 총결과");
        cell=row.createCell(4);
        cell.setCellValue("검색 날짜");

        for(int i=0; i<userKeywordCntExcelList.size(); i++) {
            row = userSearchCntList.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(userKeywordCntExcelList.get(i).getUserNm());

            cell = row.createCell(1);
            cell.setCellValue(userKeywordCntExcelList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(userKeywordCntExcelList.get(i).getKeywordCnt());

            cell = row.createCell(3);
            cell.setCellValue(userKeywordCntExcelList.get(i).getCnt());

            cell = row.createCell(4);
            cell.setCellValue(userKeywordCntExcelList.get(i).getDate());

        }

        rowNum = 0;
        Sheet userMonitoringCntList = wb.createSheet("사용자별 모니터링 현황");
        row = userMonitoringCntList.createRow(rowNum++);

        cell=row.createCell(0);
        cell.setCellValue("화면명");

        cell=row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell=row.createCell(2);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(3);
        cell.setCellValue("작업 횟수");

        cell=row.createCell(4);
        cell.setCellValue("작업 날짜");

        for(int i=0; i<userMonitoringExcelList.size(); i++) {
            row = userMonitoringCntList.createRow(rowNum++);
            if(userMonitoringExcelList.get(i).equals(userMonitoringExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("모니터링 한 갯수");

                cell = row.createCell(1);
                cell.setCellValue(userMonitoringExcelList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(userMonitoringExcelList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(userMonitoringExcelList.get(i).getMonitoringCnt());

                cell = row.createCell(4);
                cell.setCellValue(userMonitoringExcelList.get(i).getMonitoringDate());

            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(userMonitoringExcelList.get(i).getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(userMonitoringExcelList.get(i).getUserId());

                cell = row.createCell(3);
                cell.setCellValue(userMonitoringExcelList.get(i).getMonitoringCnt());

                cell = row.createCell(4);
                cell.setCellValue(userMonitoringExcelList.get(i).getMonitoringDate());

            }
        }

        for(int i=0; i<userDeleteReqExcelList.size(); i++) {
            if(userDeleteReqExcelList.get(i).equals(userDeleteReqExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("삭제요청 한 갯수");

                cell = row.createCell(1);
                cell.setCellValue(userDeleteReqExcelList.get(i).getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(userDeleteReqExcelList.get(i).getUserId());
                cell = row.createCell(3);
                cell.setCellValue(userDeleteReqExcelList.get(i).getDeleteRequestCnt());
                cell = row.createCell(4);
                cell.setCellValue(userDeleteReqExcelList.get(i).getDeleteRequestDate());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(userDeleteReqExcelList.get(i).getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(userDeleteReqExcelList.get(i).getUserId());
                cell = row.createCell(3);
                cell.setCellValue(userDeleteReqExcelList.get(i).getDeleteRequestCnt());
                cell = row.createCell(4);
                cell.setCellValue(userDeleteReqExcelList.get(i).getDeleteRequestDate());
            }
        }

        for(int i=0; i<userDeleteComptExcelList.size(); i++) {
            if(userDeleteComptExcelList.get(i).equals(userDeleteComptExcelList.get(0))){
                cell = row.createCell(0);
                cell.setCellValue("삭제 완료된 갯수");
                cell = row.createCell(1);
                cell.setCellValue(userDeleteComptExcelList.get(i).getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(userDeleteComptExcelList.get(i).getUserId());
                cell = row.createCell(3);
                cell.setCellValue(userDeleteComptExcelList.get(i).getDeleteComptCnt());
                cell = row.createCell(4);
                cell.setCellValue(userDeleteComptExcelList.get(i).getDeleteComptDate());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");
                cell = row.createCell(1);
                cell.setCellValue(userDeleteComptExcelList.get(i).getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(userDeleteComptExcelList.get(i).getUserId());
                cell = row.createCell(3);
                cell.setCellValue(userDeleteComptExcelList.get(i).getDeleteComptCnt());
                cell = row.createCell(4);
                cell.setCellValue(userDeleteComptExcelList.get(i).getDeleteComptDate());
            }
        }

        rowNum = 0;
        Sheet userAlltimeMonitoring = wb.createSheet("사용자별 24시 모니터링 현황");
        row = userAlltimeMonitoring.createRow(rowNum++);

        cell=row.createCell(0);
        cell.setCellValue("사용자 이름");
        cell=row.createCell(1);
        cell.setCellValue("사용자 아이디");
        cell=row.createCell(2);
        cell.setCellValue("24시 모니터링한 갯수");
        cell=row.createCell(3);
        cell.setCellValue("24시 모니터링 결과 갯수");
        cell=row.createCell(4);
        cell.setCellValue("검색 날짜");

        for (AllTimeCntExcelDto allTimeCntExcelDto : userAllTimeCntExcelList) {
            row = userAlltimeMonitoring.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(allTimeCntExcelDto.getUserName());
            cell = row.createCell(1);
            cell.setCellValue(allTimeCntExcelDto.getUserId());
            cell = row.createCell(2);
            cell.setCellValue(allTimeCntExcelDto.getMonitoringCnt());
            cell = row.createCell(3);
            cell.setCellValue(allTimeCntExcelDto.getMonitoringResultCnt());
            cell = row.createCell(4);
            cell.setCellValue(allTimeCntExcelDto.getMonitoringDate());
        }

        String fileName = "사용자별 현황";
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();

    }

}
