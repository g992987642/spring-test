package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.utils.CommonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SpringBootTest
class RsServiceTest {
    RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;



    LocalDateTime localDateTime;
    Vote vote;
    TradeDto tradeDto;
    UserDto userDto;
    RsEventDto rsEventDto;
    @BeforeEach
    void setUp() {
        initMocks(this);
        rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
        localDateTime = LocalDateTime.now();
        vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
        userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(1)
                        .build();
        rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(2)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        tradeDto = TradeDto.builder()
                .amount(10)
                .rank(1)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .rsEventDto(rsEventDto)
                .id(4)
                .build();
    }

    @Test
    void shouldVoteSuccess() {
        // given

        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
        // when
        rsService.vote(vote, 1);
        // then
        verify(voteRepository)
                .save(
                        VoteDto.builder()
                                .num(2)
                                .localDateTime(localDateTime)
                                .user(userDto)
                                .rsEvent(rsEventDto)
                                .build());
        verify(userRepository).save(userDto);
        verify(rsEventRepository).save(rsEventDto);
    }

    @Test
    void shouldThrowExceptionWhenUserNotExist() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        //when&then
        assertThrows(
                RequestNotValidException.class,
                () -> {
                    rsService.vote(vote, 1);
                });
    }

    @Test
    void shouldThrowExceptionWhenRsEventIsNotExist() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        Trade trade = CommonUtils.converTradeDtoToDomain(tradeDto);
        //when&then
        assertThrows(
                RequestNotValidException.class,
                () -> {
                    rsService.buy(trade,1);
                });
    }


    @Test
    void shouldAddTradeWhenNoTradeBofore() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(tradeRepository.findAllByRsEventDto(any(RsEventDto.class))).thenReturn(new ArrayList<>());
        Trade trade = new Trade(10,1);
        TradeDto tradeDtoInTest = CommonUtils.convertTradeDomainToDto(trade);
        //when
        rsService.buy(trade,2);
        //then
        verify(tradeRepository).save(any());
    }


    @Test
    void shouldThrowExceptionWhenAmountIsNotEnough() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        ArrayList<TradeDto> tradeDtos = new ArrayList<>();
        tradeDtos.add(tradeDto);
        when(tradeRepository.findAllByRsEventDto(any(RsEventDto.class))).thenReturn(tradeDtos);
        Trade trade = new Trade(9,1);
        //when&then
        assertThrows(
                RequestNotValidException.class,
                () -> {
                    rsService.buy(trade,2);
                });
    }

    @Test
    void shouldAddTrade() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        ArrayList<TradeDto> tradeDtos = new ArrayList<>();
        tradeDtos.add(tradeDto);
        when(tradeRepository.findAllByRsEventDto(any(RsEventDto.class))).thenReturn(tradeDtos);
        Trade trade = new Trade(11,1);
        //when
            rsService.buy(trade,2);
        // then
        verify(tradeRepository).save(any());
    }




}
