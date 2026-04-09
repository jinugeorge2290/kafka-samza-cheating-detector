package com.exam.monitor;

import org.apache.samza.config.MapConfig;
import org.apache.samza.runtime.LocalApplicationRunner;
import org.apache.samza.task.StreamTaskFactory;

import java.util.HashMap;
import java.util.Map;

public class RealTimeRunner {
    public static void main(String[] args) {
        try {
            Map<String, String> config = new HashMap<>();

            // -------------------
            // BASIC JOB CONFIG
            // -------------------
            config.put("job.name", "cheating-detector");
            config.put("job.id", "1");

            // -------------------
            // LOCAL RUNNER
            // -------------------
            config.put("app.runner.class", "org.apache.samza.runtime.LocalApplicationRunner");
            config.put("job.factory.class", "org.apache.samza.job.local.ThreadJobFactory");
            config.put("job.coordinator.factory", "org.apache.samza.standalone.PassthroughJobCoordinatorFactory");
            config.put("job.coordination.utils.factory", "org.apache.samza.standalone.PassthroughCoordinationUtilsFactory");
            config.put("task.name.grouper.factory", "org.apache.samza.container.grouper.task.SingleContainerGrouperFactory");
            config.put("processor.id", "0");

            // -------------------
            // TASK CONFIG
            // -------------------
            config.put("task.class", "com.exam.monitor.CheatingDetectionTask");
            config.put("task.inputs", "kafka.exam-events");

            // -------------------
            // KAFKA CONFIG
            // -------------------
            config.put("systems.kafka.samza.factory", "org.apache.samza.system.kafka.KafkaSystemFactory");
            config.put("systems.kafka.consumer.zookeeper.connect", "localhost:2181");
            config.put("systems.kafka.consumer.bootstrap.servers", "localhost:29092");
            config.put("systems.kafka.producer.bootstrap.servers", "localhost:29092");
            config.put("job.default.system", "kafka");

            // Read from beginning
            config.put("systems.kafka.default.stream.samza.offset.default", "oldest");
            config.put("systems.kafka.consumer.auto.offset.reset", "earliest");

            // -------------------
            // SERDE
            // -------------------
            config.put("serializers.registry.string.class", "org.apache.samza.serializers.StringSerdeFactory");
            config.put("systems.kafka.samza.key.serde", "string");
            config.put("systems.kafka.samza.msg.serde", "string");

            // -------------------
            // STREAMS
            // -------------------
            config.put("streams.exam-events.samza.system", "kafka");
            config.put("streams.exam-events.samza.physical.name", "exam-events");
            config.put("streams.exam-events.samza.key.serde", "string");
            config.put("streams.exam-events.samza.msg.serde", "string");

            config.put("streams.exam-alerts.samza.system", "kafka");
            config.put("streams.exam-alerts.samza.physical.name", "exam-alerts");
            config.put("streams.exam-alerts.samza.key.serde", "string");
            config.put("streams.exam-alerts.samza.msg.serde", "string");

            System.out.println("--- STARTING SAMZA TASK JOB ---");

            LocalApplicationRunner runner = new LocalApplicationRunner(new StreamTaskFactory() {
                @Override
                public org.apache.samza.task.StreamTask createInstance() {
                    return new CheatingDetectionTask();
                }
            }, new MapConfig(config));

            runner.run();

            System.out.println("--- SAMZA TASK JOB IS NOW RUNNING ---");
            System.out.println("Listening to exam-events and writing to exam-alerts...");

            while (true) {
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            System.err.println("Fatal error starting Samza job:");
            e.printStackTrace();
        }
    }
}