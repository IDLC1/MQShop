package com.tom.service;

import com.tom.pojo.enetity.Result;
import com.tom.pojo.pojo.TradeGoods;
import com.tom.pojo.pojo.TradeGoodsNumberLog;

public interface IGoodsService {

    /**
     * 根据ID查找商品
     * @param goodsId
     * @return
     */
    TradeGoods findOne(Long goodsId);

    /**
     * 扣减库存
     * @param tradeGoodsNumberLog
     * @return
     */
    Result reduceGoodsNum(TradeGoodsNumberLog tradeGoodsNumberLog);
}
