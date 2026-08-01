package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;

import io.github.opensabe.common.utils.mapstruct.vo.CustomerDto;
import io.github.opensabe.common.utils.mapstruct.vo.Customer;

@Mapper
public interface CustomerDtoCustomerMapper extends CommonCopyMapper<CustomerDto, Customer> {

}
