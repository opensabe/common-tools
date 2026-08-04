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
package io.github.opensabe.common.mybatis.test.po;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.github.opensabe.common.mybatis.types.JSONTypeHandler;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tk.mybatis.mapper.annotation.ColumnType;

/**
 * 活动测试 PO。
 */
@Getter
@Setter
@Table(name = "t_activity")
public class Activity {

/** activityId。 */
    @Id
    private String activityId;

/** displaySetting。 */
    @ColumnType(typeHandler = JSONTypeHandler.class)
    private Display displaySetting;

/** bizType。 */
    @ColumnType(typeHandler = JSONTypeHandler.class)
    private List<Integer> bizType;

/** configSetting。 */
    @ColumnType(typeHandler = JSONTypeHandler.class)
    private Configs configSetting;

    @Getter
    @Setter
    public static class DisplaySetting {
/** label。 */
        private String label;

/** displayInList。 */
        private boolean displayInList;

/** displayInAz。 */
        private boolean displayInAz;

/** title。 */
        private String title;

/** link。 */
        private String link;

/** image。 */
        private String image;

/** startTime。 */
        private Date startTime;

/** endTime。 */
        private Date endTime;
    }

    @Setter
    @Getter
    public static class Config {
/** val。 */
        private String val;
/** displaySettings。 */
        private List<DisplaySetting> displaySettings;
    }

    @NoArgsConstructor
    public static class Configs extends ArrayList<Config> {

        public Configs(Collection<? extends Config> c) {
            super(c);
        }
    }

    public static class Display extends HashMap<String, DisplaySetting> {

    }
}
