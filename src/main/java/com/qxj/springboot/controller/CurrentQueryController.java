package com.qxj.springboot.controller;

import com.qxj.springboot.response.CommonReturnType;
import com.qxj.springboot.service.CurrentQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author YoSaukit
 * @date 2019/10/24 14:12
 */
@RestController
@RequestMapping("/current")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class CurrentQueryController extends BaseController {
    @Autowired
    private CurrentQueryService currentQueryService;

    /**
     * 获取沪深交易所当日分钟线数据
     *
     * @param unit              时间宽度
     * @param ticker            交易唯一标识
     * @param endTime,startTime 时间（范围查询）
     * @return
     */
    @GetMapping("/min")
    public CommonReturnType query(@RequestParam("unit") int unit,
                                  @RequestParam("ticker") String ticker,
                                  @RequestParam("startTime") String startTime,
                                  @RequestParam("endTime") String endTime) {
        return CommonReturnType.create(currentQueryService.Query(unit, ticker, startTime, endTime));
    }


}
