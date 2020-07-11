package com.tom.goods.mq;

import com.alibaba.fastjson.JSON;
import com.tom.coupon.constant.ShopCode;
import com.tom.coupon.exception.CastException;
import com.tom.goods.mapper.TradeGoodsMapper;
import com.tom.goods.mapper.TradeGoodsNumberLogMapper;
import com.tom.goods.mapper.TradeMqConsumerLogMapper;
import com.tom.pojo.enetity.MQEntity;
import com.tom.pojo.pojo.TradeGoods;
import com.tom.pojo.pojo.TradeGoodsNumberLog;
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

import java.io.UnsupportedEncodingException;
import java.util.Date;

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

    @Autowired
    private TradeGoodsMapper goodsMapper;

    @Autowired
    private TradeGoodsNumberLogMapper goodsNumberLogMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        try {
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
                    // 先更新一次消息处理记录，记录为处理中
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
                        // TODO: 若三次处理都失败了呢？
                        log.info("并发修改，稍后处理");
                    }
                }
            } else {
                // 若消息记录里没有这条消息记录，则创建消费记录信息
                mqConsumerLog = new TradeMqConsumerLog();
                mqConsumerLog.setMsgTag(tags);
                mqConsumerLog.setMsgKey(keys);
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                mqConsumerLog.setMsgBody(body);
                mqConsumerLog.setConsumerTimes(0);

                // 存入数据库
                tradeMqConsumerLogMapper.insert(mqConsumerLog);
            }
            // 处理完成，回退库存
            MQEntity entity = JSON.parseObject(body, MQEntity.class);
            Long goodsId = entity.getGoodsId();
            // 获取这个要回退的商品
            TradeGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            // 修改商品库存数量
            goods.setGoodsNumber(goods.getGoodsNumber() + entity.getGoodsNum());
            // 更新商品
            goodsMapper.updateByPrimaryKey(goods);

            // 记录库存操作日志
            TradeGoodsNumberLog goodsNumberLog = new TradeGoodsNumberLog();
            goodsNumberLog.setOrderId(entity.getOrderId());
            goodsNumberLog.setGoodsId(goodsId);
            goodsNumberLog.setGoodsNumber(entity.getGoodsNum());
            goodsNumberLog.setLogTime(new Date());
            goodsNumberLogMapper.insert(goodsNumberLog);

            // 将消息的处理状态改为成功
            mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode());
            mqConsumerLog.setConsumerTimestamp(new Date());
            tradeMqConsumerLogMapper.updateByPrimaryKey(mqConsumerLog);
            log.info("回退库存成功");
        } catch (Exception e) {
            CastException.cast(ShopCode.SHOP_FAIL);
            e.printStackTrace();
\        }

    }
}
