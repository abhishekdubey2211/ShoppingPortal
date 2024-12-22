package com.jodo.portal.redis;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonConfig {
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Object.class, new HibernateProxyTypeAdapter())
                .create();
    }
}
