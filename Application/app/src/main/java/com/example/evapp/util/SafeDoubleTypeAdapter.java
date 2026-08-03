package com.example.evapp.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SafeDoubleTypeAdapter extends TypeAdapter<Double> {

    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        if (value == null || value.isNaN() || value.isInfinite()) {
            out.value(0.0);
        } else {
            out.value(value);
        }
    }

    @Override
    public Double read(JsonReader in) throws IOException {
        JsonToken token = in.peek();

        if (token == JsonToken.NULL) {
            in.nextNull();
            return 0.0;
        }

        if (token == JsonToken.STRING) {
            String str = in.nextString();
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        if (token == JsonToken.NUMBER) {
            return in.nextDouble();
        }

        in.skipValue();
        return 0.0;
    }
}
