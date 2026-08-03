package com.example.evapp.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SafeStringTypeAdapter extends TypeAdapter<String> {

    @Override
    public void write(JsonWriter out, String value) throws IOException {
        if (value == null) {
            out.value("");
        } else {
            out.value(value);
        }
    }

    @Override
    public String read(JsonReader in) throws IOException {
        JsonToken token = in.peek();

        if (token == JsonToken.NULL) {
            in.nextNull();
            return "";
        }

        if (token == JsonToken.STRING) {
            return in.nextString();
        }

        if (token == JsonToken.NUMBER) {
            return String.valueOf(in.nextDouble());
        }

        if (token == JsonToken.BOOLEAN) {
            return String.valueOf(in.nextBoolean());
        }

        in.skipValue();
        return "";
    }
}
