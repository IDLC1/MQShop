package com.tom.pojo.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/****
 * @Author:tkmybatis
 * @Description:TradeMqConsumerLog构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_mq_consumer_log")
@Data
public class TradeMqConsumerLog implements Serializable{

    @Column(name = "msg_id")
	private String msgId;//

    @Column(name = "group_name")
	private String groupName;//

	@Id
    @Column(name = "msg_tag")
	private String msgTag;//

    @Column(name = "msg_key")
	private String msgKey;//

    @Column(name = "msg_body")
	private String msgBody;//

    @Column(name = "consumer_status")
	private Integer consumerStatus;//0:正在处理;1:处理成功;2:处理失败

    @Column(name = "consumer_times")
	private Integer consumerTimes;//

    @Column(name = "consumer_timestamp")
	private Date consumerTimestamp;//

    @Column(name = "remark")
	private String remark;//

}
