package com.tom.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/****
 * @Author:tkmybatis
 * @Description:TradeGoodsNumberLog构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_goods_number_log")
@Data
public class TradeGoodsNumberLog implements Serializable{

    @Column(name = "goods_id")
	private Long goodsId;//商品ID

	@Id
    @Column(name = "order_id")
	private Long orderId;//订单ID

    @Column(name = "goods_number")
	private Integer goodsNumber;//库存数量

    @Column(name = "log_time")
	private Date logTime;//
}
