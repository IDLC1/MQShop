package com.tom.order.mapper;
import com.tom.pojo.pojo.TradeOrder;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:tkmybatis
 * @Description:TradeOrder的Dao
 * @Date 2019/6/14 0:12
 *****/
@Repository
public interface TradeOrderMapper extends Mapper<TradeOrder> {
}
