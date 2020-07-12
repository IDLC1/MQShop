package com.tom.test;

import com.tom.coupon.constant.ShopCode;
import com.tom.pay.PayServiceApplication;
import com.tom.pojo.pojo.TradePay;
import com.tom.service.IPayService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PayServiceApplication.class)
public class PayServiceTest {

    @Autowired
    private IPayService payService;

    @Test
    public void createPay() {
        long orderId = 479736605444280320L;
        TradePay tradePay = new TradePay();
        tradePay.setOrderId(orderId);
        tradePay.setPayAmount(new BigDecimal(4800.00));
        payService.createPayment(tradePay);
    }

    @Test
    public void callbackPayment() throws Exception {
        long payId = 479739158403883008L;
        long orderId = 479736605444280320L;

        TradePay tradePay = new TradePay();
        tradePay.setPayId(payId);
        tradePay.setOrderId(orderId);
        tradePay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());

        payService.callbackPayment(tradePay);

        System.in.read();
    }
}
