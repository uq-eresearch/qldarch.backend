package net.qldarch.gson.serialize;

import java.util.Collections;
import java.util.List;

public class Utils {

  public static <T> List<T> tail(List<T> list) {
    if(list.isEmpty()) {
      return Collections.emptyList();
    } else {
      return list.subList(1, list.size());
    }
  }

}
