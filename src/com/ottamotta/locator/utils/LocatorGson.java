package com.ottamotta.locator.utils;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LocatorGson {

    private static Gson gson;

    public static synchronized Gson getGson() {
        if (null == gson) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(Uri.class, new UriSerializer())
                    .registerTypeAdapter(Uri.class, new UriDeserializer())
                    .create();
        }
        return gson;
    }

    private static class UriSerializer implements JsonSerializer<Uri> {
        @Override
        public JsonElement serialize(Uri uri, Type type, JsonSerializationContext jsonSerializationContext) {
            if (null == uri) return null;
            return new JsonPrimitive(uri.toString());
        }
    }

    private static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement == null) return null;
            return Uri.parse(jsonElement.getAsJsonPrimitive().getAsString());
        }
    }

}
