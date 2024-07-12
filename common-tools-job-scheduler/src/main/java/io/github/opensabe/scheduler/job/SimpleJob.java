package io.github.opensabe.scheduler.job;

import java.io.Serializable;

public interface SimpleJob extends Serializable {
    void execute() ;
}
