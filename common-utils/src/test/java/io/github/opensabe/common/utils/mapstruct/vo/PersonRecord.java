package io.github.opensabe.common.utils.mapstruct.vo;

import java.util.List;

public record PersonRecord (String name, AddressRecord address, List<BookRecord> books) {

    public record AddressRecord (String code, String name) {}

    public record BookRecord (String name) {}

}
