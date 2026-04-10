# **STEP 0 — What you need installed**

Run these one by one in Terminal:

```
java -version
mvn -version
docker --version
docker compose version
kubectl version --client
```

### You should have:

- **Java 8 or 11** (usually safest for Samza projects)
- **Maven**
- **Docker Desktop**

# **STEP 1 — Open the project folder**

```
cd ~/Downloads
git clone https://github.com/jinugeorge2290/kafka-samza-cheating-detector.git
cd kafka-samza-cheating-detector
```

If you already downloaded it:

```
cd /path/to/kafka-samza-cheating-detector
```

# **STEP 2 — Check project structure**

Run:

```
ls
```

You should see files/folders like:

```
docker-compose.yml
pom.xml
src/
README.md
```

If yes, good.

# **STEP 3 — Start Kafka + Zookeeper**

This is the most important step.

Run:

```
docker compose up -d
```

Then verify:

```
docker ps
```

You should see containers like:

- `zookeeper`
- `kafka`

If they are running → good.

# **STEP 4 — Check Kafka is healthy**

Run:

```
docker logs kafka --tail 50
```

or if the container name is different:

```
docker ps
```

Look for the Kafka container name, then:

```
docker logs <your-kafka-container-name> --tail 50
```

You want to see Kafka started successfully.

# **STEP 5 — Create Kafka topics**

Your project uses these topics:

- **`exam-events`** → input
- **`exam-alerts`** → output

Run this inside the Kafka container.

```
docker exec -it kafka  kafka-topics --create --topic exam-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

docker exec -it kafka kafka-topics --create --topic exam-alerts --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

List topics to confirm:

```
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

You should see:

```
exam-events
exam-alerts
```

# **STEP 6 — Build the project**

Run:

cd to parent directory kafka-samza-cheating-detector

In my case: /Users/macminii7/OBSIDIAN/BITS-PILANI/StreamProcessing-Assignment2/kafka-samza-cheating-detector

```
cd /Users/macminii7/OBSIDIAN/BITS-PILANI/StreamProcessing-Assignment2/kafka-samza-cheating-detector
```

```
mvn clean compile
```

If successful, Maven should finish without errors.

# **STEP 8 — Start the Kafka output consumer first - Terminal 1**

Before running Samza, open a **new Terminal tab** and listen for alerts.

```
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic exam-alerts
```

Keep this terminal open.

This will show **alerts produced by Samza**.

If you wish to see all previous alerts as well use below command —from-beginning

```
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic exam-alerts --from-beginning
```

# **STEP 9 — Start the Samza job - Terminal 2 **

Go back to your main terminal (project folder) and run:

```
mvn exec:java -Dexec.mainClass=com.exam.monitor.RealTimeRunner
```

This should start:

- Kafka consumer in Samza
- Real-time processing
- Alert generation

If it starts successfully, your Samza pipeline is now live.

```
[INFO] Scanning for projects...
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/mojo/maven-metadata.xml
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-metadata.xml
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-metadata.xml (14 kB at 24 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/mojo/maven-metadata.xml (21 kB at 32 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/mojo/exec-maven-plugin/maven-metadata.xml
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/mojo/exec-maven-plugin/maven-metadata.xml (1.1 kB at 32 kB/s)
[INFO]
[INFO] ----------------< com.exam.monitor:cheating-detection >-----------------
[INFO] Building cheating-detection 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[WARNING] 2 problems were encountered while building the effective model for org.apache.yetus:audience-annotations:jar:0.5.0 during dependency collection step for project (use -X to see details)
[INFO]
[INFO] --- exec:3.6.3:java (default-cli) @ cheating-detection ---
===== EXAM CHEATING DETECTION SYSTEM STARTED =====
log4j:WARN No appenders could be found for logger (org.apache.kafka.clients.consumer.ConsumerConfig).
log4j:WARN Please initialize the log4j system properly.
Listening to topic: exam-events
Publishing alerts to topic: exam-alerts
```

# **STEP 10 — Send test events into Kafka - Terminal 3**

Open **another terminal tab** and enter Kafka container:

Start a producer:

```
docker exec -it kafka kafka-console-producer --broker-list localhost:9092 --topic exam-events
```

Now type test messages one by one on Terminal 3 - kafka-console-producer

```
student1:login
student2:login
student3:login
student1:question_opened_1
student2:question_opened_1
student1:copy_detected
student2:copy_detected
student3:independent_work
student1:submit
student2:submit
student3:submit
```

---

### ALERT: Potential Cheating by student1, student2

```
ALERT: student1 suspected cheating (copy_detected event)
ALERT: student2 suspected cheating (copy_detected event)
student3: normal behavior
```

---

```
student1
login
question_opened_1
copy_detected (very important signal)
submit

Strong cheating indicator

student2
login
question_opened_1
copy_detected
submit

Also strong cheating indicator

student3
login 
question_opened_1  (no interaction overlap)
independent_work 
submit 

Clean behavior → likely NOT cheating
```

## Key Features
```
- Real-time stream processing
- Violation tracking per student
- Alert generation after threshold
- Kafka-based event pipeline
```
