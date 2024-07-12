package io.github.opensabe.scheduler.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum JobExecuteObservationDocumentation implements ObservationDocumentation {
    JOB_EXECUTE {
        @Override
        public String getName() {
            return "job.execute";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return JobExecuteObservationConvention.class;
        }
    };

    public enum JOB_EXECUTE_TAG implements KeyName {
        JOB_EXECUTE_STATUS {
            @Override
            public String asString() {
                return "job.execute.status";
            }
        }
        ;
    }
}
