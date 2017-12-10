package net.ni30.consumer;

/**
 * Created by nitish.aryan on 08/12/17.
 */
public class ConsumerVerticleOptions {
    private KfkConsumerOptions kfkConsumerOptions;

    public KfkConsumerOptions getKfkConsumerOptions() {
        return this.kfkConsumerOptions;
    }

    public void setKfkConsumerOptions(KfkConsumerOptions kfkConsumerOptions) {
        this.kfkConsumerOptions = kfkConsumerOptions;
    }

}
