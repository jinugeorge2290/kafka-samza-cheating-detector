1. Download APACHE Kafka ver 3.7.0 (kafka_2.13-3.7.0)
2. Place it in you C or D drive.
3. Start the commandline from the kafka folder. Ex - **cd C:\kafka_2.13-3.7.0**
4. Start the zookeeper - **bin/zookeeper-server-start.sh config/zookeeper.properties**
5. Starts the kafka broker - **bin/kafka-server-start.sh config/server.properties**
6. Create the topic -
   PS C:\kafka_2.13-3.7.0> .\bin\windows\kafka-topics.bat --create --topic exam-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
>> .\bin\windows\kafka-topics.bat --create --topic exam-alerts --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
Created topic exam-events.
7. Open a powershell terminall and start the consumer ( this will show the message processed by samza pipeline). just keep this running
    .\bin\windows\kafka-console-consumer.bat --topic exam-alerts --from-beginning --bootstrap-server localhost:9092
8. Run the Apache Samza job and keep this listening.
   mvn --% compile exec:java -Dexec.mainClass=com.exam.monitor.RealTimeRunner
9. Open another powershell window to feed the data as producer -
    .\bin\windows\kafka-console-producer.bat --topic exam-events --bootstrap-server localhost:9092
    >student_001:VIOLATION_LOOK_AWAY
    >student_001:VIOLATION_TAB_SWITCH
    >student_001:VIOLATION_LOOK_AWAY
    >student_001:VIOLATION_LOOK_AWAY
