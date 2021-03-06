package demo.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;

public class DemoConsumer {

    private final KafkaConsumer<String, JsonNode> consumer;

    public DemoConsumer(List<String> topics) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9090,localhost:9091,localhost:9092");

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "demo-consumer-group-id");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 3);

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(topics, new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                System.out.println("Revoked partitions " + partitions);
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                System.out.println("Assigned partitions " + partitions);
            }
        });

    }

    public ConsumerRecords<String, JsonNode> poll() {
        return this.consumer.poll(50);

    }


    public void commit(Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap) {
        consumer.commitSync(topicPartitionOffsetAndMetadataMap);
    }


    public static void readAllRecords() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> topics = Collections.singletonList("test");
        DemoConsumer consumer = new DemoConsumer(topics);

        try {
            while (true) {

                ConsumerRecords<String, JsonNode> records = consumer.poll();

                for (ConsumerRecord<String, JsonNode> record : records) {

                    JsonNode value = record.value();
                    System.out.println(objectMapper.writeValueAsString(value));
                }
                consumer.commit();
            }
        } finally {
            consumer.close();
        }
    }

    public void close() {
        consumer.close();
    }

    public void commit() {
        consumer.commitSync();
    }

    public void assign(List<TopicPartition> topicPartitions) {
        consumer.assign(topicPartitions);
    }
}
