package com.thoughtworks.rslist.utils;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CommonUtils {
    public static TradeDto convertTradeDomainToDto(Trade trade) {
        TradeDto tradeDto = TradeDto.builder()
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .rank(trade.getRank())
                .amount(trade.getAmount())
                .build();
        return tradeDto;
    }

    public static Trade converTradeDtoToDomain(TradeDto tradeDto) {
        Trade trade = Trade.builder()
                .amount(tradeDto.getAmount())
                .rank(tradeDto.getRank())
                .build();
        return trade;

    }

    public static List<RsEvent> converRsEventDtoListToDomain(List<RsEventDto> rsEventDtos) {
        List<RsEvent> rsEvents = new ArrayList<>();
        for (RsEventDto rsEventDto : rsEventDtos) {
            RsEvent rsEvent = RsEvent.builder()
                    .eventName(rsEventDto.getEventName())
                    .keyword(rsEventDto.getKeyword())
                    .voteNum(rsEventDto.getVoteNum())
                    .userId(rsEventDto.getUser().getId())
                    .build();
            rsEvents.add(rsEvent);
        }

        return rsEvents;

    }

}
