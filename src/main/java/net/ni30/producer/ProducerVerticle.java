package net.ni30.producer;

import net.ni30.bootstrap.KfkRecord;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class ProducerVerticle extends AbstractVerticle implements Handler<HttpServerRequest> {
    private ProducerVerticleOptions options;
    private KfkProducer producer;
    private HttpServer httpServer;

    public ProducerVerticle(ProducerVerticleOptions options) {
        super();
        this.options = options;
    }

    public void start() throws Exception {
        producer = new KfkProducer(this.getVertx(), options.getKfkProducerOptions());
        httpServer = getVertx().createHttpServer();
        httpServer.requestHandler(this);
        httpServer.listen(options.getPort());
    }

    public void stop() throws Exception {
        if(httpServer != null) {
            httpServer.close();
        }

        if(producer != null) {
            producer.close();
        }
    }

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        if(httpServerRequest.path().equals(options.getPath())) {
            final String topic = httpServerRequest.getHeader("X-Topic");
            final String router = httpServerRequest.getHeader("X-Router");
            if(topic == null || topic.isEmpty()) {
                badRequest(httpServerRequest.response(), "X-Topic is missing/invalid in header");
            } else if(router == null
                    || router.isEmpty()
                    || router.split("/").length < 3
                    || router.charAt(0) != '/'
                    || router.charAt(router.length() - 1) == '/') {
                badRequest(httpServerRequest.response(), "X-Router is missing/invalid in header");
            } else {
                getVertx().executeBlocking(future -> {
                    httpServerRequest.bodyHandler(buffer -> {
                        KfkRecord.Metadata metadata = new KfkRecord.Metadata(topic, -1, -1, -1, router);
                        KfkRecord record = new KfkRecord();
                        record.setMetadata(metadata);
                        record.setData(buffer.toJsonObject());
                        future.complete(record);
                    });
                }, false, result -> {
                    producer.push((KfkRecord) result.result(), new HttpRequestCallback(httpServerRequest.response()));
                });
            }
        } else {
            badRequest(httpServerRequest.response(), "invalid http path");
        }
    }

    protected void badRequest(HttpServerResponse response, String message) {
        response.setStatusCode(400)
                .setStatusMessage("BadRequest: " + message)
                .end();
    }

    public class HttpRequestCallback implements ProduceCallback {
        private HttpServerResponse response;

        public HttpRequestCallback(HttpServerResponse response) {
            this.response = response;
        }

        @Override
        public void onSucess(JsonObject jsonObject) {
            response.putHeader("X-Producer-Timestamp", String.valueOf(jsonObject.getLong("timestamp")))
                    .putHeader("X-Topic", jsonObject.getString("topic"))
                    .putHeader("X-Partition", String.valueOf(jsonObject.getInteger("partition")))
                    .putHeader("X-Offset", String.valueOf(jsonObject.getLong("offset")))
                    .setStatusCode(200)
                    .end();
        }

        @Override
        public void onFailure(Throwable throwable) {
            response.setStatusCode(500)
                    .setStatusMessage("Error: " + throwable.getLocalizedMessage())
                    .end();
        }
    }

}
