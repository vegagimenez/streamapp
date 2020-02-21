package com.streamapp.log.streamapp;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;

import com.streamapp.log.json.Json;
import com.streamapp.log.json.JsonMapper;

import java.time.Duration;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * Created by ashutosh.sharma1 on 11/28/2018.
 */
public class ServerResponse {
    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : "192.168.99.100:9092";
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "server-response");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "server-response-client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);

        // For illustrative purposes we disable record caches
        streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream("weblog", Consumed.with(Serdes.String(), Serdes.String()));

        KTable<String, Long> countWin = source.mapValues(new JsonMapper())
                .map((key, jsonValue) -> {
            String timestamp = jsonValue.propertyStringValue("timestamp");
            Long response = jsonValue.propertyLongValue("response");
            return new KeyValue<String, String>(response.toString(), timestamp);
                })
                .groupByKey()
                .count();

        countWin.toStream().print(Printed.toSysOut());

        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }
}

    /*KTable<String, Long> countWin = source.mapValues(new JsonMapper())
            .filter((key, jsonValue) -> jsonValue.propertyLongValue("response")
                    .equals(200))
            .map((key, Long) -> {
                return new KeyValue<String, Long>("Success", 1L);
            })
            .groupByKey()
            .count();
        countWin
                .toStream()
                .foreach((windowKey, count) -> {
                long windowStart = windowKey.window().start();
                long windowEnd = windowKey.window().end();
                });*/