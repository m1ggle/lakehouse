package com.ronglian.lackhouse.mock.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ronglian.lackhouse.mock.db.bean.SkuInfo;
import com.ronglian.lackhouse.mock.db.mapper.SkuInfoMapper;
import com.ronglian.lackhouse.mock.db.service.SkuInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存单元表 服务实现类
 * </p>
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {

}
