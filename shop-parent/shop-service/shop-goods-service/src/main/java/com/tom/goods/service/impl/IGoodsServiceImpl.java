package com.tom.goods.service.impl;

import com.tom.coupon.constant.ShopCode;
import com.tom.pojo.enetity.Result;
import com.tom.coupon.exception.CastException;
import com.tom.goods.mapper.TradeGoodsMapper;
import com.tom.goods.mapper.TradeGoodsNumberLogMapper;
import com.tom.pojo.pojo.TradeGoods;
import com.tom.pojo.pojo.TradeGoodsNumberLog;
import com.tom.service.IGoodsService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @File: IGoodsServiceImpl
 * @Description:
 * @Author: tom
 * @Create: 2020-07-08 10:46
 **/
@DubboService
public class IGoodsServiceImpl implements IGoodsService {

    @Autowired
    private TradeGoodsMapper tradeGoodsMapper;

    @Autowired
    private TradeGoodsNumberLogMapper tradeGoodsNumberLogMapper;

    @Override
    public TradeGoods findOne(Long goodsId) {
        if (goodsId == null) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeGoodsMapper.selectByPrimaryKey(goodsId);
    }

    @Override
    public Result reduceGoodsNum(TradeGoodsNumberLog tradeGoodsNumberLog) {
        if (tradeGoodsNumberLog == null || tradeGoodsNumberLog.getGoodsId() == null ||
                tradeGoodsNumberLog.getGoodsNumber() == null || tradeGoodsNumberLog.getGoodsNumber() < 0) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        TradeGoods goods = tradeGoodsMapper.selectByPrimaryKey(tradeGoodsNumberLog.getGoodsId());
        if (goods == null) {
            CastException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }
        // 库存不足
        if (goods.getGoodsNumber() < tradeGoodsNumberLog.getGoodsNumber()) {
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        goods.setGoodsNumber(goods.getGoodsNumber() - tradeGoodsNumberLog.getGoodsNumber());
        // 减库存
        tradeGoodsMapper.updateByPrimaryKey(goods);

        // 记录库存操作日志
        tradeGoodsNumberLog.setGoodsNumber(-(tradeGoodsNumberLog.getGoodsNumber()));
        tradeGoodsNumberLog.setLogTime(new Date());
        tradeGoodsNumberLogMapper.insert(tradeGoodsNumberLog);
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(), ShopCode.SHOP_SUCCESS.getMessage());
    }
}
