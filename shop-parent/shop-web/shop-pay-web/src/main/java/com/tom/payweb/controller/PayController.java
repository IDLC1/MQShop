package com.tom.payweb.controller;


import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradePay;
import com.tom.service.IPayService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {

    @DubboReference
    private IPayService payService;

    @RequestMapping("/payment")
    public Result createPayment(@RequestBody TradePay tradePay) {
        return payService.createPayment(tradePay);
    }

    @RequestMapping("/callbackpay")
    public Result callBackPayment(@RequestBody TradePay tradePay) {
        return payService.callbackPayment(tradePay);
    }
}
