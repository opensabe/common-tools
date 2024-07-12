package io.github.opensabe.alive.client.callback;

import io.github.opensabe.alive.protobuf.Message;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lone
 */
public class CallbackManager {

    //requestId To count
    private static Map<Integer, AtomicInteger> countMap = new ConcurrentHashMap<>();
    //requestId to responseSet
    private static Map<Integer, Set<Message.Response>> responseMap = new HashMap<>();
    //requestId to callback
    private static Map<Integer, ClientCallback> callbackMap = new ConcurrentHashMap<>();

    public static void addTask(int requestId, AtomicInteger count, ClientCallback callback) {
        count.incrementAndGet();
        countMap.put(requestId, count);
        callbackMap.put(requestId, callback);
    }

    public static void finishTask(Message.Response response) {
        int requestId = response.getRequestId();
        AtomicInteger count = countMap.get(requestId);
        if (count == null) {
            return;
        }
        synchronized (responseMap) {
            Set<Message.Response> responses = responseMap.get(requestId);
            if (responses == null) {
                responses = new HashSet<>();
                responseMap.put(requestId, responses);
            }
            responses.add(response);
            int i = count.decrementAndGet();
            if (i == 0) {
                ClientCallback callback = callbackMap.remove(requestId);
                responseMap.remove(requestId);
                callback.opComplete(responses);
                countMap.remove(requestId);
            }
        }
    }
}
