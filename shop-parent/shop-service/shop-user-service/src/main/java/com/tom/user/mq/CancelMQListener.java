package com.tom.user.mq;

import com.alibaba.fastjson.JSON;
import com.tom.coupon.constant.ShopCode;
import com.tom.pojo.enetity.MQEntity;
import com.tom.pojo.pojo.TradeUserMoneyLog;
import com.tom.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @File: CancelMQListener
 * @Description: 回退用户余额
 * @Author: tom
 * @Create: 2020-07-09 16:20
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}", consumerGroup = "${mq.order.consumer.group.name}", messageModel = MessageModel.BROADCASTING)
public class CancelMQListener implements RocketMQListener<MessageExt> {

    @Autowired
    private IUserService userService;

    @Override
    public void onMessage(MessageExt messageExt) {
        try {
            // 解析消息
            String body = new String(messageExt.getBody(), "UTF-8");
            MQEntity entity = JSON.parseObject(body, MQEntity.class);
            log.info("接收到消息");

            if (entity.getUserMoney() != null && BigDecimal.ZERO.compareTo(entity.getUserMoney()) < 0) {
                TradeUserMoneyLog moneyLog = new TradeUserMoneyLog();
                moneyLog.setUserId(entity.getUserId());
                moneyLog.setOrderId(entity.getOrderId());
                moneyLog.setMoneyLogType(ShopCode.SHOP_USER_MONEY_REFUND.getCode());
                moneyLog.setUseMoney(entity.getUserMoney());

                // 调用业务层进行余额修改
                userService.updateMoneyPaid(moneyLog);
                log.info("余额回退成功");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("余额回退失败");
        }
    }
}
