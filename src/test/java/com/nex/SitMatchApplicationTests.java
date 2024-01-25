package com.nex;

import com.nex.batch.ScheduleTasks;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RequiredArgsConstructor
class SitMatchApplicationTests {

    private final ScheduleTasks scheduleTasks;


    @Test
    void contextLoads() {
//        scheduleTasks.trackingTask();
    }

}
