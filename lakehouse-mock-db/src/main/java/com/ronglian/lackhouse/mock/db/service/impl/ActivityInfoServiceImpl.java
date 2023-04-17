package com.ronglian.lackhouse.mock.db.service.impl;

import com.ronglian.lackhouse.mock.db.bean.ActivityInfo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ronglian.lackhouse.mock.db.mapper.ActivityInfoMapper;
import com.ronglian.lackhouse.mock.db.service.ActivityInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-25
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {


}
