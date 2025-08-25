/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
