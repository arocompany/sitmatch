package com.nex.user.controller;

import com.nex.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;

@RequiredArgsConstructor // final 객체를 Constructor Injection 해줌. (Autowired 역할)
class UserControllerTest {

    @Test
    void test() {
        Timestamp pwModifyDt = Timestamp.valueOf("2023-02-21 19:20:30.0");
        Timestamp nowDt = new Timestamp(new Date().getTime());

        System.out.println("pwModifyDt : "+ pwModifyDt);
        System.out.println("nowDt : "+ nowDt);

        Long difftime = nowDt.getTime() - pwModifyDt.getTime();
        long diffday = difftime / 1000 / (24*60*60);

        System.out.println("difftime : " + difftime);
        System.out.println("diffday : " + diffday);

    }
}