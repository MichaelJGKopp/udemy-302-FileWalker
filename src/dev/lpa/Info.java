package dev.lpa;

import java.util.EnumMap;

public enum Info {

  SIZE, FILES, FOLDERS;

  public static EnumMap<Info, Long> getEmptyMap() {

    EnumMap<Info, Long> infoMap = new EnumMap<>(Info.class);
    infoMap.put(Info.SIZE, 0L);
    infoMap.put(Info.FILES, 0L);
    infoMap.put(Info.FOLDERS, 0L);

    return infoMap;
  }
}
