package com.tom.pay.mapper;
import com.tom.pojo.pojo.TradePay;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:shenkunlin
 * @Description:TradePay的Dao
 * @Date 2019/6/14 0:12
 *****/
@Repository
public interface TradePayMapper extends Mapper<TradePay> {
}
