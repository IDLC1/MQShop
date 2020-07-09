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
 * @Description:TradeUserMoneyLog构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_user_money_log")
@Data
public class TradeUserMoneyLog implements Serializable{

	@Id
    @Column(name = "user_id")
	private Long userId;//用户ID

    @Column(name = "order_id")
	private Long orderId;//订单ID

    @Column(name = "money_log_type")
	private Integer moneyLogType;//日志类型 1订单付款 2 订单退款

    @Column(name = "use_money")
	private BigDecimal useMoney;//

    @Column(name = "create_time")
	private Date createTime;//日志时间

}
