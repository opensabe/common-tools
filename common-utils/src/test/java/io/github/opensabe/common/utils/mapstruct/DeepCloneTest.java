package io.github.opensabe.common.utils.mapstruct;

import io.github.opensabe.common.utils.mapstruct.vo.*;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class DeepCloneTest {

    @Test
    void test () {

        CustomerDto customerDto = new CustomerDto();
        customerDto.setId( 10L );
        customerDto.setCustomerName("Jaques" );
        OrderItemDto order1 = new OrderItemDto();
        order1.setName ("Table" );
        order1.setQuantity( 2L );
        customerDto.setOrders( new ArrayList<>( Collections.singleton( order1 ) ) );
        OrderItemKeyDto key = new OrderItemKeyDto();
        key.setStockNumber( 5 );
        Map stock = new HashMap(  );
        stock.put( key, order1 );
        customerDto.setStock( stock );

        CommonCopyMapper<CustomerDto, Customer> mapper = MapperRepository.getInstance().getMapper(CustomerDto.class, Customer.class);
        Customer customer = mapper.map(customerDto);
        // check if cloned
        assertThat( customer.getId() ).isEqualTo( 10 );
        assertThat( customer.getCustomerName() ).isEqualTo( "Jaques" );
        assertThat( customer.getOrders() )
                .extracting( "name", "quantity" )
                .containsExactly( tuple( "Table", 2L ) );
        assertThat( customer.getStock()  ).isNotNull();
        assertThat( customer.getStock() ).hasSize( 1 );

        Map.Entry<OrderItemKeyDto, OrderItemDto> entry = customer.getStock().entrySet().iterator().next();
        assertThat( entry.getKey().getStockNumber() ).isEqualTo( 5 );
        assertThat( entry.getValue().getName() ).isEqualTo( "Table" );
        assertThat( entry.getValue().getQuantity() ).isEqualTo( 2L );

        // check mapper really created new objects
        assertThat( customer ).isNotSameAs( customerDto );
        assertThat( customer.getOrders().get( 0 ) ).isEqualTo( order1 );
        assertThat( entry.getKey() ).isEqualTo( key );
        assertThat( entry.getValue() ).isEqualTo( order1 );
        assertThat( entry.getValue() ).isEqualTo( customer.getOrders().get( 0 ) );
    }

    @Test
    void testInnerType () {
        var book1 = new PersonRecord.BookRecord("book1");
        var book2 = new PersonRecord.BookRecord("book2");
        var address = new PersonRecord.AddressRecord("001", "NewYork");
        var record = new PersonRecord("tom", address, List.of(book1, book2));
        CommonCopyMapper<PersonRecord, Person> mapper = MapperRepository.getInstance().getMapper(PersonRecord.class, Person.class);
        Person person = mapper.map(record);
        Assertions.assertEquals("tom", person.getName());
        Assertions.assertEquals("001", person.getAddress().getCode());
        Assertions.assertEquals("NewYork", person.getAddress().getName());
        Assertions.assertEquals(2, person.getBooks().size());
        assertThat(person.getBooks()).extracting(Person.Book::getName).containsExactly("book1", "book2");
    }
}
