package com.streamapp.log.streamapp;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

public class TotalTraffic {

    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : "172.16.41.216:9092,172.16.41.224:9093,172.16.41.227:9094";
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "traffic");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "traffic-client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);

        // For illustrative purposes we disable record caches
        streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamsConfiguration.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream("weblog");

        KTable<String, Long> logcounts = source
                .selectKey((key, value) -> "apachelog")
                .groupByKey()
                .count();

        logcounts.toStream().print(Printed.toSysOut());

        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));



    }

}