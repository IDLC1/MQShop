package com.tom.service;

import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradePay;

/**
 * 支付接口
 */
public interface IPayService {

    public Result createPayment(TradePay pay);

    public Result callbackPayment(TradePay pay);
}
