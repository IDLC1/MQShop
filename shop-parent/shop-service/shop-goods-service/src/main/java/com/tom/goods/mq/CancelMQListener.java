package com.tom.goods.mq;

import com.tom.coupon.constant.ShopCode;
import com.tom.goods.mapper.TradeMqConsumerLogMapper;
import com.tom.pojo.pojo.TradeMqConsumerLog;
import com.tom.pojo.pojo.TradeMqConsumerLogKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @File: CancelMQListener
 * @Description: 回退商品
 * @Author: tom
 * @Create: 2020-07-09 16:20
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}", consumerGroup = "${mq.order.consumer.group.name}", messageModel = MessageModel.BROADCASTING)
public class CancelMQListener implements RocketMQListener<MessageExt> {

    @Autowired
    private TradeMqConsumerLogMapper tradeMqConsumerLogMapper;

    @Value("${mq.order.consumer.group.name}")
    private String groupName;

    @SneakyThrows
    @Override
    public void onMessage(MessageExt messageExt) {
        // 解析消息内容
        String msgId = messageExt.getMsgId();
        String tags = messageExt.getTags();
        String keys = messageExt.getKeys();
        String body = new String(messageExt.getBody(), "UTF-8");
        // 查询消息消费记录
        TradeMqConsumerLogKey primaryKey = new TradeMqConsumerLogKey();
        primaryKey.setGroupName(groupName);
        primaryKey.setMsgKey(keys);
        primaryKey.setMsgTag(tags);
        TradeMqConsumerLog mqConsumerLog = tradeMqConsumerLogMapper.selectByPrimaryKey(primaryKey);

        // 若消息已经被消费过了
        if(mqConsumerLog != null) {
            // 先获取消息状态
            Integer status = mqConsumerLog.getConsumerStatus();
            // 若处理过，则直接放过
            if (ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode().equals(status)) {
                log.info("消息：【" + msgId + "】已经处理过");
            } else if (ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode().equals(status)){
                // 若消息正在处理中
                log.info("消息：【" + msgId + "】正在处理中");
            } else {
                // 若消息处理失败
                // 获得消息处理次数
                Integer times = mqConsumerLog.getConsumerTimes();
                if (times > 3) {
                    log.info("消息：【" + msgId + "】处理超过3次，不能再进行处理了");
                    return;
                }

                // 若重试，则重新开始处理
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                Example example = new Example(TradeMqConsumerLog.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("msgTag", mqConsumerLog.getMsgTag());
                criteria.andEqualTo("msg_key", mqConsumerLog.getMsgKey());
                criteria.andEqualTo("groupName", groupName);
                criteria.andEqualTo("consumerTimes", mqConsumerLog.getConsumerTimes());
                int res = tradeMqConsumerLogMapper.updateByExampleSelective(mqConsumerLog, example);
                if (res <= 0) {
                    // 未修改成功，有其他线程在并发修改
                    log.info("并发修改，稍后处理");
                }
            }
        } else {
            // 消息未被消费
        }
    }
}
