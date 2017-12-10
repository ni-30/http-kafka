package net.ni30.producer;


import net.ni30.bootstrap.KfkRecord;

import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.Closeable;
import java.util.Properties;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class KfkProducer implements Closeable {
    private Vertx vertx;
    private KfkProducerOptions options;
    private KafkaProducer<String, JsonObject> producer;

    public KfkProducer(Vertx vertx, KfkProducerOptions kfkProducerOptions) {
        this.vertx = vertx;
        this.options = kfkProducerOptions;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "io.vertx.kafka.client.serialization.JsonObjectSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.vertx.kafka.client.serialization.JsonObjectSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        this.producer = KafkaProducer.createShared(vertx, options.getName(), props);
    }

    public void push(KfkRecord kfkRecord, ProduceCallback callback) {
        this.vertx.executeBlocking(future -> {
            KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create(kfkRecord.getMetadata().getTopic(), kfkRecord.getMetadata().getTopic(), kfkRecord.getData());
            producer.write(record, handler -> {
                if (handler.succeeded()) {
                    future.complete(handler.result().toJson());
                } else {
                    if(handler.cause() == null) {
                        future.fail("unknown failure");
                    } else {
                        future.fail(handler.cause());
                    }
                }
            });
        }, false,  result -> {
            if(result.succeeded()) {
                callback.onSucess((JsonObject) result.result());
            } else {
                callback.onFailure(result.cause());
            }
        });
    }

    @Override
    public void close() {
        if(this.producer != null) {
            this.producer.close();
        }
    }
}
