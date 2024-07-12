package io.github.opensabe.common.utils.mapstruct.vo;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Data;

import java.util.List;

@Data
@Binding(PersonRecord.class)
public class Person {

    private String name;

    private Address address;

    private List<Book> books;

    @Data
    public static class Address {
        private String code;

        private String name;
    }

    @Data
    public static class Book {
        private String name;
    }
}
