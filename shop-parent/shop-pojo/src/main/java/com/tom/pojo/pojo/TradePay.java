package com.tom.pojo.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/****
 * @Author:tkmybatis
 * @Description:TradePay构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_pay")
@Data
public class TradePay implements Serializable{

	@Id
    @Column(name = "pay_id")
	private Long payId;//支付编号

    @Column(name = "order_id")
	private Long orderId;//订单编号

    @Column(name = "pay_amount")
	private BigDecimal payAmount;//支付金额

    @Column(name = "is_paid")
	private Integer isPaid;//是否已支付 1否 2是

}
