package com.nex.common;

public interface Consts {
    String LOGIN_SESSION = "LOGIN_SESSION";
    String SESSION_USER_UNO = "userUno";
    String SESSION_USER_ID = "userId";
    String SESSION_USER_NM = "userNm";
    String SESSION_USER_CL = "crawling_limit";
    String SESSION_USER_PL = "percent_limit";
    String SESSION_IS_ADMIN = "isAdmin";

    Integer PAGE_SIZE = 12;     // 1page에 보여줄 row 개수
    Integer MAX_PAGE = 10;      // 하단 페이징 부분

    String DATA_STAT_CD_NORMAL = "10";      // 정상
    String DATA_STAT_CD_DELETE = "20";      // 삭제

    String TRK_STAT_CD_NULL = null;         // null
    String TRK_STAT_CD_MONITORING = "10";   // 모니터링
    String TRK_STAT_CD_DEL_REQ = "20";      // 삭제요청
    String TRK_STAT_CD_DEL_CMPL = "30";  // 삭제완료

    String GOOGLE = "GOOGLE";
    String FACEBOOK = "FACEBOOK";
    String INSTAGRAM = "INSTAGRAM";

    String MONITORING_CD_NONE = "10";
    String MONITORING_CD_ING = "20";

}
