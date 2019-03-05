package io.elastest.etm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class SharedAsyncModel<T> {

    Future<T> future;
    Map<String, Object> data;

    public SharedAsyncModel() {
        this.data = new HashMap<>();
    }

    public SharedAsyncModel(Future<T> future) {
        this.future = future;
        this.data = new HashMap<>();
    }

    public SharedAsyncModel(Future<T> future, Map<String, Object> data) {
        this.future = future;
        this.data = data;
    }

    public Future<T> getFuture() {
        return future;
    }

    public void setFuture(Future<T> future) {
        this.future = future;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SharedAsyncModel [future=" + future + ", data=" + data + "]";
    }

}
