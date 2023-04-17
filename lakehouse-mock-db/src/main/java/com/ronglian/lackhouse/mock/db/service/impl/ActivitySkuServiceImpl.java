package com.ronglian.lackhouse.mock.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ronglian.lackhouse.mock.db.bean.ActivitySku;
import com.ronglian.lackhouse.mock.db.mapper.ActivitySkuMapper;
import com.ronglian.lackhouse.mock.db.service.ActivitySkuService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 活动参与商品 服务实现类
 * </p>
 *
 */
@Service
public class ActivitySkuServiceImpl extends ServiceImpl<ActivitySkuMapper, ActivitySku> implements ActivitySkuService {

}
