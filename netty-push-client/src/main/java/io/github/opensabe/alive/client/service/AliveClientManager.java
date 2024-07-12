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
