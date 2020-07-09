package com.tom.pojo.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/****
 * @Author:tkmybatis
 * @Description:TradeCoupon构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_coupon")
@Data
public class TradeCoupon implements Serializable{

	@Id
    @Column(name = "coupon_id")
	private Long couponId;//优惠券ID

    @Column(name = "coupon_price")
	private BigDecimal couponPrice;//优惠券金额

    @Column(name = "user_id")
	private Long userId;//用户ID

    @Column(name = "order_id")
	private Long orderId;//订单ID

    @Column(name = "is_used")
	private Integer isUsed;//是否使用 0未使用 1已使用

    @Column(name = "used_time")
	private Date usedTime;//使用时间
}
