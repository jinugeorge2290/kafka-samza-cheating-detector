package com.exam.monitor;

import org.apache.samza.application.StreamApplication;
import org.apache.samza.application.descriptors.StreamApplicationDescriptor;
import org.apache.samza.serializers.StringSerde;
import org.apache.samza.system.kafka.descriptors.KafkaInputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaOutputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaSystemDescriptor;

import java.util.HashMap;
import java.util.Map;

public class CheatingDetectionApp implements StreamApplication {

    // In-memory counter per student
    private static final Map<String, Integer> violationCounts = new HashMap<>();

    @Override
    public void describe(StreamApplicationDescriptor appDescriptor) {
        System.out.println("===== NEW VERSION OF CheatingDetectionApp LOADED =====");
        KafkaSystemDescriptor kafkaSystemDescriptor = new KafkaSystemDescriptor("kafka");
        StringSerde stringSerde = new StringSerde();

        KafkaInputDescriptor<String> inputDescriptor =
                kafkaSystemDescriptor.getInputDescriptor("exam-events", stringSerde);

        KafkaOutputDescriptor<String> outputDescriptor =
                kafkaSystemDescriptor.getOutputDescriptor("exam-alerts", stringSerde);

        appDescriptor.getInputStream(inputDescriptor)
                .map(event -> {
                    System.out.println("DEBUG: Received event -> " + event);

                    // Expected format: STU101:TAB_SWITCH
                    String[] parts = event.split(":");
                    if (parts.length < 2) {
                        System.out.println("DEBUG: Invalid event format -> " + event);
                        return null;
                    }

                    String studentId = parts[0].trim();
                    String eventType = parts[1].trim();

                    int count = violationCounts.getOrDefault(studentId, 0) + 1;
                    violationCounts.put(studentId, count);

                    System.out.println("DEBUG: Student " + studentId + " event = " + eventType + ", count = " + count);

                    if (count >= 3) {
                        String alert = "ALERT: Potential Cheating by " + studentId;
                        System.out.println("DEBUG: Sending alert -> " + alert);

                        // Reset after alert so it doesn't spam endlessly
                        violationCounts.put(studentId, 0);

                        return alert;
                    }

                    return null;
                })
                .filter(alert -> alert != null)
                .sendTo(appDescriptor.getOutputStream(outputDescriptor));
    }
}
