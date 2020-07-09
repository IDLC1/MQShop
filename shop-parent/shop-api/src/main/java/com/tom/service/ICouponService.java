package com.tom.service;

import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradeCoupon;

public interface ICouponService {

    /**
     * 根据ID查找优惠券
     * @param couponId
     * @return
     */
    TradeCoupon findOne(Long couponId);

    Result updateCouponStatus(TradeCoupon coupon);
}
