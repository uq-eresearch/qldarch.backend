package net.qldarch.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

// ImmutableMap does not support null values, so use HashMaps...
public class M {

  public static <K,V> Map<K,V> of(K k1, V v1) {
    Map<K,V> m = new LinkedHashMap<>();
    m.put(k1, v1);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2) {
    Map<K,V> m = of(k1, v1);
    m.put(k2, v2);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
    Map<K,V> m = of(k1, v1, k2, v2);
    m.put(k3, v3);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    Map<K,V> m = of(k1, v1, k2, v2, k3, v3);
    m.put(k4, v4);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    Map<K,V> m = of(k1, v1, k2, v2, k3, v3, k4, v4);
    m.put(k5, v5);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3,
      K k4, V v4, K k5, V v5, K k6, V v6) {
    Map<K,V> m = of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    m.put(k6, v6);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3,
      K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
    Map<K,V> m = of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    m.put(k7, v7);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3,
      K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
    Map<K,V> m = of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    m.put(k8, v8);
    return m;
  }

  public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3,
      K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
    Map<K,V> m = of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    m.put(k9, v9);
    return m;
  }

  @SafeVarargs
  public static <K, V> Map<K,V> copyOf(Map<K,V> map, K... keys) {
    Map<K,V> m = new LinkedHashMap<>();
    for(K key : keys) {
      m.put(key, map.get(key));
    }
    return m;
  }

  public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2) {
    if((map1 == null) && (map2 == null)) {
      return null;
    } else if(map1 == null) {
      return map2;
    } else if(map2 == null) {
      return map1;
    } else {
      Map<K, V> m = new LinkedHashMap<>();
      m.putAll(map1);
      m.putAll(map2);
      return m;
    }
  }

  public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2, Map<K, V> map3) {
    if(map3 == null) {
      Map<K, V> m = merge(map1, map2);
      return m;
    } else {
      Map<K, V> m = merge(map1, map2);
      m.putAll(map3);
      return m;
    }
  }

  @SafeVarargs
  public static <K, V> boolean valuesNotNull(Map<K, V> map, K... keys) {
    return Arrays.stream(keys).allMatch(key -> map.get(key) != null);
  }

  public static <K, V> V singleValue(Map<K, V> map) {
    return ((map!=null)&&(map.values().size() == 1))?map.values().iterator().next():null;
  }

  public static <K, V> Map<K,V> singleReplace(Map<K,V> map, K key, UnaryOperator<V> op) {
    if(map == null) {
      return null;
    }
    Map<K,V> result = new HashMap<>(map);
    result.replace(key, op.apply(map.get(key)));
    return result;
  }

}
