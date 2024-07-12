package io.github.opensabe.alive.client.service;

import io.github.opensabe.alive.client.Client;
import io.github.opensabe.alive.client.exception.AliveClientRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliveClientProxyManager {
    private List<AliveClientManager> managerList;

    private Map<String, AliveClientManager> managerMap;

    public void init() {
        managerMap = new HashMap<String, AliveClientManager>();
        if (managerList != null) {
            for (AliveClientManager manager : managerList) {
                managerMap.put(Integer.toString(manager.getProductCode()), manager);
            }
        }
    }

    public Client getClient(Integer productCode) {
        if (productCode == null) {
            throw new IllegalArgumentException("product code is null");
        }
        AliveClientManager clientManager = managerMap.get(Integer.toString(productCode));
        if (clientManager == null) {
            throw new AliveClientRuntimeException("product manager not config, product code = " + productCode);
        } else {
            return clientManager.getClient();
        }
    }

    public Client getClient(String productCode) {
        if (productCode == null) {
            throw new IllegalArgumentException("product code is null");
        }
        AliveClientManager clientManager = managerMap.get(productCode);
        if (clientManager == null) {
            throw new AliveClientRuntimeException("product manager not config, product code = " + productCode);
        } else {
            return clientManager.getClient();
        }
    }

    public List<AliveClientManager> getManagerList() {
        return managerList;
    }

    public void setManagerList(List<AliveClientManager> managerList) {
        this.managerList = managerList;
    }

}
