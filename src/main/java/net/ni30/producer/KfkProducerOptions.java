package net.ni30.producer;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class KfkProducerOptions {
    private String name;
    private String bootstrapServers;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

}
