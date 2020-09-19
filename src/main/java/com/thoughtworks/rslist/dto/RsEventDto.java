package com.thoughtworks.rslist.dto;

import com.thoughtworks.rslist.domain.Trade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rsEvent")
public class RsEventDto {
  @Id @GeneratedValue private int id;
  private String eventName;
  private String keyword;
  private int voteNum;
  @ManyToOne
  private UserDto user;
  @OneToMany(mappedBy = "rsEventDto", cascade= CascadeType.REMOVE)
  private List<TradeDto> tradeDto;
}
