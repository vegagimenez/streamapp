package com.streamapp.log.json;

/**
 * Created by ashutosh.sharma1 on 11/28/2018.
 */

import com.google.gson.JsonParser;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonMapper implements ValueMapper<String, Json> {

    private static final Logger log = LoggerFactory.getLogger(JsonMapper.class);

    private JsonParser jsonParser = new JsonParser();

    @Override
    public Json apply(String value) {
        Json json = new Json(jsonParser.parse(value).getAsJsonObject());

        return json;
    }
}
