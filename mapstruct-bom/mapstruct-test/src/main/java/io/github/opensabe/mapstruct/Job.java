package io.github.opensabe.mapstruct;


import io.github.opensabe.mapstruct.core.Binding;

@Binding
public class Job {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
