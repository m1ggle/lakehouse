package com.ronglian.lackehouse.mock.db.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ronglian.lackehouse.mock.db.bean.OrderInfo;
import com.ronglian.lackehouse.mock.db.bean.OrderStatusLog;
import com.ronglian.lackehouse.mock.db.mapper.OrderStatusLogMapper;
import com.ronglian.lackehouse.mock.db.service.OrderStatusLogService;
import com.ronglian.lackehouse.mock.db.common.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class OrderStatusLogServiceImpl extends ServiceImpl<OrderStatusLogMapper, OrderStatusLog> implements OrderStatusLogService {


    @Value("${mock.date}")
    String mockDate;


    public void  genOrderStatusLog(List<OrderInfo> orderInfoList){
        Date date = ParamUtil.checkDate(mockDate);

        List<OrderStatusLog> orderStatusLogList=new ArrayList<>();
        for (OrderInfo orderInfo : orderInfoList) {
            OrderStatusLog orderStatusLog = new OrderStatusLog();
            orderStatusLog.setOperateTime(date);
            orderStatusLog.setOrderStatus(orderInfo.getOrderStatus());
            orderStatusLog.setOrderId(orderInfo.getId());
            orderStatusLogList.add(orderStatusLog);
        }
        saveBatch(orderStatusLogList);

    }
}
