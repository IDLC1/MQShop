package com.tom.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/****
 * @Author:tkmybatis
 * @Description:TradeUser构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_user")
@Data
public class TradeUser implements Serializable{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
	private Long userId;//用户ID

    @Column(name = "user_name")
	private String userName;//用户姓名

    @Column(name = "user_password")
	private String userPassword;//用户密码

    @Column(name = "user_mobile")
	private String userMobile;//手机号

    @Column(name = "user_score")
	private Integer userScore;//积分

    @Column(name = "user_reg_time")
	private Date userRegTime;//注册时间

    @Column(name = "user_money")
	private Long userMoney;//用户余额
}
