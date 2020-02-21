package com.streamapp.log.streamapp;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import com.streamapp.log.json.JsonMapper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Properties;

/**
 * Created by ashutosh.sharma1 on 11/28/2018.
 */
public class UserAgent {
    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : "192.168.99.100:9092";
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "user-agent");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "user-agent-client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);

        // For illustrative purposes we disable record caches
        streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();
        final UserAgentParser parser = new UserAgentService().loadParser();
        final KStream<String, String> source = builder.stream("weblog", Consumed.with(Serdes.String(), Serdes.String()));

        KTable<String, Long> countWin = source.mapValues(new JsonMapper())
                .map((key, jsonValue) -> {
                    String timestamp = jsonValue.propertyStringValue("timestamp");
                    String agent = jsonValue.propertyStringValue("agent");
                    Capabilities capabilities = parser.parse(agent);
                    return new KeyValue<String, String>(capabilities.getBrowser(), timestamp);
                })
                .groupByKey()
                .count();

        countWin.toStream().print(Printed.toSysOut());

        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }
}
