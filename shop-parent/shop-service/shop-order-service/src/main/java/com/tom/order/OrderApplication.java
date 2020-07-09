package com.tom.order;

import com.tom.coupon.utils.IDWorker;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @File: CouponApplication
 * @Description:
 * @Author: tom
 * @Create: 2020-07-09 10:41
 **/
@SpringBootApplication
@EnableDubbo
@MapperScan(basePackages = {"com.tom.order.mapper"})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public IDWorker getBean(){
        return new IDWorker(1,1);
    }
}
