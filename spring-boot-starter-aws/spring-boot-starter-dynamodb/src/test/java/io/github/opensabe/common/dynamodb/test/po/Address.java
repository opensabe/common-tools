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

import java.util.Objects;

import lombok.EqualsAndHashCode;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * @author heng.ma
 */
@EqualsAndHashCode
@DynamoDbBean
public class Address {
/** street。 */
    private String street;
/** city。 */
    private String city;
/** state。 */
    private String state;
/** zipCode。 */
    private String zipCode;

    public Address() {
    }

    /**
     * @return street
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * @param street 待设置值
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return city
     */
    public String getCity() {
        return this.city;
    }

    /**
     * @param city 待设置值
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return state
     */
    public String getState() {
        return this.state;
    }

    /**
     * @param state 待设置值
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return zipCode
     */
    public String getZipCode() {
        return this.zipCode;
    }

    /**
     * @param zipCode 待设置值
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /** {@inheritDoc} — 判断对象相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) && Objects.equals(city, address.city) && Objects.equals(state, address.state) && Objects.equals(zipCode, address.zipCode);
    }

    /** {@inheritDoc} — 返回哈希码。 */
    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, zipCode);
    }

    /** {@inheritDoc} — 返回字符串表示。 */
    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                '}';
    }
}
