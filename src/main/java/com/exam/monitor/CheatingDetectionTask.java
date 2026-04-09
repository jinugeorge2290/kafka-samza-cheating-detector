package com.exam.monitor;

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskCoordinator;

import java.util.HashMap;
import java.util.Map;

public class CheatingDetectionTask implements StreamTask {

    private final Map<String, Integer> violationCounts = new HashMap<>();
    private final SystemStream outputStream = new SystemStream("kafka", "exam-alerts");

    @Override
    public void process(IncomingMessageEnvelope envelope,
                        MessageCollector collector,
                        TaskCoordinator coordinator) {

        Object msg = envelope.getMessage();

        if (msg == null) {
            System.out.println("DEBUG: Received null message");
            return;
        }

        String event = msg.toString();
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
                " event=" + eventType + ", count=" + count);

        if (count >= 3) {
            String alert = "ALERT: Potential Cheating by " + studentId;
            System.out.println("DEBUG: Sending alert -> " + alert);

            collector.send(new OutgoingMessageEnvelope(outputStream, alert));

            // reset after alert
            violationCounts.put(studentId, 0);
        }
    }
}