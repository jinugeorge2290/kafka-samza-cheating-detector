package com.exam.monitor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;

public class RealTimeRunner {

    private static final Map<String, Integer> violationCounts = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("===== EXAM CHEATING DETECTION SYSTEM STARTED =====");

        // -----------------------------
        // KAFKA CONSUMER CONFIG
        // -----------------------------
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "cheating-detector-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("exam-events"));

        // -----------------------------
        // KAFKA PRODUCER CONFIG
        // -----------------------------
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        System.out.println("Listening to topic: exam-events");
        System.out.println("Publishing alerts to topic: exam-alerts");

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                records.forEach(record -> {
                    String event = record.value();
                    System.out.println("DEBUG: Received event -> " + event);

                    String[] parts = event.split(":");
                    if (parts.length < 2) {
                        System.out.println("DEBUG: Invalid event format -> " + event);
                        return;
                    }

                    String studentId = parts[0].trim();
                    String eventType = parts[1].trim();

                    int count = violationCounts.getOrDefault(studentId, 0) + 1;
                    violationCounts.put(studentId, count);

                    System.out.println("DEBUG: Student " + studentId +
                            " event = " + eventType + ", count = " + count);

                    if (count >= 3) {
                        String alert = "ALERT: Potential Cheating by " + studentId;
                        System.out.println("DEBUG: Sending alert -> " + alert);

                        producer.send(new ProducerRecord<>("exam-alerts", alert));

                        // Reset after alert
                        violationCounts.put(studentId, 0);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
            producer.close();
        }
    }
}