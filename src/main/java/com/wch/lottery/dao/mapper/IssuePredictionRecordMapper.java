package com.wch.lottery.dao.mapper;

import com.wch.lottery.model.IssuePredictionRecord;

import java.util.List;

public interface IssuePredictionRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(IssuePredictionRecord record);

    int insertSelective(IssuePredictionRecord record);

    IssuePredictionRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(IssuePredictionRecord record);

    int updateByPrimaryKey(IssuePredictionRecord record);

    List<IssuePredictionRecord> selectByCondition(IssuePredictionRecord record);
}