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
package io.github.opensabe.common.compat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Anonymized fixture DTO for persist/wire compat tests (no business identifiers).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 持久化/线格式兼容测试用的匿名化 fixture DTO（不含业务标识）。
 */
public class SamplePayload {
    private String id;
    private LocalDateTime updatedAt;
    private Date createdAt;
    private PlainStatus status;
    private Integer tier;
    private String note;
    private Nested nested;
    private List<String> labels;
    private Map<String, Integer> counts;
    private BigDecimal ratio;
    private UUID token;

    public enum PlainStatus {
        ACTIVE, INACTIVE
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Nested {
        private String ref;
        private double weight;
    }
}
