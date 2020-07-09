package com.tom.coupon.service.impl;

import com.tom.coupon.constant.ShopCode;
import com.tom.pojo.enetity.Result;
import com.tom.coupon.exception.CastException;
import com.tom.coupon.mapper.TradeCouponMapper;
import com.tom.pojo.pojo.TradeCoupon;
import com.tom.service.ICouponService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @File: ICouponServiceImpl
 * @Description:
 * @Author: tom
 * @Create: 2020-07-08 11:32
 **/
@DubboService
public class ICouponServiceImpl implements ICouponService {
    @Autowired
    private TradeCouponMapper tradeCouponMapper;

    /**
     * 根据优惠券id查找
     * @param couponId
     * @return
     */
    @Override
    public TradeCoupon findOne(Long couponId) {
        if (couponId == null) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeCouponMapper.selectByPrimaryKey(couponId);
    }

    /**
     * 更新优惠券状态
     * @param coupon
     * @return
     */
    @Override
    public Result updateCouponStatus(TradeCoupon coupon) {
        if (coupon == null || coupon.getCouponId() == null) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        tradeCouponMapper.updateByPrimaryKeySelective(coupon);
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(), ShopCode.SHOP_SUCCESS.getMessage());
    }
}
