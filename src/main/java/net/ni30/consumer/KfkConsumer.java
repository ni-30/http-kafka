package net.ni30.consumer;

import net.ni30.bootstrap.KfkTopic;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;

/**
 * Created by nitish.aryan on 08/12/17.
 */
public class KfkConsumer implements Closeable {

    protected final KfkConsumerOptions options;
    protected final Vertx vertx;
    protected KafkaConsumer<String, JsonObject> kfkConsumer;

    public KfkConsumer(Vertx vertx, KfkConsumerOptions options) {
        this.vertx = vertx;
        this.options = options;
    }

    public void init() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "io.vertx.kafka.client.serialization.JsonObjectSerializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.vertx.kafka.client.serialization.JsonObjectSerializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, options.getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, options.getAutoOffsetReset().name());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, options.isEnableAutoCommit());

        kfkConsumer = KafkaConsumer.create(vertx, props);

        Set<String> topics = new HashSet<>();
        for(KfkTopic t : options.getTopics()) {
            topics.add(t.getTopic());
        }

        kfkConsumer.subscribe(topics, new SubscribeHandler());
    }


    @Override
    public void close() {
        if(this.kfkConsumer != null) {
            this.kfkConsumer.close();
        }
    }

    public class SubscribeHandler implements io.vertx.core.Handler<AsyncResult<Void>> {

        @Override
        public void handle(AsyncResult<Void> voidAsyncResult) {
            if(voidAsyncResult.succeeded()) {
                KfkRecordConsumer consumer = options.getConsumer();
                kfkConsumer.handler(consumer);
            } else {
                vertx.close();
                voidAsyncResult.cause().printStackTrace();
            }
        }

    }

}
