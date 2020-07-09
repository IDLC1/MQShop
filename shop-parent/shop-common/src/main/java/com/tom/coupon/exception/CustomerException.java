package com.tom.coupon.exception;

import com.tom.coupon.constant.ShopCode;

/**
 * 自定义异常
 */
public class CustomerException extends RuntimeException{

    private ShopCode shopCode;

    public CustomerException(ShopCode shopCode) {
        this.shopCode = shopCode;
    }
}
