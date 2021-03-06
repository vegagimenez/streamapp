package com.streamapp.log.streamapp;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import com.streamapp.log.json.JsonMapper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ashutosh.sharma1 on 11/28/2018.
 */
public class UniqueVisitor {
    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : "192.168.99.100:9092";

        ElassandraClient elassandraClient = ElassandraClient.getInstance();
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "unique-visitor1");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "unique-visitor-client");

        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);

        // For illustrative purposes we disable record caches
        streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream("weblog", Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> uniqueVisit = source.mapValues(new JsonMapper())
                .map((key, jsonValue) -> {
                    String timestamp = jsonValue.propertyStringValue("timestamp");
                    String clientip = jsonValue.propertyStringValue("clientip");
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("timestamp", timestamp);
                    jsonMap.put("clientip", clientip);
                    elassandraClient.ElassandraInsert("webanalytics", "visitor", null, jsonMap);
                    return new KeyValue<String, String>(clientip, timestamp);
                });

        KTable<Windowed<String>, Long> stats = uniqueVisit.groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
                .count();

        stats.toStream().print(Printed.toSysOut());
         KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }
}
