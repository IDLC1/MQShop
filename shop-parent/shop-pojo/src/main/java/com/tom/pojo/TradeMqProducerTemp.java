package com.tom.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/****
 * @Author:tkmybatis
 * @Description:TradeMqProducerTemp构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="trade_mq_producer_temp")
@Data
public class TradeMqProducerTemp implements Serializable{

	@Id
    @Column(name = "id")
	private String id;//

    @Column(name = "group_name")
	private String groupName;//

    @Column(name = "msg_topic")
	private String msgTopic;//

    @Column(name = "msg_tag")
	private String msgTag;//

    @Column(name = "msg_key")
	private String msgKey;//

    @Column(name = "msg_body")
	private String msgBody;//

    @Column(name = "msg_status")
	private Integer msgStatus;//0:未处理;1:已经处理

    @Column(name = "create_time")
	private Date createTime;//
}
