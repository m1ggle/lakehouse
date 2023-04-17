package com.ronglian.lackhouse.mock.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ronglian.lackhouse.mock.db.bean.CommentInfo;
import com.ronglian.lackhouse.mock.db.bean.OrderDetail;
import com.ronglian.lackhouse.mock.db.bean.OrderInfo;
import com.ronglian.lackhouse.mock.db.bean.SkuInfo;
import com.ronglian.lackhouse.mock.db.common.GmallConstant;
import com.ronglian.lackhouse.mock.db.common.util.*;
import com.ronglian.lackhouse.mock.db.mapper.CommentInfoMapper;
import com.ronglian.lackhouse.mock.db.mapper.SkuInfoMapper;
import com.ronglian.lackhouse.mock.db.mapper.UserInfoMapper;
import com.ronglian.lackhouse.mock.db.service.CommentInfoService;
import com.ronglian.lackhouse.mock.db.service.OrderInfoService;
import com.ronglian.lackehouse.mock.db.common.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 商品评论表 服务实现类
 * </p>
 *
 */
@Service
@Slf4j
public class CommentInfoServiceImpl extends ServiceImpl<CommentInfoMapper, CommentInfo> implements CommentInfoService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    OrderInfoService orderInfoService;

    @Value("${mock.date}")
    String mockDate;

    @Value("${mock.comment.appraise-rate:30:10:10:50}")
    String appraiseRate;

    public  void genComments( Boolean ifClear){
        if(ifClear){
            remove(new QueryWrapper<>());
        }



        Integer userTotal = userInfoMapper.selectCount(new QueryWrapper<>());

        List<CommentInfo> commentInfoList= new ArrayList<>();
        List<OrderInfo> orderInfoFinishList = orderInfoService.listWithDetail(new QueryWrapper<OrderInfo>().eq("order_status", GmallConstant.ORDER_STATUS_FINISH),true);
        for (OrderInfo orderInfo : orderInfoFinishList) {
            for (OrderDetail orderDetail : orderInfo.getOrderDetailList()){
                Long userId = RandomNum.getRandInt(1, userTotal)+0L;
                commentInfoList.add(initCommentInfo(orderDetail.getSkuInfo(),orderInfo,  userId)) ;
            }
        }
        log.warn("共生成评价"+commentInfoList.size()+"条");
          saveBatch(commentInfoList,100);
    }

    public  CommentInfo initCommentInfo(SkuInfo skuInfo, OrderInfo orderInfo, Long userId  ){
        Date date = ParamUtil.checkDate(mockDate);
        Integer[] appraiseRateWeight = ParamUtil.checkRate(this.appraiseRate,4);
        RandomOptionGroup<String> appraiseOptionGroup=new RandomOptionGroup(new RanOpt(GmallConstant.APPRAISE_GOOD,appraiseRateWeight[0]),
                new RanOpt(GmallConstant.APPRAISE_SOSO,appraiseRateWeight[1]),new RanOpt(GmallConstant.APPRAISE_BAD,appraiseRateWeight[2]),new RanOpt(GmallConstant.APPRAISE_AUTO,appraiseRateWeight[3]) );


        CommentInfo commentInfo = new CommentInfo();
        commentInfo.setOrderId(orderInfo.getId());
        commentInfo.setSkuId(skuInfo.getId());
        commentInfo.setSpuId(skuInfo.getSpuId());
        commentInfo.setUserId(userId);
        commentInfo.setCommentTxt("评论内容："+ RandomNumString.getRandNumString(1,9,50,""));
        commentInfo.setCreateTime(date);
        commentInfo.setAppraise(appraiseOptionGroup.getRandStringValue());
        return commentInfo;

    }
}
