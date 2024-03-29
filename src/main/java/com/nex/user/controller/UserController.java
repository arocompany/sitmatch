package com.nex.user.controller;

import com.nex.common.Consts;
import com.nex.common.EncryptUtil;
import com.nex.user.entity.SessionInfoDto;
import com.nex.user.entity.UserEntity;
import com.nex.user.entity.UserLoginCheckDto;
import com.nex.user.repo.UserRepository;
import com.nex.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RestController // JSON 형태 결과값을 반환해줌 (@ResponseBody가 필요없음)
@RequiredArgsConstructor // final 객체를 Constructor Injection 해줌. (Autowired 역할)
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EncryptUtil encryptUtil = new EncryptUtil();

    @PostMapping("signup")
    public String memberSignUp(Optional<UserEntity> userInfo) {
        if(userInfo.isPresent()) {
            return userService.memberSignUp(userInfo.get());
        } else {
            return "userInfo is null";
        }
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("html/login");
    }

    @PostMapping("/login")
    public ModelAndView login(@Valid UserLoginCheckDto userLoginCheckDto, BindingResult result, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();

        if (result.hasErrors()) {
            modelAndView.addObject("errorMessage1", "아이디와 ");
            modelAndView.addObject("errorMessage2", "비밀번호를 ");
            modelAndView.addObject("errorMessage3", "입력해주세요.");
            modelAndView.setViewName("html/login");
            return modelAndView;
        }

        if (userRepository.countByUserId(userLoginCheckDto.getLoginId()) > 0) { // 아이디가 있는지 확인
            UserEntity user = userRepository.findByUserId(userLoginCheckDto.getLoginId());
            // log.info("사용자 체크 시간: " +user.getChkLoginDt() );

            Timestamp currentDateTime = new Timestamp(System.currentTimeMillis()); // 현재시간
            Timestamp userChkLoginDt = user.getChkLoginDt();                       // 사용자 잠김시간

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String userChkTime = formatter.format(userChkLoginDt);

            if( user.getChkYn().equals("N") ){
                if( !userChkLoginDt.before(currentDateTime) ){
                    log.info("5회 초과 현재시간이 더 커서 로그인 못함");
                    modelAndView.addObject("errorMessage1", userChkTime);
                    modelAndView.addObject("errorMessage2", " 이후에 ");
                    modelAndView.addObject("errorMessage3", "로그인 하실 수 있습니다.");
                    modelAndView.setViewName("html/login");
                } else {
                    user.setChkYn("Y");
                    user.setUserChkCnt(0);
                    userRepository.save(user);
                }
            } else {
                if (encryptUtil.matches(userLoginCheckDto.getLoginPw(), user.getUserPw())) { // 비밀번호 확인(userInfo로 받은 평문 비밀번호와 디비에 있는 암호화된 비밀번호 비교)
                    // 마지막 로그인 시간 업데이트
                    user.setLstLoginDt(Timestamp.valueOf(LocalDateTime.now()));
                    user.setUserChkCnt(0);
                    user.setChkYn("Y");
                    userRepository.save(user);

                    // password 변경 시간 확인
                    Timestamp pwModifyDt = user.getPwModifyDt();
                    Timestamp nowDt = new Timestamp(new Date().getTime());

                    long diffDay = (nowDt.getTime() - pwModifyDt.getTime()) / 1000 / (24 * 60 * 60);

                    log.debug("diffDay = {}", diffDay);

                    if (diffDay >= 90) {   //   비밀번호 변경일자가 90일이 넘을 경우 패스워드 변경 화면으로 이동
                        if(user.getUserClfCd().equals("99")) { // 관리자인 경우 redirect
                            modelAndView.setViewName("redirect:/");
                        } else {
                            modelAndView.setViewName("redirect:/password");
                        }
                    } else {
                        modelAndView.setViewName("redirect:/");
                    }

                    // 로그인 성공 처리
                    // 세션이 있으면 있는 세션 반환, 없으면 신규 세션 설정
                    setSession(request, user);

                    int userUno = Math.toIntExact(user.getUserUno());
                    String userId = user.getUserId();
                    log.info("userUno: "+userUno+" userId "+userId);
                    userService.loginHistInsert(userUno, userId);

                } else {    // 로그인 실패
                    if (user.getUserChkCnt() >= 5) {
                        log.info("5번 넘으면 현재시간에서 10분 추가");
                        // 현재 시간을 가져오기
                        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

                        // 10분 뒤의 시간을 계산
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(currentTimestamp.getTime());
                        calendar.add(Calendar.MINUTE, 10);

                        // 10분 뒤의 시간을 Timestamp로 변환
                        Timestamp tenMinutesLaterTimestamp = new Timestamp(calendar.getTimeInMillis());
                        user.setChkLoginDt(tenMinutesLaterTimestamp);
                        user.setChkYn("N");
                        userRepository.save(user);

                        String tenMinutesLaterTime = formatter.format(tenMinutesLaterTimestamp);

                        modelAndView.addObject("errorMessage1", "로그인 횟수를 초과하여 ");
                        modelAndView.addObject("errorMessage2", tenMinutesLaterTime + " 이후                           ");
                        modelAndView.addObject("errorMessage3",  " 로그인 하실 수 있습니다.");
                        modelAndView.setViewName("html/login");
                    } else {
                        log.info("chkCnt만 추가");
                        user.setUserChkCnt(user.getUserChkCnt() + 1);
                        userRepository.save(user);

                        modelAndView.addObject("errorMessage1", "아이디");
                        modelAndView.addObject("errorMessage2", " 또는 ");
                        modelAndView.addObject("errorMessage3", "비밀번호를 다시 입력해주세요.");
                        modelAndView.setViewName("html/login");
                    }
                }
            }


        } else {    // 로그인 실패
            modelAndView.addObject("errorMessage1", "아이디");
            modelAndView.addObject("errorMessage2", " 또는 ");
            modelAndView.addObject("errorMessage3", "비밀번호를 다시 입력해주세요.");
            modelAndView.setViewName("html/login");
        }
        return modelAndView;
    }

    @GetMapping("password")
    public ModelAndView password(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("/html/password");
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        log.debug(sessionInfoDto.getUserId());
        return modelAndView;
    }

    @PostMapping("password")
    public ModelAndView password(UserLoginCheckDto userLoginCheckDto) {
        userService.modifyPassword(userLoginCheckDto.getLoginId(), userLoginCheckDto.getLoginPw());
        return new ModelAndView("redirect:/");
    }

    @GetMapping("logout")
    public ModelAndView logout(HttpSession session) {
        session.invalidate();
        return new ModelAndView("redirect:/user/login");
    }

    @PostMapping("modify")
    public ModelAndView userModify(Optional<UserEntity> userInfo) {
        userInfo.ifPresent(userService::modifyUserInfo);
        return new ModelAndView("redirect:/");
    }

    @PostMapping("modify-counselor")
    public ModelAndView modifyCounselor(Optional<UserEntity> userInfo) {
        userInfo.ifPresent(userService::modifyUserInfo);
        return new ModelAndView("redirect:/manage");
    }

    @GetMapping("info")
    public ModelAndView info(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/info");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        UserEntity userInfo = userService.getUserInfoByUserId(sessionInfoDto.getUserId());
        modelAndView.addObject("userInfo", userInfo);

        return modelAndView;
    }

    // 회원 정보 삭제
    @GetMapping("delete")
    public ModelAndView deleteCounselor(@RequestParam Optional<Long> userUno) {
        userUno.ifPresent(userService::deleteCounselor);
        return new ModelAndView("redirect:/manage");
    }

    @PostMapping("ajax_con_limit_update")
    public void ajax_con_limit_update(@Valid UserLoginCheckDto userLoginCheckDto,  BindingResult result, HttpServletRequest request) {
        log.info("testtest////"+userLoginCheckDto.getUserUno()+'/'+userLoginCheckDto.getCrawling_limit()+'/'+userLoginCheckDto.getPercent_limit()+result);

        if(userLoginCheckDto.getCrawling_limit().equals("무제한")){
            log.info("무제한임");
            String crawling_limit = userLoginCheckDto.getCrawling_limit();
            crawling_limit = crawling_limit.replaceAll("무제한","0");
            userRepository.ajax_con_limit_update(userLoginCheckDto.getUserUno(), crawling_limit, userLoginCheckDto.getPercent_limit());
        } else {
            userRepository.ajax_con_limit_update(userLoginCheckDto.getUserUno(), userLoginCheckDto.getCrawling_limit(), userLoginCheckDto.getPercent_limit());
        }

        UserEntity user = userRepository.findByUserUno(userLoginCheckDto.getUserUno());
        setSession(request, user);

    }

    public void setSession(HttpServletRequest request, UserEntity user) {
        HttpSession session = request.getSession();
        SessionInfoDto sessionInfo = SessionInfoDto.builder()
                .userUno(Math.toIntExact(user.getUserUno()))
                .userId(user.getUserId())
                .userNm(user.getUserNm())
                .crawling_limit(user.getCrawling_limit())
                .percent_limit(user.getPercent_limit())
                .build();

        if( user.getUserClfCd().equals("99") ) {
            sessionInfo.setAdmin(true);
            session.setAttribute(Consts.SESSION_IS_ADMIN, true);
        } else {
            sessionInfo.setAdmin(false);
            session.setAttribute(Consts.SESSION_IS_ADMIN, false);
        }
        session.setAttribute(Consts.SESSION_USER_UNO, user.getUserUno());
        session.setAttribute(Consts.SESSION_USER_ID, user.getUserId());
        session.setAttribute(Consts.SESSION_USER_NM, user.getUserNm());
        session.setAttribute(Consts.SESSION_USER_CL, user.getCrawling_limit());
        session.setAttribute(Consts.SESSION_USER_PL, user.getPercent_limit());
        session.setAttribute(Consts.LOGIN_SESSION, sessionInfo);

    }

}
