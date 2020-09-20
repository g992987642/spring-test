package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class RsService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;
    final TradeRepository tradeRepository;

    public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.tradeRepository = tradeRepository;
    }

    public void vote(Vote vote, int rsEventId) {
        Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
        Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
        if (!rsEventDto.isPresent()
                || !userDto.isPresent()
                || vote.getVoteNum() > userDto.get().getVoteNum()) {
            throw new RuntimeException();
        }
        VoteDto voteDto =
                VoteDto.builder()
                        .localDateTime(vote.getTime())
                        .num(vote.getVoteNum())
                        .rsEvent(rsEventDto.get())
                        .user(userDto.get())
                        .build();
        voteRepository.save(voteDto);
        UserDto user = userDto.get();
        user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
        userRepository.save(user);
        RsEventDto rsEvent = rsEventDto.get();
        rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
        rsEventRepository.save(rsEvent);
    }


    @Transactional
    public void buy(Trade trade, int id) {

        //TODO
//        如果该排名上热搜已被购买，用户需要花高于当前价格的钱即可买到该位热搜，原热搜将会被替换掉（删除）
//        如果出价低于当前排名热搜价格，则购买失败，返回400
        Optional<RsEventDto> rsEventDtoOptional = rsEventRepository.findById(id);
        if (!rsEventDtoOptional.isPresent()) {
            throw new RequestNotValidException("RsEvent not exist");
        }

        TradeDto mostAmountTrade = tradeRepository.findFirstByRankOrderByAmountDesc(trade.getRank());
        TradeDto tradeDto = CommonUtils.convertTradeDomainToDto(trade);
        tradeDto.setRsEventDto(rsEventDtoOptional.get());
        if (mostAmountTrade == null) {
            //如果原来在高位，后来又被买到低位，需要先清除原来的购买记录
            tradeRepository.deleteByRsEventDto(rsEventDtoOptional.get());
            tradeRepository.save(tradeDto);
        } else {
            if (trade.getAmount() > mostAmountTrade.getAmount()) {
                //需要删除原来排名所在的热搜
                int mostAmountTradeId = mostAmountTrade.getRsEventDto().getId();
                if(mostAmountTradeId==id){
                    tradeRepository.deleteByRsEventDto(rsEventDtoOptional.get());
                }else{
                    rsEventRepository.deleteById(mostAmountTradeId);
                }
                tradeRepository.save(tradeDto);
            } else {
                throw new RequestNotValidException("amount not enough");
            }
        }
    }


    public List<RsEvent> getRsList() {
        RsEventDto rsEventDtoArray[] = new RsEventDto[1000];
        List<RsEventDto> allRsEventDto = rsEventRepository.findAll();
        List<TradeDto> allTrade = tradeRepository.findAll();
        if(allTrade.size()!=0){
            allTrade.stream().forEach(e -> {
                rsEventDtoArray[e.getRank()] = e.getRsEventDto();
                allRsEventDto.remove(e.getRsEventDto());
            });
            Comparator comparator = new Comparator<RsEventDto>() {
                @Override
                public int compare(RsEventDto o1, RsEventDto o2) {
                    int voteNumGap = o2.getVoteNum() - o1.getVoteNum();
                    return voteNumGap > 0 ? voteNumGap : -1;
                }
            };
            Collections.sort(allRsEventDto,comparator);
            int index=0;
            for(RsEventDto rsEventDto:allRsEventDto){
                while (rsEventDtoArray[index]!=null){
                    index++;
                }
                rsEventDtoArray[index]=rsEventDto;
            }
            List<RsEventDto> rsEventDtoResult=new ArrayList<RsEventDto>();
            for(RsEventDto rsEventDto:rsEventDtoArray){
                if(rsEventDto!=null){
                    rsEventDtoResult.add(rsEventDto);
                }
            }
            List<RsEvent> rsEvents = CommonUtils.converRsEventDtoListToDomain(rsEventDtoResult);
            return rsEvents;

        }else{
            List<RsEvent> rsEvents = CommonUtils.converRsEventDtoListToDomain(allRsEventDto);
            return rsEvents;
        }



    }
}
