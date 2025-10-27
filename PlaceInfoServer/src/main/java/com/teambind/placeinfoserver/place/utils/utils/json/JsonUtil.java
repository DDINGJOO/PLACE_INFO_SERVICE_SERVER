package com.teambind.placeinfoserver.place.utils.utils.json;

public interface JsonUtil {
  String toJson(Object object);
  
  <T> T fromJson(String json, Class<T> clazz);
}
