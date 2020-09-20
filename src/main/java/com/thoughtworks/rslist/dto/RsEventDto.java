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
  //这两个应当无级联关系，如果想要保存所有的Trade数据，也不应当在数据库删除RsEvent，而是设置为"过期"等类似的标志位
  //这里还是通过级联简单实现下，会导致删除热搜时将所有相关Trade清空
  @OneToMany(mappedBy = "rsEventDto", cascade= CascadeType.REMOVE)
  private List<TradeDto> tradeDto;
}
