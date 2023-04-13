package com.ronglian.lackehouse.mock.db.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 优惠券领用表
 * </p>
 *
 * @author yawei
 * @since 2023-04-13
 */
public class CouponUse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 购物券ID
     */
    private Long couponId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 购物券状态（1：未使用 2：已使用）
     */
    private String couponStatus;

    /**
     * 获取时间
     */
    private String getTime;

    /**
     * 使用时间
     */
    private String usingTime;

    /**
     * 支付时间
     */
    private String usedTime;

    /**
     * 过期时间
     */
    private String expireTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCouponStatus() {
        return couponStatus;
    }

    public void setCouponStatus(String couponStatus) {
        this.couponStatus = couponStatus;
    }

    public String getGetTime() {
        return getTime;
    }

    public void setGetTime(String getTime) {
        this.getTime = getTime;
    }

    public String getUsingTime() {
        return usingTime;
    }

    public void setUsingTime(String usingTime) {
        this.usingTime = usingTime;
    }

    public String getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(String usedTime) {
        this.usedTime = usedTime;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "CouponUse{" +
        "id=" + id +
        ", couponId=" + couponId +
        ", userId=" + userId +
        ", orderId=" + orderId +
        ", couponStatus=" + couponStatus +
        ", getTime=" + getTime +
        ", usingTime=" + usingTime +
        ", usedTime=" + usedTime +
        ", expireTime=" + expireTime +
        "}";
    }
}
