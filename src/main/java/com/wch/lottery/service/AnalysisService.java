package com.wch.lottery.service;

import com.wch.lottery.model.vo.PredictionVO;

public interface AnalysisService {

    PredictionVO get(String issueNo, Integer count);

}
