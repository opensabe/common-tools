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
package io.github.opensabe.scheduler.listener;

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class JobListeners {

    private final List<JobListener> listeners;

    public JobListeners(JobListener... jobListeners){
        this(Arrays.asList(jobListeners));
    }

    public JobListeners(List<JobListener> listeners) {
        this.listeners = new ArrayList<>();
        addAll(listeners);
    }

    public void addAll(List<? extends JobListener> listeners) {
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }
        this.listeners.addAll(listeners);
    }

    public List<JobListener> getListeners() {
        return listeners;
    }
}
