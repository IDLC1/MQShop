package com.tom.service;

import com.tom.entity.Result;
import com.tom.pojo.TradeCoupon;

public interface ICouponService {

    /**
     * 根据ID查找优惠券
     * @param couponId
     * @return
     */
    TradeCoupon findOne(Long couponId);

    Result updateCouponStatus(TradeCoupon coupon);
}
