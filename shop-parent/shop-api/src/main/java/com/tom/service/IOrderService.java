package com.tom.service;

import com.tom.entity.Result;
import com.tom.pojo.TradeOrder;

public interface IOrderService {

    /**
     * 用户下单接口
     * @param tradeOrder
     * @return
     */
    public Result confirmOrder(TradeOrder tradeOrder);
}
