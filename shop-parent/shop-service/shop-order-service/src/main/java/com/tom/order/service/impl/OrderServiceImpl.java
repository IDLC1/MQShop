package com.tom.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.tom.coupon.constant.ShopCode;
import com.tom.pojo.enetity.MQEntity;
import com.tom.pojo.enetity.Result;
import com.tom.coupon.exception.CastException;
import com.tom.pojo.pojo.*;
import com.tom.service.IUserService;
import com.tom.coupon.utils.IDWorker;
import com.tom.order.mapper.TradeOrderMapper;
import com.tom.service.ICouponService;
import com.tom.service.IGoodsService;
import com.tom.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @File: OrderServiceImpl
 * @Description:
 * @Author: tom
 * @Create: 2020-07-08 10:31
 **/
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @DubboReference
    private IGoodsService goodsService;

    @DubboReference
    private IUserService userService;

    @DubboReference
    private ICouponService couponService;

    @Autowired
    private IDWorker idWorker;

    @Autowired
    private TradeOrderMapper orderMapper;

    @Value("${mq.order.topic}")
    private String topic;

    @Value("${mq.order.tag.cancel}")
    private String tag;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 用户下单
     * @param order
     * @return
     */
    @Override
    public Result confirmOrder(TradeOrder order) {
        // 校验订单
        checkOrder(order);

        // 生成预订单
        savePreOrder(order);
        try {
            // 扣减库存
            reduceGoodsNum(order);
            // 扣减优惠券
            updateCouponStatus(order);
            // 扣减余额
            reduceMoneyPaid(order);

            // 模拟异常
            CastException.cast(ShopCode.SHOP_FAIL);

            // 确认订单
            updateOrderStatus(order);
            // 返回成功状态
            return new Result(ShopCode.SHOP_SUCCESS.getSuccess(), ShopCode.SHOP_SUCCESS.getMessage());
        } catch (Exception e) {
            // 确认订单失败，发送消息
            MQEntity mqEntity = new MQEntity();
            mqEntity.setOrderId(order.getOrderId());
            mqEntity.setUserId(order.getUserId());
            mqEntity.setUserMoney(order.getMoneyPaid());
            mqEntity.setGoodsId(order.getGoodsId());
            mqEntity.setGoodsNum(order.getGoodsNumber());
            mqEntity.setCouponId(order.getCouponId());

            // 发送失败消息
            sendCancelOrder(topic, tag, mqEntity.getOrderId().toString(), JSON.toJSONString(mqEntity));
            return new Result(ShopCode.SHOP_FAIL.getSuccess(), ShopCode.SHOP_FAIL.getMessage());
        }
    }

    /**
     * 发送订单确认失败消息
     * @param topic
     * @param tag
     * @param Keys
     * @param body
     */
    private void sendCancelOrder(String topic, String tag, String Keys, String body) {
        Message message = new Message(topic, tag, Keys, body.getBytes());
        try {
//            rocketMQTemplate.syncSend(topic, message);
            rocketMQTemplate.getProducer().send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 确认订单
     * @param order
     */
    private void updateOrderStatus(TradeOrder order) {
        order.setOrderStatus(ShopCode.SHOP_ORDER_CONFIRM.getCode());
        order.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());
        order.setConfirmTime(new Date());
        int r = orderMapper.updateByPrimaryKey(order);
        if (r < 0) {
            CastException.cast(ShopCode.SHOP_ORDER_CONFIRM_FAIL);
        }
        log.info("订单：【" + order.getOrderId() + "】确认订单成功");
    }

    /**
     * 扣减余额
     * @param order
     */
    private void reduceMoneyPaid(TradeOrder order) {
        if (order.getMoneyPaid() != null && BigDecimal.ZERO.compareTo(order.getMoneyPaid()) == -1) {
            // 构建付款订单操作日志
            TradeUserMoneyLog userMoneyLog = new TradeUserMoneyLog();
            userMoneyLog.setOrderId(order.getOrderId());
            userMoneyLog.setUserId(order.getUserId());
            userMoneyLog.setUseMoney(order.getMoneyPaid());
            userMoneyLog.setMoneyLogType(ShopCode.SHOP_USER_MONEY_PAID.getCode());

            // 发送日志，更新余额
            Result result = userService.updateMoneyPaid(userMoneyLog);
            if (result.getSuccess().equals(ShopCode.SHOP_FAIL.getSuccess())) {
                CastException.cast(ShopCode.SHOP_USER_MONEY_REDUCE_FAIL);
            }
            log.info("订单：【" + order.getOrderId() + "】,扣减余额成功");
        }
    }

    /**
     * 扣减优惠券
     * @param order
     */
    private void updateCouponStatus(TradeOrder order) {
        if (order.getCouponId() != null) {
            // 查找优惠券
            TradeCoupon coupon = couponService.findOne(order.getCouponId());
            // 修改优惠券
            coupon.setOrderId(order.getOrderId());
            coupon.setUsedTime(new Date());
            coupon.setIsUsed(ShopCode.SHOP_COUPON_ISUSED.getCode());

            // 更新优惠券状态
            Result result = couponService.updateCouponStatus(coupon);
            if (ShopCode.SHOP_COUPON_USE_FAIL.equals(result.getSuccess())) {
                CastException.cast(ShopCode.SHOP_COUPON_USE_FAIL);
            }
            log.info("订单：【" + order.getOrderId() + "】使用成功");
        }
    }

    /**
     * 扣减库存
     * @param tradeOrder
     */
    private void reduceGoodsNum(TradeOrder tradeOrder) {
        /**
         * 订单ID  商品ID  商品数量
         */
        TradeGoodsNumberLog tradeGoodsNumberLog = new TradeGoodsNumberLog();
        tradeGoodsNumberLog.setOrderId(tradeOrder.getOrderId());
        tradeGoodsNumberLog.setGoodsId(tradeOrder.getGoodsId());
        tradeGoodsNumberLog.setGoodsNumber(tradeOrder.getGoodsNumber());
        Result result = goodsService.reduceGoodsNum(tradeGoodsNumberLog);
        if (ShopCode.SHOP_FAIL.getSuccess().equals(result.getSuccess())) {
            CastException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
        }
        log.info("订单：【" + tradeOrder.getOrderId() + "】扣减库存成功");
    }

    /**
     * 校验订单
     * @param order
     */
    private void checkOrder(TradeOrder order) {
        // 校验订单是否存在
        if (order == null) {
            CastException.cast(ShopCode.SHOP_ORDER_INVALID);
        }
        // 校验订单中的商品是否存在
        TradeGoods goods = goodsService.findOne(order.getGoodsId());
        if (goods == null) {
            CastException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }
        // 检验下单用户是否存在
        TradeUser user = userService.findOne(order.getUserId());
        if (user == null) {
            CastException.cast(ShopCode.SHOP_USER_NO_EXIST);
        }
        // 校验订单金额是否合法
        //4.校验商品单价是否合法
        if (order.getGoodsPrice().compareTo(goods.getGoodsPrice()) != 0) {
            CastException.cast(ShopCode.SHOP_GOODS_PRICE_INVALID);
        }
        //5.校验订单商品数量是否合法
        if (order.getGoodsNumber() >= goods.getGoodsNumber()) {
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }

        log.info("校验订单通过");
    }

    /**
     * 生成预订单
     * @param order
     * @return
     */
    private Long savePreOrder(TradeOrder order) {
        // 设置订单状态不可见
        order.setOrderStatus(ShopCode.SHOP_ORDER_NO_CONFIRM.getCode());

        // 设置订单ID
        order.setOrderId(idWorker.nextId());

        // 核算运费
        BigDecimal shoppingFee = calculateShoppingFee(order.getOrderAmount());
        if (order.getShippingFee().compareTo(shoppingFee) != 0) {
            CastException.cast(ShopCode.SHOP_ORDER_SHIPPINGFEE_INVALID);
        }

        // 核算订单总价
        BigDecimal orderAmount = order.getGoodsPrice().multiply(new BigDecimal(order.getGoodsNumber()));
        orderAmount.add(shoppingFee);
        if (orderAmount.compareTo(order.getOrderAmount()) != 0) {
            CastException.cast(ShopCode.SHOP_ORDERAMOUNT_INVALID);
        }

        // 判断优惠券是否合法
        Long couponId = order.getCouponId();
        // 若使用优惠券
        if (couponId != null) {
            TradeCoupon coupon = couponService.findOne(couponId);
            // 优惠券不存在
            if (coupon == null) {
                CastException.cast(ShopCode.SHOP_COUPON_NO_EXIST);
            }
            // 优惠券已经使用
            if (ShopCode.SHOP_COUPON_ISUSED.getCode().toString()
                    .equals(coupon.getIsUsed().toString())) {
                CastException.cast(ShopCode.SHOP_COUPON_ISUSED);
            }
            // 设置优惠券金额
            order.setCouponPaid(coupon.getCouponPrice());
        } else {
            // 若没有使用优惠券
            order.setCouponPaid(BigDecimal.ZERO);
        }

        // 是否使用余额
        BigDecimal moneyPaid = order.getMoneyPaid();
        if (moneyPaid != null) {
            // 判断余额是否合法
            int result = BigDecimal.ZERO.compareTo(moneyPaid);
            // 若余额小于0
            if (result == 1) {
                CastException.cast(ShopCode.SHOP_MONEY_PAID_LESS_ZERO);
            } else if (result == -1) {
                // 若余额大于0，则比较当前这个用户的余额
                // 订单中的余额不能大于用户的余额
                TradeUser user = userService.findOne(order.getUserId());
                if (user == null) {
                    CastException.cast(ShopCode.SHOP_USER_NO_EXIST);
                }
                if (moneyPaid.compareTo(new BigDecimal(user.getUserMoney())) == 1) {
                    CastException.cast(ShopCode.SHOP_MONEY_PAID_INVALID);
                }
            }
        } else {
            order.setMoneyPaid(BigDecimal.ZERO);
        }

        // 核算订单总价 订单总金额 - 余额 - 优惠券金额
        BigDecimal payAomunt = order.getOrderAmount().subtract(order.getMoneyPaid()).subtract(order.getCouponPaid());
        order.setPayAmount(payAomunt);

        // 设置订单时间
        order.setAddTime(new Date());

        // 保存订单
        orderMapper.insert(order);
        log.info("生成预订单成功：【" + order.getOrderId() + "】");

        return order.getOrderId();
    }

    private BigDecimal calculateShoppingFee(BigDecimal orderAmount) {
        if (orderAmount.compareTo(new BigDecimal(100)) == 1) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(10);
        }
    }
}
