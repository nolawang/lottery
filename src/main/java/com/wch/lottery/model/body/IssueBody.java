package com.wch.lottery.model.body;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


@Data
public class IssueBody implements Serializable {

    /**
     * 开始期数
     */
    private int issueNo;

    private LocalDate date;

    /**
     * 中奖号码串
     */
    private List<List<Integer>> records;
}
