package com.wch.lottery;

import com.wch.lottery.config.DataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@MapperScan("com.wch.lottery.dao.mapper")
@SpringBootApplication
public class LotteryServer {

    public static void main(String[] args) {
        SpringApplication.run(LotteryServer.class, args);
    }
}
