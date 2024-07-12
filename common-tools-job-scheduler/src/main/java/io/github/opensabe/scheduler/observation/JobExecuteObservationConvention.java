package io.github.opensabe.scheduler.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class JobExecuteObservationConvention implements ObservationConvention<JobExecuteContext> {
    public static final JobExecuteObservationConvention DEFAULT = new JobExecuteObservationConvention();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof JobExecuteContext;
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(JobExecuteContext context) {
        return KeyValues.of(JobExecuteObservationDocumentation.JOB_EXECUTE_TAG.JOB_EXECUTE_STATUS.withValue(context.getJobName() + "-" +context.getStatus().name()));
    }

}
