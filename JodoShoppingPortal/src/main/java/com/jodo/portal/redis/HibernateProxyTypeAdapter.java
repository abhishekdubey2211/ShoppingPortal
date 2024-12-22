package com.jodo.portal.redis;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class HibernateProxyTypeAdapter extends TypeAdapter<Object> {
    @Override
    public void write(JsonWriter out, Object value) throws IOException {
        if (value instanceof HibernateProxy) {
            // Extract the actual entity
            Object entity = ((HibernateProxy) value).getHibernateLazyInitializer().getImplementation();
            new Gson().toJson(entity, entity.getClass(), out); // Serialize the actual entity
        } else {
            new Gson().toJson(value, value.getClass(), out);
        }
    }

    @Override
    public Object read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Deserialization not supported");
    }
}
