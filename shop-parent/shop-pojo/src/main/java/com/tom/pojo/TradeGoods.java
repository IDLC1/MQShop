package com.tom.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/****
 * @Author:tkmybatis
 * @Description:TradeGoods构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_goods")
@Data
public class TradeGoods implements Serializable{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goods_id")
	private Long goodsId;//

    @Column(name = "goods_name")
	private String goodsName;//商品名称

    @Column(name = "goods_number")
	private Integer goodsNumber;//商品库存

    @Column(name = "goods_price")
	private BigDecimal goodsPrice;//商品价格

    @Column(name = "goods_desc")
	private String goodsDesc;//商品描述

    @Column(name = "add_time")
	private Date addTime;//添加时间

}
