package com.wch.lottery.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class IssueRecordDTO {

    private Integer beginIssueNo;

    private Integer endIssueNo;

    private Integer minCount;

    private Integer maxCount;

    private Byte num;

    /**
     * 是否为红球 1红球 0蓝球
     */
    private Boolean red;

}
