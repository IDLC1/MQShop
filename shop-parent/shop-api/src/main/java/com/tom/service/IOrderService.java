package com.tom.service;

import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradeOrder;

public interface IOrderService {

    /**
     * 用户下单接口
     * @param tradeOrder
     * @return
     */
    public Result confirmOrder(TradeOrder tradeOrder);
}
