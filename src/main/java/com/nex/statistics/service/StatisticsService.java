package com.nex.statistics.service;

import com.nex.Chart.dto.StatisticsDto;
import com.nex.common.CommonCode;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    public void exportExcel(HttpServletResponse response
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

        Workbook wb = new XSSFWorkbook();

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

        String fileName = "통계";
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
