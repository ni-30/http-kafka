package net.ni30.consumer;

import net.ni30.bootstrap.KfkRecord;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class KfkRecordConsumer implements io.vertx.core.Handler<KafkaConsumerRecord<String, JsonObject>> {

    @Override
    public void handle(KafkaConsumerRecord<String, JsonObject> kcr) {
        KfkRecord.Metadata metadata = new KfkRecord.Metadata(kcr.topic(),
                kcr.partition(),
                kcr.offset(),
                kcr.timestamp(),
                kcr.key());

        KfkRecord record = new KfkRecord();
        record.setMetadata(metadata);
        record.setData(kcr.value());

        this.consume(record);
    }

    private Vertx vertx;
    private KfkRecordConsumerOptions options;
    private RedissonClient redissonClient;

    public KfkRecordConsumer(Vertx vertx, KfkRecordConsumerOptions options, RedissonClient redissonClient) {
        this.vertx = vertx;
        this.options = options;
        this.redissonClient = redissonClient;
    }

    protected void consume(KfkRecord record) {
        HttpRedirectVerticle verticle = new HttpRedirectVerticle(record);
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setWorker(true)
                .setMultiThreaded(false);
        HttpVerticleDeploymentHandler deploymentHandler = new HttpVerticleDeploymentHandler(record);

        vertx.deployVerticle(verticle, deploymentOptions, deploymentHandler);
    }

    public class HttpVerticleDeploymentHandler implements Handler<AsyncResult<String>> {
        private KfkRecord record;

        public HttpVerticleDeploymentHandler(KfkRecord record) {
            this.record = record;
        }

        @Override
        public void handle(AsyncResult<String> stringAsyncResult) {
            // TODO
        }

    }

    public class HttpRedirectVerticle extends AbstractVerticle {

        private KfkRecord record;

        public HttpRedirectVerticle(KfkRecord record) {
            super();
            this.record = record;
        }

        @Override
        public void start() throws Exception {
            String router = record.getMetadata().getRouter();
            if(router == null
                    || router.isEmpty()
                    || router.charAt(0) != '/'
                    || router.charAt(router.length() - 1) == '/') {
                return;
            }

            String[] routerArr = router.split("/");
            if(routerArr.length < 3) {
                return;
            }

            RLock lock = null;
            if(!"null".equals(routerArr[routerArr.length - 1])) {
                lock = redissonClient.getLock("kfk-lock:" + options.getGroupId() + ":" + routerArr[routerArr.length - 1]);
            }

            try {
                if(lock != null) {
                    lock.tryLock(options.getHttpRedirect().getTimeout(), options.getHttpRedirect().getTimeout(), TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                start();
                return;
            }

            KfkRecordConsumerOptions.HttpRedirect httpRedirect = options.getHttpRedirect();
            HttpRequest request = options.getWebClient()
                    .post(httpRedirect.getPort(), httpRedirect.getHost(), httpRedirect.getPath() + record.getMetadata().getRouter())
                    .putHeader("Accept", "application/json")
                    .putHeader("Content-Type", "application/json")
                    .putHeader("X-ProducerTimestamp", String.valueOf(System.currentTimeMillis()))
                    .putHeader("X-ConsumerTimestamp", String.valueOf(record.getMetadata().getTimestamp()))
                    .putHeader("X-GroupId", options.getGroupId())
                    .putHeader("X-Topic", String.valueOf(record.getMetadata().getTopic()))
                    .putHeader("X-Partition", String.valueOf(record.getMetadata().getPartition()))
                    .putHeader("X-Offset", String.valueOf(record.getMetadata().getOffset()))
                    .putHeader("X-Router", record.getMetadata().getRouter());

            request.sendJsonObject(record.getData(), new HttpResponseHandler(request, lock));
        }

        @Override
        public void stop() throws Exception {

        }

        public class HttpResponseHandler implements Handler<AsyncResult<HttpResponse>> {

            private HttpRequest request;
            private RLock lock;

            public HttpResponseHandler(HttpRequest request, RLock lock) {
                this.request = request;
                this.lock = lock;
            }

            @Override
            public void handle(AsyncResult<HttpResponse> httpResponseAsyncResult) {
                try {

                } finally {
                    if(lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        }
    }
}
