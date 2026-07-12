package com.example.springboot.judge0;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Judge0 설정 활성화 + 비동기 채점(@Async) 활성화. */
@Configuration
@EnableConfigurationProperties(Judge0Properties.class)
@EnableAsync
public class Judge0Config {
}
