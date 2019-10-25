package com.qxj.springboot.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author YoSaukit
 * @date 2019/10/24 22:08
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeDataModel implements Serializable {
    private Long id;

    private Integer unit;

    private String ticker;


    private String shortnm;


    private DateTime barTime;


    private BigDecimal openPrice;


    private BigDecimal closePrice;


    private BigDecimal lowPrice;


    private BigDecimal highPrice;


    private BigDecimal totalValue;


    private Integer totalVolume;


    private BigDecimal vwap;

    public TradeDataModel() {
    }

    public TradeDataModel(Integer unit, String ticker, String shortnm, DateTime barTime, BigDecimal openPrice, BigDecimal closePrice, BigDecimal lowPrice, BigDecimal highPrice, BigDecimal totalValue, Integer totalVolume, BigDecimal vwap) {
        this.unit = unit;
        this.ticker = ticker;
        this.shortnm = shortnm;
        this.barTime = barTime;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.lowPrice = lowPrice;
        this.highPrice = highPrice;
        this.totalValue = totalValue;
        this.totalVolume = totalVolume;
        this.vwap = vwap;
    }
}
