package com.ronglian.lackehouse.mock.db.bean;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 优惠券表
 * </p>
 *
 * @author yawei
 * @since 2023-04-13
 */
public class CouponInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物券编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 购物券名称
     */
    private String couponName;

    /**
     * 购物券类型 1 现金券 2 折扣券 3 满减券 4 满件打折券
     */
    private String couponType;

    /**
     * 满额数（3）
     */
    private BigDecimal conditionAmount;

    /**
     * 满件数（4）
     */
    private Long conditionNum;

    /**
     * 活动编号
     */
    private Long activityId;

    /**
     * 减金额（1 3）
     */
    private BigDecimal benefitAmount;

    /**
     * 折扣（2 4）
     */
    private BigDecimal benefitDiscount;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 范围类型 1、商品(spuid) 2、品类(三级分类id) 3、品牌
     */
    private String rangeType;

    /**
     * 商品id
     */
    private Integer spuId;

    /**
     * 品牌id
     */
    private Integer tmId;

    /**
     * 品类id
     */
    private Integer category3Id;

    /**
     * 最多领用次数
     */
    private Integer limitNum;

    /**
     * 已领用次数
     */
    private Integer takenCount;

    /**
     * 可以领取的开始日期
     */
    private String startTime;

    /**
     * 可以领取的结束日期
     */
    private String endTime;

    /**
     * 修改时间
     */
    private String operateTime;

    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 范围描述
     */
    private String rangeDesc;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public BigDecimal getConditionAmount() {
        return conditionAmount;
    }

    public void setConditionAmount(BigDecimal conditionAmount) {
        this.conditionAmount = conditionAmount;
    }

    public Long getConditionNum() {
        return conditionNum;
    }

    public void setConditionNum(Long conditionNum) {
        this.conditionNum = conditionNum;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public BigDecimal getBenefitAmount() {
        return benefitAmount;
    }

    public void setBenefitAmount(BigDecimal benefitAmount) {
        this.benefitAmount = benefitAmount;
    }

    public BigDecimal getBenefitDiscount() {
        return benefitDiscount;
    }

    public void setBenefitDiscount(BigDecimal benefitDiscount) {
        this.benefitDiscount = benefitDiscount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangeType) {
        this.rangeType = rangeType;
    }

    public Integer getSpuId() {
        return spuId;
    }

    public void setSpuId(Integer spuId) {
        this.spuId = spuId;
    }

    public Integer getTmId() {
        return tmId;
    }

    public void setTmId(Integer tmId) {
        this.tmId = tmId;
    }

    public Integer getCategory3Id() {
        return category3Id;
    }

    public void setCategory3Id(Integer category3Id) {
        this.category3Id = category3Id;
    }

    public Integer getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(Integer limitNum) {
        this.limitNum = limitNum;
    }

    public Integer getTakenCount() {
        return takenCount;
    }

    public void setTakenCount(Integer takenCount) {
        this.takenCount = takenCount;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public String getRangeDesc() {
        return rangeDesc;
    }

    public void setRangeDesc(String rangeDesc) {
        this.rangeDesc = rangeDesc;
    }

    @Override
    public String toString() {
        return "CouponInfo{" +
        "id=" + id +
        ", couponName=" + couponName +
        ", couponType=" + couponType +
        ", conditionAmount=" + conditionAmount +
        ", conditionNum=" + conditionNum +
        ", activityId=" + activityId +
        ", benefitAmount=" + benefitAmount +
        ", benefitDiscount=" + benefitDiscount +
        ", createTime=" + createTime +
        ", rangeType=" + rangeType +
        ", spuId=" + spuId +
        ", tmId=" + tmId +
        ", category3Id=" + category3Id +
        ", limitNum=" + limitNum +
        ", takenCount=" + takenCount +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", operateTime=" + operateTime +
        ", expireTime=" + expireTime +
        ", rangeDesc=" + rangeDesc +
        "}";
    }
}
