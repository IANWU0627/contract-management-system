package com.contracthub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.contracthub.mapper")
public class ContractManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContractManagementApplication.class, args);
    }
}
