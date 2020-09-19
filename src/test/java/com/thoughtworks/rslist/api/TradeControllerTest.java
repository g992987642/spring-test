package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.utils.CommonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class TradeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    TradeRepository tradeRepository;
    UserDto userDto;
    RsEventDto rsEventDto;
    RsEventDto rsEventDto2;
    TradeDto tradeDto;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
        tradeRepository.deleteAll();

        //TODO 这边的ID设置并不能起作用，因为JPA的ID生成策略是一个所有Entity通用的计数器，
        // 每一次Save都会+1，所以每个表的第一个数据的主键ID也不是1，需要修改。
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
        rsEventDto2 =
                RsEventDto.builder()
                        .eventName("event name2")
                        .id(3)
                        .keyword("keyword2")
                        .voteNum(3)
                        .user(userDto)
                        .build();
        tradeDto = TradeDto.builder()
                .amount(10)
                .rank(1)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .rsEventDto(rsEventDto)
                .id(4)
                .build();
        userRepository.save(userDto);
        rsEventRepository.save(rsEventDto);
        rsEventRepository.save(rsEventDto2);
        tradeRepository.save(tradeDto);
    }

    @Test
    void shouldNotBuyRsEventWhenAmoutIsNotEnough() throws Exception {

        Trade trade = new Trade(9, 1);
        ObjectMapper objectMapper = new ObjectMapper();
        String tradeString = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/rs/buy/2").content(tradeString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("amount not enough")));
    }

    @Test
    void shouldNotBuyRsEventWhenRsEventIsNotExist() throws Exception {

        Trade trade = new Trade(9, 1);
        ObjectMapper objectMapper = new ObjectMapper();
        String tradeString = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/rs/buy/4").content(tradeString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("RsEvent not exist")));
    }

    @Test
    void shouldAddTradeforBuyRsEvent() throws Exception {

        Trade trade = new Trade(11, 1);
        ObjectMapper objectMapper = new ObjectMapper();
        String tradeString = objectMapper.writeValueAsString(trade);
        List<TradeDto> allTradeDtoBeforeBuy = tradeRepository.findAllByRsEventDto(rsEventDto);
        assertEquals(1,allTradeDtoBeforeBuy.size());
        mockMvc.perform(post("/rs/buy/2").content(tradeString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<TradeDto> allTradeDtoAfterBuy = tradeRepository.findAllByRsEventDto(rsEventDto);
        assertEquals(2,allTradeDtoAfterBuy.size());

    }


}
