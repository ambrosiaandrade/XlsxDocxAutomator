package com.ambrosiaandrade.exceldocxautomator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ExcelDocxAutomatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelDocxAutomatorApplication.class, args);
    }

}