package com.qxj.springboot.dao;

import com.qxj.springboot.dataobject.TradeDataDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TradeDataDOMapper extends Mapper<TradeDataDO> {

    List<TradeDataDO> selectByFields(@Param("unit") int unit, @Param("ticket") String ticket, @Param("time") String time);
}