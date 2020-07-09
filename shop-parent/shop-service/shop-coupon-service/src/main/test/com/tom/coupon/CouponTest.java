package com.tom.coupon;

import com.tom.coupon.mapper.TradeCouponMapper;
import com.tom.coupon.utils.IDWorker;
import com.tom.pojo.pojo.TradeCoupon;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @File: CouponTest
 * @Description:
 * @Author: tom
 * @Create: 2020-07-09 15:45
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CouponApplication.class)
@Slf4j
public class CouponTest {

    @Autowired
    private TradeCouponMapper couponMapper;

    @Autowired
    private IDWorker idWorker;

    @Test
    public void createBatchCoupon() {
        for (int i = 0; i < 10; i++) {
            TradeCoupon coupon = new TradeCoupon();
            coupon.setCouponId(idWorker.nextId());
            coupon.setUserId(345963634385633280L);
            coupon.setIsUsed(0);
            coupon.setCouponPrice(new BigDecimal(100));
            log.info(coupon.toString());
            couponMapper.insert(coupon);
        }
    }
}
