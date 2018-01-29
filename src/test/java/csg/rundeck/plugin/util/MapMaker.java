package csg.rundeck.plugin.util;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

public final class MapMaker {
    public static <TKey, TValue> Map<TKey, TValue> create(Pair<TKey, TValue> ... pairs) {
        val map = new HashMap<TKey, TValue>();
        for(val p : pairs) {
            map.put(p.Key, p.Value);
        }
        return map;
    }
}
