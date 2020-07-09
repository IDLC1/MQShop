package com.tom.pojo.enetity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MQEntity {

    private Long orderId;
    private Long couponId;
    private Long userId;
    private BigDecimal userMoney;
    private Long goodsId;
    private Integer goodsNum;
}
