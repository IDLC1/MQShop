package com.tom.orderweb.controller;

import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradeOrder;
import com.tom.service.IOrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @DubboReference
    private IOrderService orderService;

    @RequestMapping("/confirm")
    public Result confirmOrder(@RequestBody TradeOrder order) {
        return orderService.confirmOrder(order);
    }
}
