package com.qxj.springboot.service.serviceImpl;

import com.qxj.springboot.controller.model.TradeDataModel;
import com.qxj.springboot.dao.TradeDataDOMapper;
import com.qxj.springboot.dataobject.TradeDataDO;
import com.qxj.springboot.service.CurrentQueryService;
import com.qxj.springboot.utils.CommonConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author YoSaukit
 * @date 2019/10/24 14:13
 */
@Service
public class CurrentQueryServiceImpl implements CurrentQueryService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TradeDataDOMapper tradeDataDOMapper;

    /**
     * 获取沪深交易所当日分钟线数据
     *
     * @param unit
     * @param ticker
     * @param startTime,endTime
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<TradeDataModel> Query(int unit, String ticker, String startTime, String endTime) {
        Set<TradeDataModel> tradeDataModelSet;
        if (startTime == null || startTime.length() == 0) {
            if (endTime == null || endTime.length() == 0) {
                //没有指定时间范围查询全量
                tradeDataModelSet = (HashSet<TradeDataModel>) redisTemplate.opsForZSet()
                        .range(CommonConstant.MIN1_PREFIX + ticker, 0, -1);
            }else{
                //从开始时间向后查全量
                tradeDataModelSet = (HashSet<TradeDataModel>) redisTemplate.opsForZSet()
                        .rangeByScore(CommonConstant.MIN1_PREFIX,transforTime(startTime),transforTime("3"));
            }
        }else{
            tradeDataModelSet = (HashSet<TradeDataModel>) redisTemplate.opsForZSet()
                    .rangeByScore(CommonConstant.MIN1_PREFIX,transforTime(startTime),transforTime(endTime));
        }

        if (tradeDataModelSet == null || tradeDataModelSet.size() == 0) {
            //缓存中没有 从数据库中查

        }
        return tradeDataModelSet;
    }

    private Long transforTime(String time) {
        String str = time.replaceAll("[[\\s-:punct:]]", "");
        int len = str.length();
        StringBuffer sb = new StringBuffer(str);
        //redis里面score：20191024093300 14位
        while (len < 14) {
            sb.append("0");
            len++;
        }
        return Long.valueOf(sb.toString());
    }
}
