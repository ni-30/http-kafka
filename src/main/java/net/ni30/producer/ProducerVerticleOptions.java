package net.ni30.producer;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class ProducerVerticleOptions {
    private KfkProducerOptions kfkProducerOptions;
    private int port;
    private String path;

    public KfkProducerOptions getKfkProducerOptions() {
        return this.kfkProducerOptions;
    }

    public int getPort() {
        return this.port;
    }

    public String getPath() {
        return this.path;
    }
}
