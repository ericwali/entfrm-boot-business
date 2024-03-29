package com.entfrm.biz.toolkit.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entfrm.biz.toolkit.entity.Column;
import com.entfrm.biz.toolkit.mapper.ColumnMapper;
import com.entfrm.biz.toolkit.service.ColumnService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务字段 服务层实现
 */
@Service
public class ColumnServiceImpl extends ServiceImpl<ColumnMapper, Column> implements ColumnService {

    @Override
    public List<Column> selectDbColumnsByName(String tableName) {
        return baseMapper.selectDbColumnsByName(tableName);
    }

    @Override
    public int deleteColumnByTableIds(String ids) {
        return baseMapper.deleteColumnByTableIds(Convert.toIntArray(ids));
    }
}
