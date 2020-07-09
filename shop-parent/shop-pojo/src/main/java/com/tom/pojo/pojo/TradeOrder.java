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
 * @Description:TradeOrder构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_order")
@Data
public class TradeOrder implements Serializable{

	@Id
    @Column(name = "order_id")
	private Long orderId;//订单ID

    @Column(name = "user_id")
	private Long userId;//用户ID

    @Column(name = "order_status")
	private Integer orderStatus;//订单状态 0未确认 1已确认 2已取消 3无效 4退款

    @Column(name = "pay_status")
	private Integer payStatus;//支付状态 0未支付 1支付中 2已支付

    @Column(name = "shipping_status")
	private Integer shippingStatus;//发货状态 0未发货 1已发货 2已收货

    @Column(name = "address")
	private String address;//收货地址

    @Column(name = "consignee")
	private String consignee;//收货人

    @Column(name = "goods_id")
	private Long goodsId;//商品ID

    @Column(name = "goods_number")
	private Integer goodsNumber;//商品数量

    @Column(name = "goods_price")
	private BigDecimal goodsPrice;//商品价格

    @Column(name = "goods_amount")
	private Long goodsAmount;//商品总价

    @Column(name = "shipping_fee")
	private BigDecimal shippingFee;//运费

    @Column(name = "order_amount")
	private BigDecimal orderAmount;//订单价格

    @Column(name = "coupon_id")
	private Long couponId;//优惠券ID

    @Column(name = "coupon_paid")
	private BigDecimal couponPaid;//优惠券

    @Column(name = "money_paid")
	private BigDecimal moneyPaid;//已付金额

    @Column(name = "pay_amount")
	private BigDecimal payAmount;//支付金额

    @Column(name = "add_time")
	private Date addTime;//创建时间

    @Column(name = "confirm_time")
	private Date confirmTime;//订单确认时间

    @Column(name = "pay_time")
	private Date payTime;//支付时间
}
