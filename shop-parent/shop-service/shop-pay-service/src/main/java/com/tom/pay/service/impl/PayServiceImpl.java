package com.tom.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.tom.coupon.constant.ShopCode;
import com.tom.coupon.exception.CastException;
import com.tom.coupon.utils.IDWorker;
import com.tom.pay.mapper.TradeMqProducerTempMapper;
import com.tom.pay.mapper.TradePayMapper;
import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradeMqProducerTemp;
import com.tom.pojo.pojo.TradePay;
import com.tom.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@DubboService
@Slf4j
@Service
public class PayServiceImpl implements IPayService {

    @Autowired
    private TradePayMapper payMapper;

    @Autowired
    private IDWorker idWorker;

    @Autowired
    private TradeMqProducerTempMapper tradeMqProducerTempMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Value("${mq.topic}")
    private String topic;

    @Value("${mq.pay.tag}")
    private String tag;

    @Value("${rocketmq.producer.group}")
    private String group;

    /**
     * 创建支付订单
     * @param pay
     */
    @Override
    public Result createPayment(TradePay pay) {
        // 判断订单的支付状态
        if (pay == null || pay.getOrderId() == null) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        Example example = new Example(TradePay.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", pay.getOrderId());
        criteria.andEqualTo("isPaid", ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
        int count = payMapper.selectCountByExample(example);
        // 若已存在支付的订单
        if (count > 0) {
            CastException.cast(ShopCode.SHOP_PAYMENT_IS_PAID);
        }
        // 设置订单状态未支付
        pay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_PAYING.getCode());
        // 保存支付订单
        pay.setPayId(idWorker.nextId());
        payMapper.insert(pay);

        log.info("生成支付订单成功");
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(), ShopCode.SHOP_SUCCESS.getMessage());
    }

    /**
     * 支付回调请求
     * @param tradePay
     * @return
     */
    @Override
    public Result callbackPayment(TradePay tradePay) {
        // 判断用户支付状态
        if (ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode().equals(tradePay.getIsPaid())) {
            // 更新支付订单状态为已支付，则从数据库中获取这条订单信息，并发送到mq
            Long payId = tradePay.getPayId();
            TradePay pay = payMapper.selectByPrimaryKey(payId);
            if (pay == null) {
                CastException.cast(ShopCode.SHOP_PAYMENT_NOT_FOUND);
            }
            pay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
            int res = payMapper.updateByPrimaryKeySelective(pay);
            // 若数据库更新成功
            if (res == 1) {
                // 创建支付成功的消息
                TradeMqProducerTemp tradeMqProducerTemp = new TradeMqProducerTemp();
                tradeMqProducerTemp.setId(String.valueOf(idWorker.nextId()));
                tradeMqProducerTemp.setMsgTopic(topic);
                tradeMqProducerTemp.setMsgTag(tag);
                tradeMqProducerTemp.setGroupName(group);
                tradeMqProducerTemp.setMsgKey(String.valueOf(pay.getPayId()));
                tradeMqProducerTemp.setMsgBody(JSON.toJSONString(pay));
                tradeMqProducerTemp.setCreateTime(new Date());

                // 将消息持久化数据库
                tradeMqProducerTempMapper.insert(tradeMqProducerTemp);
                log.info("将支付成功消息持久化到数据库");

                threadPoolTaskExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        // 将消息发送到MQ
                        SendResult result = null;
                        try {
                            result = sendMessage(topic, tag, tradeMqProducerTemp.getMsgKey(), tradeMqProducerTemp.getMsgBody());
                            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                                log.info("发送消息成功");
                                // 等待发送结果，若MQ接收到消息，删除发送成功的消息
                                tradeMqProducerTempMapper.deleteByPrimaryKey(tradeMqProducerTemp.getId());
                                log.info("持久化到数据库的消息删除");
                            }
                        } catch (Exception e) {
                            CastException.cast(ShopCode.SHOP_PAYMENT_PAY_ERROR);
                        }
                    }
                });
            }
        } else {
            return new Result(ShopCode.SHOP_FAIL.getSuccess(), ShopCode.SHOP_FAIL.getMessage());
        }
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(), ShopCode.SHOP_SUCCESS.getMessage());
    }

    /**
     * 发送订单支付成功消息到队列中
     * @param topic
     * @param tag
     * @param key
     * @param body
     */
    private SendResult sendMessage(String topic, String tag, String key, String body) throws Exception {
        if (StringUtils.isEmpty(topic)) {
            CastException.cast(ShopCode.SHOP_MQ_TOPIC_IS_EMPTY);
        }
        if (StringUtils.isEmpty(body)) {
            CastException.cast(ShopCode.SHOP_MQ_MESSAGE_BODY_IS_EMPTY);
        }
        Message message = new Message(topic, tag, key, body.getBytes());
        return rocketMQTemplate.getProducer().send(message);
    }
}
