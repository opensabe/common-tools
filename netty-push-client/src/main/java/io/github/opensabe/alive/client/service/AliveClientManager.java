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

public class AliveClientManager {

    private int productCode;
    private String salt;
    private Client client;
    private String zkString;
    private int clientNum = 1;

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

    public void destory() {
        if (client != null) {
            try {
                client.close();
            } catch (AliveClientException e) {
                e.printStackTrace();
            }
        }
    }

    public Client getClient() {
        return client;
    }

    public String getZkString() {
        return zkString;
    }

    public void setZkString(String zkString) {
        this.zkString = zkString;
    }

    public int getProductCode() {
        return productCode;
    }

    public void setProductCode(int productCode) {
        this.productCode = productCode;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getClientNum() {
        return clientNum;
    }

    public void setClientNum(int clientNum) {
        this.clientNum = clientNum;
    }
}
