package com.wch.lottery.dao.mapper;

import com.wch.lottery.model.AreaPredictionRecord;

import java.util.List;

public interface AreaPredictionRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AreaPredictionRecord record);

    int insertSelective(AreaPredictionRecord record);

    AreaPredictionRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(AreaPredictionRecord record);

    int updateByPrimaryKey(AreaPredictionRecord record);

    List<AreaPredictionRecord> selectByCondition(AreaPredictionRecord record);
}