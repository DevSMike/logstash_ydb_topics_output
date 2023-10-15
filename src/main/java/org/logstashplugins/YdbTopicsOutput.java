package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.Output;
import co.elastic.logstash.api.PluginConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.ydb.core.grpc.GrpcTransport;
import tech.ydb.topic.TopicClient;
import tech.ydb.topic.write.AsyncWriter;
import tech.ydb.topic.write.Message;
import tech.ydb.topic.settings.WriterSettings;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;


/**
 * @author Mikhail Lukashev
 */
@LogstashPlugin(name = "ydb_topics_output")
public class YdbTopicsOutput implements Output {

    public static final PluginConfigSpec<String> PREFIX_CONFIG = PluginConfigSpec.stringSetting("prefix", "");

    private final Logger logger = LoggerFactory.getLogger(YdbTopicsOutput.class);

    private final String topicPath;
    private final String producerId;
    private final String connectionString;
    private final CompletableFuture<Void> initFuture;
    private GrpcTransport transport;
    private TopicClient topicClient;
    private AsyncWriter asyncWriter;
    private final String id;
    private final String prefix;
    private volatile boolean stopped = false;
    private final Object initLock = new Object();

    public YdbTopicsOutput(String id, Configuration config, Context context) {
        this.topicPath = config.get(PluginConfigSpec.stringSetting("topic_path"));
        this.connectionString = config.get(PluginConfigSpec.stringSetting("connection_string"));

        //Producer id передавать в конфиге ??
        this.producerId = "your_producer_id";


        this.id = id;
        this.prefix = config.get(PREFIX_CONFIG);
        this.initFuture = initAsyncWriter();
    }

    @Override
    public void output(Collection<Event> events) {
        try {
            // проверка, что асинхронная операция инициализации писателя завершилась
            if (initFuture != null) {
                initFuture.get();
            }

            Iterator<Event> z = events.iterator();
            while (z.hasNext() && !stopped) {
                String s = prefix + z.next();
                sendMessage(s);
            }

        } catch (Exception e) {
            logger.error("Error sending messages to YDB Topics: " + e.getMessage(), e);
        }
    }

    //чтобы избежать задержек, не блокируем основной поток инициализацией
    public CompletableFuture<Void> initAsyncWriter() {
        return CompletableFuture.runAsync(() -> {
            try {
                synchronized (initLock) {
                    transport = GrpcTransport.forConnectionString(connectionString).build();
                    topicClient = TopicClient.newClient(transport).build();

                    WriterSettings settings = WriterSettings.newBuilder()
                            .setTopicPath(topicPath)
                            .setProducerId(producerId)
                            .setMessageGroupId(producerId)
                            .build();

                    asyncWriter = topicClient.createAsyncWriter(settings);

                    asyncWriter.init()
                            .thenRun(() -> logger.info("YDB Topics AsyncWriter initialized successfully"))
                            .exceptionally(ex -> {
                                logger.error("YDB Topics AsyncWriter initialization failed with ex: ", ex);
                                return null;
                            });
                }
            } catch (Exception e) {
                logger.error("Failed to initialize YDB Topics Output: " + e.getMessage(), e);
            }
        });
    }

    private void sendMessage(String message) {
        if (asyncWriter != null) {
            try {
                asyncWriter.send(Message.of(message.getBytes()));
            } catch (Exception e) {
                logger.error("Error sending message to YDB Topics: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        if (asyncWriter != null) {
            asyncWriter.shutdown();
        }
        if (transport != null) {
            transport.close();
        }
        stopped = true;
    }

    @Override
    public void awaitStop() throws InterruptedException {
        // Implement awaitStop if needed
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Collections.singletonList(PREFIX_CONFIG);
    }

    @Override
    public String getId() {
        return id;
    }
}
