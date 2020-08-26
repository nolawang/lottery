package com.wch.lottery.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseVO implements Serializable {
    private int code;

    private String msg;

    private Object result;
}
