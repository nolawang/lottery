package com.wch.lottery.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * area_prediction_record
 * @author 
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AreaPredictionRecord implements Serializable {

    public AreaPredictionRecord(Integer issueNo,Byte num,Boolean red){
        this.issueNo = issueNo;
        this.num = num;
        this.red = red;
    }

    public AreaPredictionRecord(Integer issueNo){
        this.issueNo = issueNo;
    }

    private Long id;

    private Integer issueNo;

    /**
     * 号码
     */
    private Byte num;

    /**
     * 是否为红球 0红球 1蓝球
     */
    private Boolean red;

    /**
     * 是否命中 1命中 0未命中
     */
    private Boolean hit;

    private static final long serialVersionUID = 1L;
}