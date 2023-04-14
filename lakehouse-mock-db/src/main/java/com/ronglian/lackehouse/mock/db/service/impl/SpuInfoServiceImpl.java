package com.ronglian.lackehouse.mock.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ronglian.lackehouse.mock.db.bean.SpuInfo;
import com.ronglian.lackehouse.mock.db.mapper.SpuInfoMapper;
import com.ronglian.lackehouse.mock.db.service.SpuInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 */
@Service
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfo> implements SpuInfoService {

}
