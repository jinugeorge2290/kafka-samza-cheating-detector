package com.exam.monitor;

import org.apache.samza.application.StreamApplication;
import org.apache.samza.application.descriptors.StreamApplicationDescriptor;
import org.apache.samza.operators.MessageStream;
import org.apache.samza.serializers.StringSerde;
import org.apache.samza.system.kafka.descriptors.KafkaInputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaOutputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaSystemDescriptor;

import java.util.HashMap;
import java.util.Map;

public class CheatingDetectionApp implements StreamApplication {

    private final Map<String, Integer> violationCounts = new HashMap<>();

    @Override
    public void describe(StreamApplicationDescriptor appDescriptor) {

        System.out.println("DEBUG: CheatingDetectionApp.describe() called");

        KafkaSystemDescriptor kafkaSystem = new KafkaSystemDescriptor("kafka");
        appDescriptor.withDefaultSystem(kafkaSystem);

        KafkaInputDescriptor<String> inputDescriptor =
                kafkaSystem.getInputDescriptor("exam-events", new StringSerde());

        KafkaOutputDescriptor<String> outputDescriptor =
                kafkaSystem.getOutputDescriptor("exam-alerts", new StringSerde());

        MessageStream<String> inputStream = appDescriptor.getInputStream(inputDescriptor);

        inputStream
            .map(event -> {
                System.out.println("DEBUG: Received event -> " + event);

                String[] parts = event.split(":");
                if (parts.length < 2) {
                    System.out.println("DEBUG: Invalid event format -> " + event);
                    return null;
                }

                String studentId = parts[0].trim();
                String eventType = parts[1].trim();

                int count = violationCounts.getOrDefault(studentId, 0) + 1;
                violationCounts.put(studentId, count);

                System.out.println("DEBUG: Student " + studentId +
                        " event=" + eventType + ", count=" + count);

                if (count >= 3) {
                    violationCounts.put(studentId, 0);
                    String alert = "ALERT: Potential Cheating by " + studentId;
                    System.out.println("DEBUG: ALERT CREATED -> " + alert);
                    return alert;
                }

                return null;
            })
            .filter(alert -> {
                boolean keep = alert != null;
                if (keep) {
                    System.out.println("DEBUG: Passing alert downstream -> " + alert);
                }
                return keep;
            })
            .sendTo(appDescriptor.getOutputStream(outputDescriptor));

        System.out.println("DEBUG: Samza pipeline created successfully");
    }
}