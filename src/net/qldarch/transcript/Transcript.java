package net.qldarch.transcript;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.Lists;

public class Transcript {

  @Getter
  @Setter
  private String title;

  @Getter
  @Setter
  private String date;

  private List<Exchange> exchanges = Lists.newArrayList();

  void addExchange(Exchange u, boolean steadyTimestamp) {
    // just make sure that converting into seconds works (without throwing an exception)
    u.getSeconds();
    exchanges.add(u);
  }

  public boolean hasTitle() {
    return title != null;
  }

  public boolean hasDate() {
    return date != null;
  }

  public List<Exchange> getExchanges() {
    return Collections.unmodifiableList(exchanges);
  }

  public Exchange last() {
    return getExchanges().isEmpty() ? null : getExchanges().get(getExchanges().size() - 1);
  }
}
