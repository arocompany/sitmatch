package com.nex.user.service;

import com.nex.Chart.dto.*;
import com.nex.Chart.entity.LoginHistEntity;
import com.nex.Chart.repo.LoginHistRepository;
import com.nex.common.CommonCode;
import com.nex.common.EncryptUtil;
import com.nex.user.entity.UserEntity;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LoginHistRepository loginHistRepository;
    private final EncryptUtil encryptUtil = new EncryptUtil();

    public String memberSignUp(UserEntity userInfo) {
        if (userRepository.countByUserId(userInfo.getUserId()) > 0) { // 아이디 중복 여부 확인
            return "중복된 아이디 입니다.";
        }
/*
        if(userRepository.countByEmail(userInfo.getEmailId(), userInfo.getEmailDomain()) > 0) { // 이메일 중복 여부 확인
            return "중복된 이메일 입니다.";
        }
        if(userRepository.countByPhoneNum(userInfo.getPhoneNum1(), userInfo.getPhoneNum2(), userInfo.getPhoneNum3()) > 0) { // 핸드폰 중복 여부 확인
            return "중복된 핸드폰 번호 입니다.";
        }
*/

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
                .userChkCnt(0)
                .chkLoginDt(Timestamp.valueOf(LocalDateTime.now()))
                .chkYn("Y")
                .build();
        userRepository.save(user);
        return "success";
    }

    public String modifyUserInfo(UserEntity userInfo) {
        UserEntity user = userRepository.findByUserUno(userInfo.getUserUno());
//            if(StringUtils.isNotBlank(userInfo.getUserId())) user.setUserId((userInfo.getUserId()));      // 아이디 변경 불가
        if (StringUtils.isNotBlank(userInfo.getUserPw())) {
            user.setUserPw(encryptUtil.getEncrypt(userInfo.getUserPw(), encryptUtil.getSalt()));
            user.setPwModifyDt((Timestamp.valueOf(LocalDateTime.now())));                                               // 비밀번호 변경 시간 업데이트
        }
        if (StringUtils.isNotBlank(userInfo.getUserNm())) user.setUserNm((userInfo.getUserNm()));
        if (StringUtils.isNotBlank(userInfo.getOrgNm())) user.setOrgNm((userInfo.getOrgNm()));
        if (StringUtils.isNotBlank(userInfo.getUserPosition())) user.setUserPosition((userInfo.getUserPosition()));
        if (StringUtils.isNotBlank(userInfo.getEmailId())) user.setEmailId((userInfo.getEmailId()));
        if (StringUtils.isNotBlank(userInfo.getEmailDomain())) user.setEmailDomain((userInfo.getEmailDomain()));
        if (StringUtils.isNotBlank(userInfo.getPhoneNum1())) user.setPhoneNum1((userInfo.getPhoneNum1()));
        if (StringUtils.isNotBlank(userInfo.getPhoneNum2())) user.setPhoneNum2((userInfo.getPhoneNum2()));
        if (StringUtils.isNotBlank(userInfo.getPhoneNum3())) user.setPhoneNum3((userInfo.getPhoneNum3()));
        if (StringUtils.isNotBlank(userInfo.getBizDetail())) user.setBizDetail((userInfo.getBizDetail()));

        user.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));

        userRepository.save(user);

        return "Success";
    }

    public void deleteCounselor(Long userUno) {
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

        userRepository.save(user);
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

/*
    public Map<String, Object> getAutoKeyword(String auto_user_id){
        Map<String,Object> outMap = new HashMap<>();
        List<AutoKeywordInterface> autoKeyword_list = autoRepository.auto_list_select(auto_user_id);

        outMap.put("autoKeyword_list", autoKeyword_list);

        return outMap;
    }
*/

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

    public List<LoginExcelDto> userHistExcel(String fromDate, String toDate2) {
        return userRepository.userHistExcel(fromDate, toDate2);
    }

    public void excelUserHistory(HttpServletResponse response, List<LoginExcelDto> loginExcelDtoList) throws IOException {
        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("사용자 접속 이력");
        Row row;
        Cell cell;
        int rowNum = 0;

        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(2);
        cell.setCellValue("방문날짜");

        cell = row.createCell(3);
        cell.setCellValue("방문횟수");

        for (LoginExcelDto loginExcelDto : loginExcelDtoList) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(loginExcelDto.getUserId());
            log.info("getUserId" + loginExcelDto.getUserId());

            cell = row.createCell(1);
            cell.setCellValue(loginExcelDto.getUserNm());
            log.info("getUserNm " + loginExcelDto.getUserNm());

            cell = row.createCell(2);
            cell.setCellValue(loginExcelDto.getDate());
            log.info("getDate" + loginExcelDto.getDate());

            cell = row.createCell(3);
            cell.setCellValue(loginExcelDto.getCnt());
            log.info("getCnt" + loginExcelDto.getCnt());

        }

        String fileName = "기간별 사용자 현황";
        fileName = setFileName(fileName);

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();
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
            , List<AllTimeCntExcelDto> dateAlltimeMonitoringExcelList
            , List<StatisticsDto> statisticsSearchInfoByTsiType1
            , List<StatisticsDto> statisticsSearchResultByTsiType1
            , List<StatisticsDto> statisticsSearchInfoMonitoringByTsiType1
            , List<StatisticsDto> statisticsSearchResultMonitoringByTsiType1
            , List<StatisticsDto> statisticsSearchInfoMonitoringByTsiTypeAndUser1
            , List<StatisticsDto> statisticsSearchInfoByTsiType2
            , List<StatisticsDto> statisticsSearchResultByTsiType2
            , List<StatisticsDto> statisticsSearchInfoMonitoringByTsiType2
            , List<StatisticsDto> statisticsSearchResultMonitoringByTsiType2
            , List<StatisticsDto> statisticsSearchInfoMonitoringByTsiTypeAndUser2) throws IOException {

        log.info("prcuseExcel 진입");

        Workbook wb = new XSSFWorkbook();
        Sheet infoSheet = wb.createSheet("화면별 접속 현황");

        Row row;
        Cell cell;
        int rowNum = 0;

        row = infoSheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("화면명");

        cell = row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(2);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(3);
        cell.setCellValue("방문 날짜");

        cell = row.createCell(4);
        cell.setCellValue("방문 횟수");

        for (SearchInfoExcelDto searchInfoExcelDto : searchInfoExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            if (searchInfoExcelDto.equals(searchInfoExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("이력관리");

                cell = row.createCell(1);
                cell.setCellValue(searchInfoExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchInfoExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchInfoExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchInfoExcelDto.getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(searchInfoExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchInfoExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchInfoExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchInfoExcelDto.getCnt());
            }
        }

        for (TraceHistExcelDto traceHistExcelDto : traceHistExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            if (traceHistExcelDto.equals(traceHistExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("모니터링");

                cell = row.createCell(1);
                cell.setCellValue(traceHistExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(traceHistExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(traceHistExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(traceHistExcelDto.getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(traceHistExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(traceHistExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(traceHistExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(traceHistExcelDto.getCnt());
            }
        }

        for (SearchResultExcelDto searchResultExcelDto : searchResultExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            if (searchResultExcelDto.equals(searchResultExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("검색결과");

                cell = row.createCell(1);
                cell.setCellValue(searchResultExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchResultExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchResultExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchResultExcelDto.getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(searchResultExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchResultExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchResultExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(searchResultExcelDto.getCnt());
            }
        }

        for (NoticeListExcelDto noticeListExcelDto : noticeListExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            if (noticeListExcelDto.equals(noticeListExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("재확산 자동추적");

                cell = row.createCell(1);
                cell.setCellValue(noticeListExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(noticeListExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(noticeListExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(noticeListExcelDto.getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(noticeListExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(noticeListExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(noticeListExcelDto.getDate());

                cell = row.createCell(4);
                cell.setCellValue(noticeListExcelDto.getCnt());
            }
        }

        rowNum = 0;
        Sheet loginHistory = wb.createSheet("사용자 접속 이력");
        row = loginHistory.createRow(rowNum++);

        cell = row.createCell(0);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(2);
        cell.setCellValue("방문날짜");

        cell = row.createCell(3);
        cell.setCellValue("방문횟수");

        for (LoginExcelDto loginExcelDto : loginExcelDtoList) {
            row = loginHistory.createRow(rowNum++);

            cell = row.createCell(0);
            cell.setCellValue(loginExcelDto.getUserId());
            log.info("getUserId" + loginExcelDto.getUserId());

            cell = row.createCell(1);
            cell.setCellValue(loginExcelDto.getUserNm());
            log.info("getUserNm " + loginExcelDto.getUserNm());

            cell = row.createCell(2);
            cell.setCellValue(loginExcelDto.getDate());
            log.info("getDate" + loginExcelDto.getDate());

            cell = row.createCell(3);
            cell.setCellValue(loginExcelDto.getCnt());
            log.info("getCnt" + loginExcelDto.getCnt());

        }

        rowNum = 0;
        Sheet keywordHistory = wb.createSheet("기간별 검색 키워드 현황");
        row = keywordHistory.createRow(rowNum++);

        cell = row.createCell(0);
        cell.setCellValue("검색 횟수");

        cell = row.createCell(1);
        cell.setCellValue("키워드 결과 갯수");

        cell = row.createCell(2);
        cell.setCellValue("검색 날짜");


        for (DateKeywordExcelDto dateKeywordExcelDto : dateSearchInfoResultExcelList) {
            row = keywordHistory.createRow(rowNum++);

            cell = row.createCell(0);
            cell.setCellValue(dateKeywordExcelDto.getKeywordCnt());

            cell = row.createCell(1);
            cell.setCellValue(dateKeywordExcelDto.getResultCnt());

            cell = row.createCell(2);
            cell.setCellValue(dateKeywordExcelDto.getResultDate());
        }

        rowNum = 0;
        Sheet monitoringCnt = wb.createSheet("기간별 모니터링 현황");
        row = monitoringCnt.createRow(rowNum++);

        cell = row.createCell(0);
        cell.setCellValue("화면명");

        cell = row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(2);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(3);
        cell.setCellValue("요청 갯수");

        cell = row.createCell(4);
        cell.setCellValue("요청 날짜");

        for (MonitoringHistExcelDto monitoringHistExcelDto : dateMonitoringExcelList) {
            row = monitoringCnt.createRow(rowNum++);

            if (monitoringHistExcelDto.equals(dateMonitoringExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("모니터링한 갯수");

                cell = row.createCell(1);
                cell.setCellValue(monitoringHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(monitoringHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(monitoringHistExcelDto.getMonitoringCnt());
                cell = row.createCell(4);
                cell.setCellValue(monitoringHistExcelDto.getMonitoringDate());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(monitoringHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(monitoringHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(monitoringHistExcelDto.getMonitoringCnt());
                cell = row.createCell(4);
                cell.setCellValue(monitoringHistExcelDto.getMonitoringDate());
            }
        }

        for (DeleteReqHistExcelDto deleteReqHistExcelDto : dateMonitoringReqExcelList) {
            row = monitoringCnt.createRow(rowNum++);
            if (deleteReqHistExcelDto.equals(dateMonitoringReqExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("삭제 요청한 갯수");

                cell = row.createCell(1);
                cell.setCellValue(deleteReqHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteReqHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteReqHistExcelDto.getDeleteRequestCnt());
                cell = row.createCell(4);
                cell.setCellValue(deleteReqHistExcelDto.getDeleteRequestDate());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(deleteReqHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteReqHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteReqHistExcelDto.getDeleteRequestCnt());
                cell = row.createCell(4);
                cell.setCellValue(deleteReqHistExcelDto.getDeleteRequestDate());
            }
        }

        for (DeleteComptHistExcelDto deleteComptHistExcelDto : dateMonitoringComptExcelList) {
            row = monitoringCnt.createRow(rowNum++);
            if (deleteComptHistExcelDto.equals(dateMonitoringComptExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("삭제 완료된 갯수");

                cell = row.createCell(1);
                cell.setCellValue(deleteComptHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteComptHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteComptHistExcelDto.getDeleteComptCnt());
                cell = row.createCell(4);
                cell.setCellValue(deleteComptHistExcelDto.getDeleteComptDate());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(deleteComptHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteComptHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteComptHistExcelDto.getDeleteComptCnt());
                cell = row.createCell(4);
                cell.setCellValue(deleteComptHistExcelDto.getDeleteComptDate());
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

        for (AllTimeCntExcelDto allTimeCntExcelDto : dateAlltimeMonitoringExcelList) {
            row = allTimeMonitoringCnt.createRow(rowNum++);
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

        makeSheet(wb, "모니터링 대상(이력관리 촬영물 수) - 피해촬영물", statisticsSearchInfoByTsiType1, false);
        makeSheet(wb, "모니터링 결과(이력관리-검색결과) - 피해촬영물", statisticsSearchResultByTsiType1, false);
        makeSheet(wb, "24시 모니터링 대상 - 피해촬영물", statisticsSearchInfoMonitoringByTsiType1, false);
        makeSheet(wb, "24시 모니터링 결과 - 피해촬영물", statisticsSearchResultMonitoringByTsiType1, false);
        makeSheet(wb, "담당자별 모니터링 대상(이력관리 촬영물 수) - 피해촬영물", statisticsSearchInfoMonitoringByTsiTypeAndUser1, true);
        makeSheet(wb, "모니터링 대상(이력관리 촬영물 수) - 아동청소년", statisticsSearchInfoByTsiType2, false);
        makeSheet(wb, "모니터링 결과(이력관리-검색결과) - 아동청소년", statisticsSearchResultByTsiType2, false);
        makeSheet(wb, "24시 모니터링 대상 - 아동청소년", statisticsSearchInfoMonitoringByTsiType2, false);
        makeSheet(wb, "24시 모니터링 결과 - 아동청소년", statisticsSearchResultMonitoringByTsiType2, false);
        makeSheet(wb, "담당자별 모니터링 대상(이력관리 촬영물 수) - 아동청소년", statisticsSearchInfoMonitoringByTsiTypeAndUser2, true);

        String fileName = "사용자 현황";
        fileName = setFileName(fileName);

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

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

        Sheet infoSheet = wb.createSheet("화면별 접속 기록");

        Row row;
        Cell cell;

        int rowNum = 0;

        row = infoSheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("화면명");

        cell = row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(2);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(3);
        cell.setCellValue("방문 횟수");

        for (SearchInfoExcelDto searchInfoExcelDto : searchInfoExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            if (searchInfoExcelDto.equals(searchInfoExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("이력관리");

                cell = row.createCell(1);
                cell.setCellValue(searchInfoExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchInfoExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchInfoExcelDto.getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(searchInfoExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchInfoExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchInfoExcelDto.getCnt());
            }
        }

        for (TraceHistExcelDto traceHistExcelDto : traceHistExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            if (traceHistExcelDto.equals(traceHistExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("모니터링");

                cell = row.createCell(1);
                cell.setCellValue(traceHistExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(traceHistExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(traceHistExcelDto.getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(traceHistExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(traceHistExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(traceHistExcelDto.getCnt());
            }
        }

        for (SearchResultExcelDto searchResultExcelDto : searchResultExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            if (searchResultExcelDto.equals(searchResultExcelDtoList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("검색결과");

                cell = row.createCell(1);
                cell.setCellValue(searchResultExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchResultExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchResultExcelDto.getCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(searchResultExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(searchResultExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(searchResultExcelDto.getCnt());
            }
        }

        for (NoticeListExcelDto noticeListExcelDto : noticeListExcelDtoList) {
            row = infoSheet.createRow(rowNum++);

            cell = row.createCell(0);
            cell.setCellValue("재확산 자동추적");

            cell = row.createCell(1);
            cell.setCellValue(noticeListExcelDto.getUserNm());
            log.info("noticeListExcelDtoList: " + noticeListExcelDto.getUserId());

            cell = row.createCell(2);
            cell.setCellValue(noticeListExcelDto.getUserId());

            cell = row.createCell(3);
            cell.setCellValue(noticeListExcelDto.getCnt());
        }

        rowNum = 0;
        Sheet loginHistory = wb.createSheet("사용자 접속 현황");
        row = loginHistory.createRow(rowNum++);

        cell = row.createCell(0);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(2);
        cell.setCellValue("방문횟수");

        for (LoginExcelDto loginExcelDto : loginExcelDtoList) {
            row = loginHistory.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(loginExcelDto.getUserId());

            cell = row.createCell(1);
            cell.setCellValue(loginExcelDto.getUserNm());

            cell = row.createCell(2);
            cell.setCellValue(loginExcelDto.getCnt());
        }

        rowNum = 0;
        Sheet userSearchCntList = wb.createSheet("사용자별 검색 키워드 현황");
        row = userSearchCntList.createRow(rowNum++);

        cell = row.createCell(0);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(1);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(2);
        cell.setCellValue("키워드 검색 횟수");

        cell = row.createCell(3);
        cell.setCellValue("검색 갯수 총결과");

        for (SearchInfoExcelDto searchInfoExcelDto : userKeywordCntExcelList) {
            row = userSearchCntList.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(searchInfoExcelDto.getUserNm());

            cell = row.createCell(1);
            cell.setCellValue(searchInfoExcelDto.getUserId());

            cell = row.createCell(2);
            cell.setCellValue(searchInfoExcelDto.getKeywordCnt());

            cell = row.createCell(3);
            cell.setCellValue(searchInfoExcelDto.getCnt());
        }

        rowNum = 0;
        Sheet userMonitoringCntList = wb.createSheet("사용자별 모니터링 현황");
        row = userMonitoringCntList.createRow(rowNum++);

        cell = row.createCell(0);
        cell.setCellValue("화면명");

        cell = row.createCell(1);
        cell.setCellValue("사용자 이름");

        cell = row.createCell(2);
        cell.setCellValue("사용자 아이디");

        cell = row.createCell(3);
        cell.setCellValue("작업 횟수");

        for (MonitoringHistExcelDto monitoringHistExcelDto : userMonitoringExcelList) {
            row = userMonitoringCntList.createRow(rowNum++);
            if (monitoringHistExcelDto.equals(userMonitoringExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("모니터링 한 갯수");

                cell = row.createCell(1);
                cell.setCellValue(monitoringHistExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(monitoringHistExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(monitoringHistExcelDto.getMonitoringCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(monitoringHistExcelDto.getUserNm());

                cell = row.createCell(2);
                cell.setCellValue(monitoringHistExcelDto.getUserId());

                cell = row.createCell(3);
                cell.setCellValue(monitoringHistExcelDto.getMonitoringCnt());
            }
        }

        for (DeleteReqHistExcelDto deleteReqHistExcelDto : userDeleteReqExcelList) {
            if (deleteReqHistExcelDto.equals(userDeleteReqExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("삭제요청 한 갯수");

                cell = row.createCell(1);
                cell.setCellValue(deleteReqHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteReqHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteReqHistExcelDto.getDeleteRequestCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");

                cell = row.createCell(1);
                cell.setCellValue(deleteReqHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteReqHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteReqHistExcelDto.getDeleteRequestCnt());
            }
        }

        for (DeleteComptHistExcelDto deleteComptHistExcelDto : userDeleteComptExcelList) {
            if (deleteComptHistExcelDto.equals(userDeleteComptExcelList.get(0))) {
                cell = row.createCell(0);
                cell.setCellValue("삭제 완료된 갯수");
                cell = row.createCell(1);
                cell.setCellValue(deleteComptHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteComptHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteComptHistExcelDto.getDeleteComptCnt());
            } else {
                cell = row.createCell(0);
                cell.setCellValue("");
                cell = row.createCell(1);
                cell.setCellValue(deleteComptHistExcelDto.getUserNm());
                cell = row.createCell(2);
                cell.setCellValue(deleteComptHistExcelDto.getUserId());
                cell = row.createCell(3);
                cell.setCellValue(deleteComptHistExcelDto.getDeleteComptCnt());
            }
        }

        rowNum = 0;
        Sheet userAlltimeMonitoring = wb.createSheet("24시 모니터링 그래프");
        row = userAlltimeMonitoring.createRow(rowNum++);

        cell = row.createCell(0);
        cell.setCellValue("사용자 이름");
        cell = row.createCell(1);
        cell.setCellValue("사용자 아이디");
        cell = row.createCell(2);
        cell.setCellValue("24시 모니터링한 갯수");
        cell = row.createCell(3);
        cell.setCellValue("24시 모니터링 결과 갯수");
//        cell=row.createCell(4);
//        cell.setCellValue("검색 날짜");

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
        }

        String fileName = "사용자별 현황";
        fileName = setFileName(fileName);

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();
    }

    public String setFileName(String fileName) {
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return fileName;
    }

    public void makeSheet(Workbook wb, String title, List<StatisticsDto> list, Boolean isStatisticsByUser){
        int rowNum = 0;
        Sheet sheet = wb.createSheet(title);
        Row row;
        Cell cell;
        row = sheet.createRow(rowNum++);

        

        if(!isStatisticsByUser){
            cell = row.createCell(0); cell.setCellValue("계");
            cell = row.createCell(1); cell.setCellValue("갯수");
            for (StatisticsDto info : list) {
                row = sheet.createRow(rowNum++);
                cell = row.createCell(0);
                String strTsiType = "";
                switch (info.getTsiType()){
                    case CommonCode.searchTypeKeyword -> strTsiType = "키워드";
                    case CommonCode.searchTypeKeywordImage -> strTsiType = "키워드+이미지";
                    case CommonCode.searchTypeKeywordVideo -> strTsiType = "키워드+비디오";
                    case CommonCode.searchTypeImage -> strTsiType = "이미지";
                    case CommonCode.searchTypeVideo -> strTsiType = "영상";
                }
                cell.setCellValue(strTsiType);

                cell = row.createCell(1);
                cell.setCellValue(info.getCnt());
            }
        }else{
            cell = row.createCell(0); cell.setCellValue("ID");
            cell = row.createCell(1); cell.setCellValue("이름");
            cell = row.createCell(2); cell.setCellValue("키워드");
            cell = row.createCell(3); cell.setCellValue("키워드 + 이미지");
            cell = row.createCell(4); cell.setCellValue("키워드 + 영상");
            cell = row.createCell(5); cell.setCellValue("이미지");
            cell = row.createCell(6); cell.setCellValue("영상");
            for (StatisticsDto info : list) {
                row = sheet.createRow(rowNum++);
                cell = row.createCell(0); cell.setCellValue(info.getUserId());
                cell = row.createCell(1); cell.setCellValue(info.getUserNm());
                cell = row.createCell(2); cell.setCellValue(info.getCntKeyword());
                cell = row.createCell(3); cell.setCellValue(info.getCntKeywordImg());
                cell = row.createCell(4); cell.setCellValue(info.getCntKeywordMov());
                cell = row.createCell(5); cell.setCellValue(info.getCntImg());
                cell = row.createCell(6); cell.setCellValue(info.getCntMov());
            }
        }
    }

}
