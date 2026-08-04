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
package io.github.opensabe.common.dynamodb.test.po;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * @author heng.ma
 */
@EqualsAndHashCode
@DynamoDbBean
public class Person {
/** id。 */
    private Integer id;
/** firstName。 */
    private String firstName;
/** lastName。 */
    private String lastName;
/** age。 */
    private Integer age;
/** mainAddress。 */
    private Address mainAddress;
/** addresses。 */
    private Map<String, Address> addresses;
/** phoneNumbers。 */
    private List<PhoneNumber> phoneNumbers;
/** hobbies。 */
    private Set<String> hobbies;

    /**
     * @return id
     */
    @DynamoDbPartitionKey
    public Integer getId() {
        return id;
    }

    /**
     * @param id 待设置值
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName 待设置值
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName 待设置值
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return age
     */
    public Integer getAge() {
        return age;
    }

    /**
     * @param age 待设置值
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * @return mainAddress
     */
    public Address getMainAddress() {
        return mainAddress;
    }

    /**
     * @param mainAddress 待设置值
     */
    public void setMainAddress(Address mainAddress) {
        this.mainAddress = mainAddress;
    }

    /**
     * @return addresses
     */
    public Map<String, Address> getAddresses() {
        return addresses;
    }

    /**
     * @param addresses 待设置值
     */
    public void setAddresses(Map<String, Address> addresses) {
        this.addresses = addresses;
    }

    /**
     * @return phoneNumbers
     */
    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * @param phoneNumbers 待设置值
     */
    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    /**
     * @return hobbies
     */
    public Set<String> getHobbies() {
        return hobbies;
    }

    /**
     * @param hobbies 待设置值
     */
    public void setHobbies(Set<String> hobbies) {
        this.hobbies = hobbies;
    }

    /** {@inheritDoc} — 返回字符串表示。 */
    @Override
    public String toString() {
        return "Person{" +
                "addresses=" + addresses +
                ", id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", mainAddress=" + mainAddress +
                ", phoneNumbers=" + phoneNumbers +
                ", hobbies=" + hobbies +
                '}';
    }
}
