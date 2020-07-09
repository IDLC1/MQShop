package com.tom.coupon.mapper;
import com.tom.pojo.pojo.TradeCoupon;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/****
 * @Author:tkmybatis
 * @Description:TradeCoupon的Dao
 * @Date 2019/6/14 0:12
 *****/
@Repository
public interface TradeCouponMapper extends Mapper<TradeCoupon>, MySqlMapper<TradeCouponMapper> {
}
