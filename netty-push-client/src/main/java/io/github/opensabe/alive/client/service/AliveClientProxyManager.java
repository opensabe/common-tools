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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.opensabe.alive.client.Client;
import io.github.opensabe.alive.client.exception.AliveClientRuntimeException;

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
