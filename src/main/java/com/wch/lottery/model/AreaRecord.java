package com.wch.lottery.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * area_record
 * @author 
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AreaRecord implements Serializable {
    private Long id;

    private Integer issueNo;

    private Long predictionId;

    /**
     * 数量区间
     */
    private Integer area;

    /**
     * 区间数量
     */
    private Integer count;

    private static final long serialVersionUID = 1L;
}