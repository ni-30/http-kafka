package net.ni30.bootstrap;

import net.ni30.consumer.ConsumerVerticle;
import net.ni30.consumer.ConsumerVerticleOptions;
import net.ni30.consumer.KfkRecordConsumer;
import net.ni30.consumer.KfkRecordConsumerOptions;
import net.ni30.producer.ProducerVerticle;

import org.redisson.Redisson;
import org.redisson.config.Config;

import java.util.Iterator;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.client.WebClient;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class KfkProcessorMain {

    public static void main(String[] args) {
        final JsonObject configObject = new JsonObject(); // TODO

        ClusterManager clusterManager = new HazelcastClusterManager();
        VertxOptions options = new VertxOptions()
                .setClusterManager(clusterManager)
                .setBlockedThreadCheckInterval(1000) //ms
                .setEventLoopPoolSize(2)
                .setWorkerPoolSize(20)
                .setInternalBlockingPoolSize(20)
                .setClusterPingInterval(20000) //ms
                .setClusterPingReplyInterval(20000) //ms
                .setMaxWorkerExecuteTime(60L * 1000 * 1000000) //ns
                .setQuorumSize(1)
                .setWarningExceptionTime(5L * 1000 * 1000000) //ns
                .setClustered(true)
                .setClusterHost("127.0.0.1")
                .setClusterPort(5701);

        Vertx.clusteredVertx(options, new VertxHandler(configObject));
    }

    public static class VertxHandler implements Handler<AsyncResult<Vertx>> {
        private JsonObject configObject;

        public VertxHandler(JsonObject configObject) {
            this.configObject = configObject;
        }

        @Override
        public void handle(AsyncResult<Vertx> event) {
            if(event.failed()) {
                event.cause().printStackTrace();
                return;
            }

            final Vertx vertx = event.result();
            final HttpKafkaOptions options = new HttpKafkaOptions(configObject);

            ProducerVerticle producerVerticle = new ProducerVerticle(options.getProducerVerticleOptions());
            vertx.deployVerticle(producerVerticle, handler -> {
                if(handler.failed()) {
                    vertx.close();
                    handler.cause().printStackTrace();
                    return;
                }

                KfkRecordConsumerOptions kfkRecordConsumerOptions = new KfkRecordConsumerOptions();
                kfkRecordConsumerOptions.setGroupId(options.getGroupId());
                kfkRecordConsumerOptions.setHttpRedirect(new KfkRecordConsumerOptions.HttpRedirect(options.getProcessorHost().getHost(),
                        options.getProcessorHost().getPort(),
                        options.getProcessorHost().getPath(),
                        120000));
                kfkRecordConsumerOptions.setWebClient(WebClient.create(vertx));

                Config redissonConfig = new Config();
                redissonConfig.useSingleServer()
                        .setAddress(options.getRedisHost().getHost() + ":" + options.getRedisHost().getPort());

                KfkRecordConsumer consumer = new KfkRecordConsumer(vertx, kfkRecordConsumerOptions, Redisson.create(redissonConfig));

                Iterator<ConsumerVerticleOptions> cvOptions = options.getConsumerVerticleOptions().iterator();
                Handler<AsyncResult<Void>> resultHandler = event1 -> {
                    if(event1.failed()) {
                        vertx.close();
                        event1.cause().printStackTrace();
                    }
                };
                Handler<Future<Void>> blockingCodeHandler = new Handler<Future<Void>>() {
                    @Override
                    public void handle(Future<Void> event) {
                        if(!cvOptions.hasNext()) {
                            event.complete();
                            return;
                        }

                        ConsumerVerticleOptions cvo = cvOptions.next();

                        cvo.getKfkConsumerOptions().setConsumer(consumer);

                        ConsumerVerticle verticle = new ConsumerVerticle(cvo);
                        DeploymentOptions deploymentOptions = new DeploymentOptions()
                                .setMultiThreaded(true);

                        final Handler<Future<Void>> futureHandler = this;
                        vertx.deployVerticle(verticle, deploymentOptions, handler2 -> {
                            if(handler2.succeeded()) {
                                vertx.executeBlocking(futureHandler, resultHandler);
                            } else {
                                event.fail(handler2.cause());

                            }
                        });

                        event.complete();
                    }
                };
                vertx.executeBlocking(blockingCodeHandler, resultHandler);
            });
        }
    }

}
