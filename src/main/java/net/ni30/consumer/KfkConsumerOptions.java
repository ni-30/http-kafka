package net.ni30.consumer;

import net.ni30.bootstrap.KfkTopic;

import java.util.Set;

/**
 * Created by nitish.aryan on 08/12/17.
 */

public class KfkConsumerOptions {
    private String bootstrapServers;
    private String groupId;
    private Offset autoOffsetReset;
    private boolean enableAutoCommit;
    private Set<KfkTopic> topics;
    private KfkRecordConsumer consumer;

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setAutoOffsetReset(Offset offset) {
        this.autoOffsetReset = offset;
    }

    public Offset getAutoOffsetReset() {
        return this.autoOffsetReset;
    }

    public void setEnableAutoCommit(boolean isEnabled) {
        this.enableAutoCommit = isEnabled;
    }

    public boolean isEnableAutoCommit() {
        return this.enableAutoCommit;
    }

    public void setTopics(Set<KfkTopic> topics) {
        this.topics = topics;
    }

    public Set<KfkTopic> getTopics() {
        return this.topics;
    }

    public void setConsumer(KfkRecordConsumer consumer) {
        this.consumer = consumer;
    }

    public KfkRecordConsumer getConsumer() {
        return this.consumer;
    }

    public enum Offset {
        earliest
    }
}
