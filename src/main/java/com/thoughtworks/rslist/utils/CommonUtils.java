package com.thoughtworks.rslist.utils;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.TradeDto;

import java.sql.Timestamp;

public class CommonUtils {
    public static TradeDto convertDomainToDto(Trade trade){
        TradeDto tradeDto=TradeDto.builder()
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .rank(trade.getRank())
                .amount(trade.getAmount())
                .build();
        return tradeDto;
    }

    public static Trade converDtoToDomain(TradeDto tradeDto){
        Trade trade =Trade.builder()
                .amount(tradeDto.getAmount())
                .rank(tradeDto.getRank())
                .build();
        return trade;

    }
}
