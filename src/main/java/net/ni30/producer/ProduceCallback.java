package net.ni30.producer;

import io.vertx.core.json.JsonObject;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public interface ProduceCallback {
    void onSucess(JsonObject jsonObject);
    void onFailure(Throwable throwable);
}
