package com.tom.user.service.impl;

import com.tom.coupon.constant.ShopCode;
import com.tom.pojo.enetity.Result;
import com.tom.coupon.exception.CastException;
import com.tom.service.IUserService;
import com.tom.user.mapper.TradeUserMapper;
import com.tom.user.mapper.TradeUserMoneyLogMapper;
import com.tom.pojo.pojo.TradeUser;
import com.tom.pojo.pojo.TradeUserMoneyLog;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @File: IUserServiceImpl
 * @Description:
 * @Author: tom
 * @Create: 2020-07-08 10:54
 **/
@DubboService
@Repository
public class IUserServiceImpl implements IUserService {
    @Autowired
    private TradeUserMapper tradeUserMapper;

    @Autowired
    private TradeUserMoneyLogMapper tradeUserMoneyLogMapper;

    @Override
    public TradeUser findOne(Long id) {
        if (id == null) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeUserMapper.selectByPrimaryKey(id);
    }

    /**
     * 更新余额
     * @param userMoneyLog
     * @return
     */
    @Override
    public Result updateMoneyPaid(TradeUserMoneyLog userMoneyLog) {
        // 校验参数合法
        if (userMoneyLog == null || userMoneyLog.getUserId() == null ||
                userMoneyLog.getOrderId() == null ||
                userMoneyLog.getUseMoney() == null ||
                userMoneyLog.getUseMoney().compareTo(BigDecimal.ZERO)<=0) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        // 查询订单余额使用日志
        Example example = new Example(TradeUserMoneyLog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", userMoneyLog.getOrderId());
        criteria.andEqualTo("userId", userMoneyLog.getUserId());
        int r = tradeUserMoneyLogMapper.selectCountByExample(example);

        TradeUser user = tradeUserMapper.selectByPrimaryKey(userMoneyLog.getUserId());
        // 扣减余额
        if (userMoneyLog.getMoneyLogType().equals(ShopCode.SHOP_USER_MONEY_PAID.getCode())) {
            // 若已经付过款了
            if (r > 0) {
                CastException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY);
            }
            // 若没有付款，则查出用户余额，并减去付款金额
            user.setUserMoney(new BigDecimal(user.getUserMoney()).subtract(userMoneyLog.getUseMoney()).longValue());
            tradeUserMapper.updateByPrimaryKeySelective(user);
        } else if (userMoneyLog.getMoneyLogType().equals(ShopCode.SHOP_USER_MONEY_REFUND.getCode())) {
            // 回退余额
            // 若没有这个订单记录
            if (r <= 0) {
                CastException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY);
            }
            // 防止多次退款
            Example example1 = new Example(TradeUserMoneyLog.class);
            Example.Criteria criteria1 = example1.createCriteria();
            criteria1.andEqualTo("orderId", userMoneyLog.getOrderId());
            criteria1.andEqualTo("userId", userMoneyLog.getUserId());
            criteria1.andEqualTo("moneyLogType", ShopCode.SHOP_USER_MONEY_REFUND.getCode());
            int res2 = tradeUserMoneyLogMapper.selectCountByExample(example1);
            // 若存在记录，则已经发生退款
            if (res2 > 0) {
                CastException.cast(ShopCode.SHOP_USER_MONEY_REFUND_ALREADY);
            }
            // 若不存在记录，则开始退款 用户的当前余额+本次订单的使用金额
            user.setUserMoney(new BigDecimal(user.getUserMoney()).add(userMoneyLog.getUseMoney()).longValue());
            tradeUserMapper.updateByPrimaryKey(user);
        }

        // 记录订单余额使用日志
        userMoneyLog.setCreateTime(new Date());
        tradeUserMoneyLogMapper.insert(userMoneyLog);
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(), ShopCode.SHOP_SUCCESS.getMessage());
    }
}
