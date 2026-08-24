package io.github.opensabe.common.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlarmUtilTest {

    @Test
    public void extractGroup_presetGroups() {
        Set<String> groups = AlarmUtil.extractGroup("[rd,op] something went wrong");
        assertEquals(Set.of("rd", "op"), groups);
    }

    @Test
    public void registerGroup_thenExtractable() {
        AlarmUtil.registerGroup("risk");
        Set<String> groups = AlarmUtil.extractGroup("[risk] custom alarm");
        assertEquals(Set.of("risk"), groups);
        assertTrue(AlarmUtil.getAllGroups().contains("risk"));
    }

    @Test
    public void registerGroups_batchAndNormalize() {
        AlarmUtil.registerGroups("  Biz ", "SEC");
        assertTrue(AlarmUtil.getAllGroups().contains("biz"));
        assertTrue(AlarmUtil.getAllGroups().contains("sec"));
        assertEquals(Set.of("biz", "sec"), AlarmUtil.extractGroup("[Biz,SEC] batch"));
    }

    @Test
    public void getAllGroups_unmodifiable() {
        Set<String> view = AlarmUtil.getAllGroups();
        assertTrue(view.contains("rd"));
        assertThrows(UnsupportedOperationException.class, () -> view.add("hack"));
        assertThrows(UnsupportedOperationException.class, () -> view.remove("rd"));
    }

    @Test
    public void registerGroup_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> AlarmUtil.registerGroup(null));
        assertThrows(IllegalArgumentException.class, () -> AlarmUtil.registerGroup(""));
        assertThrows(IllegalArgumentException.class, () -> AlarmUtil.registerGroup("   "));
    }

    @Test
    public void registerGroup_rejectsDelimiterChars() {
        assertThrows(IllegalArgumentException.class, () -> AlarmUtil.registerGroup("a,b"));
        assertThrows(IllegalArgumentException.class, () -> AlarmUtil.registerGroup("a[b"));
        assertThrows(IllegalArgumentException.class, () -> AlarmUtil.registerGroup("a]b"));
    }

    @Test
    public void extractGroup_ignoresUnregistered() {
        Set<String> groups = AlarmUtil.extractGroup("[unknown_group_xyz] not registered");
        assertTrue(groups.isEmpty());
        assertFalse(AlarmUtil.getAllGroups().contains("unknown_group_xyz"));
    }

    @Test
    public void registerGroup_idempotent() {
        AlarmUtil.registerGroup("dup");
        AlarmUtil.registerGroup("DUP");
        assertEquals(1, AlarmUtil.getAllGroups().stream().filter("dup"::equals).count());
    }
}
