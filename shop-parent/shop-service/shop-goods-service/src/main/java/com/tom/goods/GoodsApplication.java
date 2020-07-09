package com.tom.goods;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @File: CouponApplication
 * @Description:
 * @Author: tom
 * @Create: 2020-07-09 10:41
 **/
@SpringBootApplication
@EnableDubbo
@MapperScan(basePackages = {"com.tom.goods.mapper"})
public class GoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }
}
