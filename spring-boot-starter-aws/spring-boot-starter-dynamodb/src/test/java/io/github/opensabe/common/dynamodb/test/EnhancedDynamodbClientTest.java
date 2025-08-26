/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.dynamodb.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.opensabe.common.dynamodb.test.common.DynamicdbStarter;
import io.github.opensabe.common.dynamodb.test.po.Address;
import io.github.opensabe.common.dynamodb.test.po.Person;
import io.github.opensabe.common.dynamodb.test.po.PhoneNumber;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * @author heng.ma
 */
public class EnhancedDynamodbClientTest extends DynamicdbStarter {

    @Autowired
    private DynamoDbClient dynamoDbClient;


    @Test
    void test() {
        DynamoDbEnhancedClient client = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        DynamoDbTable<Person> table = client.table("test_table", TableSchema.fromBean(Person.class));
        table.createTable();


        Person person = new Person();

        person.setAge(10);
        person.setId(1);
        person.setHobbies(Set.of("basketball", "football"));
        person.setFirstName("heng");
        person.setLastName("ma");

        Address home = new Address();
        home.setCity("beijing");
        home.setState("beijing");
        home.setZipCode("010");
        home.setStreet("zhongguancun");

        Address work = new Address();
        work.setStreet("qiaoxi");
        work.setState("hebei");
        work.setCity("shijiazhuang");
        work.setZipCode("130");

        person.setAddresses(Map.of("home", home, "work", work));

        Address main = new Address();
        main.setZipCode("120");
        main.setCity("tianjin");
        main.setState("tianjin");
        main.setStreet("weierdao");

        person.setMainAddress(main);

        person.setMainAddress(main);

        PhoneNumber mobile = new PhoneNumber();
        mobile.setNumber("123456");
        mobile.setType("mobile");
        PhoneNumber telephone = new PhoneNumber();
        telephone.setType("telephone");
        telephone.setNumber("45937203");
        person.setPhoneNumbers(List.of(mobile, telephone));

        table.putItem(person);

        Person item = table.getItem(Key.builder().partitionValue(1).build());
        Assertions.assertEquals(item, person);
    }
}
