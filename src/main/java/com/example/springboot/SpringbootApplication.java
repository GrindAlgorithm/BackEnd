package com.example.springboot;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 시즌 자동 전환(SeasonScheduler)
public class SpringbootApplication {

    /** 서비스 기준 시간대 — 응답 DTO 들이 LocalDateTime 에 +09:00 을 붙여 내보내므로 반드시 일치해야 한다 */
    public static final String SERVICE_ZONE = "Asia/Seoul";

    static {
        // LocalDateTime.now()·LocalDate.now() 가 서버 OS 시간대를 따르지 않게 고정한다.
        // staging VM 이 UTC 라서 저장 시각이 9시간 이르게 표시되고(최근 활동 "10시간 전"),
        // 오늘 날짜 계산이 KST 0~9시에 하루 밀리던 문제. static 블록에 두어 main() 을 거치지 않는
        // @SpringBootTest 에서도 같은 시간대로 동작한다.
        TimeZone.setDefault(TimeZone.getTimeZone(SERVICE_ZONE));
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

}
