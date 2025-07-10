package io.github.opensabe.common.mybatis.test.po;

import io.github.opensabe.common.mybatis.types.JSONTypeHandler;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tk.mybatis.mapper.annotation.ColumnType;

import java.util.*;

@Getter
@Setter
@Table(name = "t_activity")
public class Activity {

    @Id
    private String activityId;

    @ColumnType(typeHandler = JSONTypeHandler.class)
    private Display displaySetting;

    @ColumnType(typeHandler = JSONTypeHandler.class)
    private List<Integer> bizType;

    @ColumnType(typeHandler = JSONTypeHandler.class)
    private Configs configSetting;

    @Getter
    @Setter
    public static class DisplaySetting {
        private String label;

        private boolean displayInList;

        private boolean displayInAz;

        private String title;

        private String link;

        private String image;

        private Date startTime;

        private Date endTime;
    }

    @Setter
    @Getter
    public static class Config {
        private String val;
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
