package com.wch.lottery.service;

import com.wch.lottery.model.Issue;
import com.wch.lottery.model.IssueRecord;
import com.wch.lottery.model.body.IssueBody;

import java.util.List;

public interface IssueService {

    Long add(IssueBody issueBody)throws Exception;

    int update(IssueBody issueBody)throws Exception;

    /**
     * 根据期号预测
     * @param issueNo 期号
     * @param count 预测数量，如果传0默认是5条
     * @return
     * @throws Exception
     */
    int[][] predict(int issueNo,int count,boolean isAccurate,int[] cases)throws Exception;


    /**
     * 根据历史分布区间预测
     * @param issueNo
     * @param count
     * @return
     * @throws Exception
     */
    int[][] areaPredict(int issueNo,int count,boolean isAccurate,int[] cases)throws Exception;

    /**
     * 预测最新一期
     * @param count 预测数量，如果传0默认是5条
     * @return
     * @throws Exception
     */
    int[][] predict(int count)throws Exception;

    /**
     * 根据期号查询list
     * @param issue
     * @return
     * @throws Exception
     */
    List<IssueRecord> list(IssueRecord issueRecord)throws Exception;


    Issue get(int issueNo)throws Exception;





}
