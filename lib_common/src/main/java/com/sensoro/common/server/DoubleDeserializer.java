package com.sensoro.common.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by tangrisheng on 2016/5/6.
 * number element.if "" or an exception appears , return 0;
 */
public class DoubleDeserializer implements JsonDeserializer<Double> {
    @Override
    public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        try {
            if (typeOfT.equals(short.class) || typeOfT.equals(Short.class)) {
                return (double) json.getAsShort();
            } else if (typeOfT.equals(int.class) || typeOfT.equals(Integer.class)) {
                return (double) json.getAsInt();
            } else if (typeOfT.equals(long.class) || typeOfT.equals(Long.class)) {
                return (double) json.getAsLong();
            } else if (typeOfT.equals(float.class) || typeOfT.equals(Float.class)) {
                return (double) json.getAsFloat();
            } else if (typeOfT.equals(double.class) || typeOfT.equals(Double.class)) {
                return json.getAsDouble();
            } else {
                return null;
            }
        } catch (Exception e) {
            return 0d;
        }
    }
}
