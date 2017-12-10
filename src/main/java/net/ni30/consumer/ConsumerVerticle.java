package net.ni30.consumer;

import io.vertx.core.AbstractVerticle;

/**
 * Created by nitish.aryan on 08/12/17.
 */
public class ConsumerVerticle extends AbstractVerticle {
    private KfkConsumer consumer;
    private ConsumerVerticleOptions options;

    public ConsumerVerticle(ConsumerVerticleOptions options) {
        super();
        this.options = options;
    }

    public void start() throws Exception {
        consumer = new KfkConsumer(this.getVertx(), options.getKfkConsumerOptions());
        consumer.init();
    }

    public void stop() throws Exception {
        if(consumer != null) {
            consumer.close();
        }
    }
}
