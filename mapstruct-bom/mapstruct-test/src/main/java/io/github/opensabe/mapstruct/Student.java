package io.github.opensabe.mapstruct;

import io.github.opensabe.mapstruct.core.Binding;

@Binding
public class Student {
    private String name;

    private Integer age;

    private Grade job;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Grade getJob() {
        return job;
    }

    public void setJob(Grade job) {
        this.job = job;
    }
}
