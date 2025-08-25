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
package io.github.opensabe.spring.cloud.parent.common.system.jfr;

import jdk.jfr.*;
import lombok.Getter;

@Category({"OOM Score Monitoring Recording"})
@Label("OOM Score JFR")
@Description("it manages to record the metrics in OOM Score Monitoring such as oom_adj and oom_score and oom_score_adj")
@StackTrace(false)
public class OOMScoreJfrEvent extends Event {
    @Getter
    @Label("oom_adj")
    private final long oomAdj;
    @Getter
    @Label("oom_score")
    private final long oomScore;
    @Getter
    @Label("oom_score_adj")
    private final long oomScoreAdj;

    public OOMScoreJfrEvent(long oomAdj, long oomScore, long oomScoreAdj) {
        this.oomAdj = oomAdj;
        this.oomScore = oomScore;
        this.oomScoreAdj = oomScoreAdj;
    }
}
