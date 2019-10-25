package com.qxj.springboot.service;

import com.qxj.springboot.controller.model.TradeDataModel;


import java.util.Set;

/**
 * @author YoSaukit
 * @date 2019/10/24 14:12
 */

public interface CurrentQueryService {

    Set<TradeDataModel> Query(int unit, String ticket, String startTime, String endTime);
}
