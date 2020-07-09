package com.tom.test;

import com.tom.pojo.pojo.TradeOrder;
import com.tom.order.OrderApplication;
import com.tom.service.IOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

/**
 * @File: OrderTest
 * @Description:
 * @Author: tom
 * @Create: 2020-07-09 14:14
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrderApplication.class)
public class OrderTest {

    @Autowired
    private IOrderService orderService;

    @Test
    public void confirmOrder() {
        Long coupouId = 478623985073139716L;
        Long goodsId = 345959443973935104L;
        Long userId = 345963634385633280L;

        TradeOrder order = new TradeOrder();

        order.setGoodsId(goodsId);
        order.setUserId(userId);
        order.setCouponId(coupouId);
        order.setAddress("中国");
        order.setGoodsNumber(1);
        order.setGoodsPrice(new BigDecimal(5000));
        order.setShippingFee(BigDecimal.ZERO);
        order.setOrderAmount(new BigDecimal(5000));
        order.setMoneyPaid(new BigDecimal(100)); // 使用余额

        orderService.confirmOrder(order);
    }
}
