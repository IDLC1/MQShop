package com.tom.goods.mapper;
import com.tom.pojo.pojo.TradeGoods;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:tkmybatis
 * @Description:TradeGoods的Dao
 * @Date 2019/6/14 0:12
 *****/
@Repository
public interface TradeGoodsMapper extends Mapper<TradeGoods> {
}
