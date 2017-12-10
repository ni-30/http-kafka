package net.ni30.bootstrap;

import net.ni30.consumer.ConsumerVerticleOptions;
import net.ni30.producer.ProducerVerticleOptions;

import java.util.List;

import io.vertx.core.json.JsonObject;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class HttpKafkaOptions {
    private String groupId;
    private String bootstrapServers;
    private HttpHost processorHost;
    private HttpHost publisherHost;
    private RedisHost redisHost;
    private ProducerVerticleOptions producerVerticleOptions;
    private List<ConsumerVerticleOptions> consumerVerticleOptions;

    public HttpKafkaOptions(JsonObject configObject) {
        // TODO
    }

    public HttpHost getProcessorHost() {
        return this.processorHost;
    }

    public RedisHost getRedisHost() {
        return this.redisHost;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setConsumerVerticleOptions(List<ConsumerVerticleOptions> options) {
        this.consumerVerticleOptions = options;
    }

    public List<ConsumerVerticleOptions> getConsumerVerticleOptions() {
        return this.consumerVerticleOptions;
    }

    public ProducerVerticleOptions getProducerVerticleOptions() {
        return this.producerVerticleOptions;
    }

    public static class HttpHost {
        private final String host;
        private final int port;
        private final String path;

        public HttpHost(String host, int port, String path) {
            this.host = host;
            this.port = port;
            this.path = path;
        }

        public String getHost() {
            return this.host;
        }

        public int getPort() {
            return this.port;
        }

        public String getPath() {
            return this.path;
        }
    }

    public static class RedisHost {
        private String host;
        private int port;

        public RedisHost(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return this.host;
        }

        public int getPort() {
            return this.port;
        }
    }
}
