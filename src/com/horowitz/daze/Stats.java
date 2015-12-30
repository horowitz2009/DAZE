package com.horowitz.daze;

import java.util.Hashtable;
import java.util.Map;

public class Stats {

  private Map<String, Integer> _map = new Hashtable<>();

  public void register(String counterName) {
    Integer cnt = 0;
    if (_map.containsKey(counterName))
      cnt = _map.get(counterName);
    _map.put(counterName, cnt + 1);
  }

  public int getCount(String key) {
    int cnt = 0;
    if (_map.containsKey(key))
      cnt = _map.get(key);
    return cnt;
  }

}
