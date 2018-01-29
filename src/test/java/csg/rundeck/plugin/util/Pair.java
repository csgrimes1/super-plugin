package csg.rundeck.plugin.util;

public final class Pair<TKey, TValue> {
    public final TKey Key;
    public final TValue Value;

    Pair(TKey key, TValue value) {
        this.Key = key;
        this.Value = value;
    }

    public static <TKey, TValue> Pair<TKey, TValue> of(TKey key, TValue value) {
        return new Pair(key, value);
    }
}
