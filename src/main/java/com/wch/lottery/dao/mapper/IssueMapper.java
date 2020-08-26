package com.wch.lottery.dao.mapper;

import com.wch.lottery.model.Issue;

import java.util.List;

public interface IssueMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Issue record);

    int insertSelective(Issue record);

    Issue selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Issue record);

    int updateByPrimaryKey(Issue record);

    Issue selectMaxIssue();

    List<Issue> selectByCondition(Issue issue);
}