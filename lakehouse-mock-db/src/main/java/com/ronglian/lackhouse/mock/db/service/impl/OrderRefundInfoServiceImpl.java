package com.ronglian.lackhouse.mock.db.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ronglian.lackhouse.mock.db.bean.OrderDetail;
import com.ronglian.lackhouse.mock.db.bean.OrderInfo;
import com.ronglian.lackhouse.mock.db.bean.OrderRefundInfo;
import com.ronglian.lackhouse.mock.db.common.GmallConstant;
import com.ronglian.lackhouse.mock.db.common.util.ParamUtil;
import com.ronglian.lackhouse.mock.db.common.util.RanOpt;
import com.ronglian.lackhouse.mock.db.common.util.RandomNumString;
import com.ronglian.lackhouse.mock.db.common.util.RandomOptionGroup;
import com.ronglian.lackhouse.mock.db.mapper.OrderRefundInfoMapper;
import com.ronglian.lackhouse.mock.db.service.OrderInfoService;
import com.ronglian.lackhouse.mock.db.service.OrderRefundInfoService;
import com.ronglian.lackhouse.mock.db.service.OrderStatusLogService;
import com.ronglian.lackehouse.mock.db.common.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 退单表 服务实现类
 * </p>
 *
 */
@Service
@Slf4j
public class OrderRefundInfoServiceImpl extends ServiceImpl<OrderRefundInfoMapper, OrderRefundInfo> implements OrderRefundInfoService {


    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    OrderStatusLogService orderStatusLogService;

    @Value("${mock.date}")
    String mockDate;


    @Value("${mock.refund.rate:30}")
    String ifRefundRate;


    RandomOptionGroup<String> refundTypeOptionGroup=new RandomOptionGroup(new RanOpt(GmallConstant.REFUND_TYPE_ONLY_MONEY,30),new RanOpt(GmallConstant.REFUND_TYPE_WITH_GOODS,60) );


    @Value("${mock.refund.reason-rate}")
    String refundReasonRate;

    public void  genRefundsOrFinish(Boolean ifClear){
        if(ifClear){
            remove(new QueryWrapper<OrderRefundInfo>());
        }
        Date date = ParamUtil.checkDate(mockDate);
        Integer ifRefundRateWeight = ParamUtil.checkRatioNum(this.ifRefundRate);
        RandomOptionGroup<Boolean> ifRefund=new RandomOptionGroup(new RanOpt(true,ifRefundRateWeight),new RanOpt(false,100-ifRefundRateWeight));
        Integer[] refundReasonRateArr = ParamUtil.checkRate(this.refundReasonRate,7);
        RandomOptionGroup<String>  refundReasonOptionGroup=new RandomOptionGroup(new RanOpt(GmallConstant.REFUND_REASON_BAD_GOODS,refundReasonRateArr[0]),
                new RanOpt(GmallConstant.REFUND_REASON_SIZE_ISSUE,refundReasonRateArr[1]),
                new RanOpt(GmallConstant.REFUND_REASON_SALE_OUT,refundReasonRateArr[2]),
                new RanOpt(GmallConstant.REFUND_REASON_MISTAKE,refundReasonRateArr[3]),
                new RanOpt(GmallConstant.REFUND_REASON_WRONG_DESC,refundReasonRateArr[4]),
                new RanOpt(GmallConstant.REFUND_REASON_NO_REASON,refundReasonRateArr[5]),
                new RanOpt(GmallConstant.REFUND_REASON_OTHER,refundReasonRateArr[6])
        );

        QueryWrapper<OrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.in("order_status",GmallConstant.ORDER_STATUS_PAID,GmallConstant.ORDER_STATUS_FINISH);
        orderInfoQueryWrapper.orderByAsc("id");
        List<OrderInfo> orderInfoList = orderInfoService.listWithDetail(orderInfoQueryWrapper);
        List<OrderRefundInfo> orderRefundInfoList=new ArrayList();
        List<OrderInfo> orderInfoListForUpdate=new ArrayList<>();
        if(orderInfoList.size()==0){
            log.warn ("没有需要退款或完结的订单！！ ");
            return;
        }
        for (OrderInfo orderInfo : orderInfoList) {
            if( ifRefund.getRandBoolValue() ){
                OrderRefundInfo orderRefundInfo = new OrderRefundInfo();
                orderRefundInfo.setOrderId(orderInfo.getId());
                OrderDetail orderDetail = orderInfo.getOrderDetailList().get(0);
                orderRefundInfo.setRefundAmount(  orderDetail.getOrderPrice().multiply(BigDecimal.valueOf(orderDetail.getSkuNum()) ) );
                orderRefundInfo.setSkuId(orderDetail.getSkuId());
                orderRefundInfo.setUserId(orderInfo.getUserId());
                orderRefundInfo.setRefundNum(orderDetail.getSkuNum());
                orderRefundInfo.setCreateTime(date);
                orderRefundInfo.setRefundReasonTxt("退款原因具体："+ RandomNumString.getRandNumString(0,9,10,""));
                orderRefundInfo.setRefundType(refundTypeOptionGroup.getRandStringValue());
                orderRefundInfo.setRefundReasonType(refundReasonOptionGroup.getRandStringValue());
                orderRefundInfoList.add(orderRefundInfo);

                orderInfo.setOrderStatus(GmallConstant.ORDER_STATUS_REFUND);
                orderInfoListForUpdate.add(orderInfo);
            }else {
                if(orderInfo.getOrderStatus().equals(GmallConstant.ORDER_STATUS_PAID)){
                    orderInfo.setOrderStatus(GmallConstant.ORDER_STATUS_FINISH);
                    orderInfoListForUpdate.add(orderInfo);
                }

            }

        }

        orderInfoService.updateOrderStatus(orderInfoListForUpdate);

        log.warn("共生成退款"+orderRefundInfoList.size()+"条");
        saveBatch(orderRefundInfoList);




    }
}
