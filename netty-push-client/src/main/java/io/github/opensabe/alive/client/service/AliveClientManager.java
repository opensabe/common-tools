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
package io.github.opensabe.alive.client.service;

import org.apache.commons.codec.digest.DigestUtils;

import io.github.opensabe.alive.client.Client;
import io.github.opensabe.alive.client.ClientBuilder;
import io.github.opensabe.alive.client.exception.AliveClientException;

/**
 * AliveClientManager 类。
 * <p>Alive 推送ClientManager。</p>
 */
public class AliveClientManager {

/** 产品代码。 */
    private int productCode;
/** salt 字段。 */
    private String salt;
/** client 字段。 */
    private Client client;
/** zkString 字段。 */
    private String zkString;
/** clientNum 字段。 */
    private int clientNum = 1;

/**
 * init 方法。
 */
    public void init() {
        ClientBuilder b = new ClientBuilder();
        b.withZkString(zkString);
        b.withProductCode(productCode);
        if (clientNum > 1) {
            b.withClientNum(clientNum);
        }
        b.withAuthToken(DigestUtils.md5Hex((salt + productCode)));
        client = b.build();
    }

/**
 * destory 方法。
 */
    public void destory() {
        if (client != null) {
            try {
                client.close();
            } catch (AliveClientException e) {
                e.printStackTrace();
            }
        }
    }

/**
 * getClient 方法。
 */
    public Client getClient() {
        return client;
    }

/**
 * getZkString 方法。
 */
    public String getZkString() {
        return zkString;
    }

/**
 * setZkString 方法。
 */
    public void setZkString(String zkString) {
        this.zkString = zkString;
    }

/**
 * getProductCode 方法。
 */
    public int getProductCode() {
        return productCode;
    }

/**
 * setProductCode 方法。
 */
    public void setProductCode(int productCode) {
        this.productCode = productCode;
    }

/**
 * getSalt 方法。
 */
    public String getSalt() {
        return salt;
    }

/**
 * setSalt 方法。
 */
    public void setSalt(String salt) {
        this.salt = salt;
    }

/**
 * getClientNum 方法。
 */
    public int getClientNum() {
        return clientNum;
    }

/**
 * setClientNum 方法。
 */
    public void setClientNum(int clientNum) {
        this.clientNum = clientNum;
    }
}
