package com.choco.smartlf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.choco.smartlf.mapper")
public class SmartlfApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartlfApplication.class, args);
    }

}
    