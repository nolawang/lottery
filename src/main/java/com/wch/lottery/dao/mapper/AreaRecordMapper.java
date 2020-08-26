package com.wch.lottery.dao.mapper;

import com.wch.lottery.model.AreaRecord;

public interface AreaRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AreaRecord record);

    int insertSelective(AreaRecord record);

    AreaRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(AreaRecord record);

    int updateByPrimaryKey(AreaRecord record);
}