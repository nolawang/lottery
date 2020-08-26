package com.wch.lottery.dao.mapper;

import com.wch.lottery.model.Issue;
import com.wch.lottery.model.IssueRecord;
import com.wch.lottery.model.dto.IssueRecordDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IssueRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(IssueRecord record);

    int insertSelective(IssueRecord record);

    IssueRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(IssueRecord record);

    int updateByPrimaryKey(IssueRecord record);

    int insertBatch(IssueRecord record);

    List<IssueRecord> selectByCondition(IssueRecord record);

    List<IssueRecord> selectNumByIssueNoAndCount(@Param("issueNo") int issueNo, @Param("count") int count,@Param("red")boolean red);

    List<IssueRecord> selectNumByAreaCondition(IssueRecordDTO issueRecordDTO);

    IssueRecord selectMaxMissRecord(@Param("red")boolean red);

    List<IssueRecord> selectByRange(@Param("issueNo") int issueNo,@Param("red")boolean red);
}