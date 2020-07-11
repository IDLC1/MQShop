package com.tom.order.mq;

import com.alibaba.fastjson.JSON;
import com.tom.coupon.constant.ShopCode;
import com.tom.order.mapper.TradeOrderMapper;
import com.tom.pojo.enetity.MQEntity;
import com.tom.pojo.pojo.TradeOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @File: CancelMQListener
 * @Description: 回退优惠券
 * @Author: tom
 * @Create: 2020-07-09 16:20
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}", consumerGroup = "${mq.order.consumer.group.name}", messageModel = MessageModel.BROADCASTING)
public class CancelMQListener implements RocketMQListener<MessageExt> {

    @Autowired
    private TradeOrderMapper orderMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        // 解析消息内容
        try {
            String body = new String(messageExt.getBody(), "UTF-8");
            MQEntity entity = JSON.parseObject(body, MQEntity.class);
            log.info("接收到消息");

            // 查询预订单
            TradeOrder order = orderMapper.selectByPrimaryKey(entity.getOrderId());
            // 更新订单状态
            order.setOrderStatus(ShopCode.SHOP_ORDER_CANCEL.getCode());
            orderMapper.updateByPrimaryKey(order);
            log.info("取消预订单成功");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.info("取消预订单失败");
        }
    }
}
