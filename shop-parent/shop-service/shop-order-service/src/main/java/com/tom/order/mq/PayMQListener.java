package com.tom.order.mq;

import com.alibaba.fastjson.JSON;
import com.tom.coupon.constant.ShopCode;
import com.tom.order.mapper.TradeOrderMapper;
import com.tom.pojo.pojo.TradeOrder;
import com.tom.pojo.pojo.TradePay;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.pay.topic}", consumerGroup = "${mq.pay.consumer.group.name}", messageModel = MessageModel.BROADCASTING)
public class PayMQListener implements RocketMQListener<MessageExt> {

    @Autowired
    private TradeOrderMapper orderMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        log.info("接收到支付成功消息");

        try {
            // 解析消息内容
            String body = new String(messageExt.getBody(), "UTF-8");
            TradePay tradePay = JSON.parseObject(body, TradePay.class);
            // 根据订单ID查询订单对象
            TradeOrder tradeOrder = orderMapper.selectByPrimaryKey(tradePay);
            // 更改订单支付状态为已支付
            tradeOrder.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
            // 更新订单数据到数据库
            orderMapper.updateByPrimaryKey(tradeOrder);
            log.info("订单状态更新：已支付");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
