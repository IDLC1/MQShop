package com.tom.service;

import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradeUser;
import com.tom.pojo.pojo.TradeUserMoneyLog;

public interface IUserService {

    /**
     * 根据用户id找用户
     * @param id
     * @return
     */
    TradeUser findOne(Long id);

    /**
     * 更新用户余额
     * @param order
     * @return
     */
    Result updateMoneyPaid(TradeUserMoneyLog order);
}
