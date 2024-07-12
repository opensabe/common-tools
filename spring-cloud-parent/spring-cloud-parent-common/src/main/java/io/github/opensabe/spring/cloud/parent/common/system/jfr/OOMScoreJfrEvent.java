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
