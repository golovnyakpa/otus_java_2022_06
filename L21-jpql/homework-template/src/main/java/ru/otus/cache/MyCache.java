package ru.otus.cache;


import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class MyCache<K, V> implements HwCache<K, V> {

    private final WeakHashMap<K, V> cache = new WeakHashMap<>();
    private final List<HwListener<K, V>> listeners = new ArrayList<>();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        listeners.forEach(l -> l.notify(key, value, "put"));
    }

    @Override
    public void remove(K key) {
        var value = cache.remove(key);
        listeners.forEach(l -> l.notify(key, value, "remove"));
    }

    @Override
    public V get(K key) {
        var value = cache.get(key);
        listeners.forEach(l -> l.notify(key, value, "get"));
        return value;
    }

    @Override
    public void addListener(HwListener<K, V> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(HwListener<K, V> listener) {
        var isRemoved = listeners.remove(listener);
        if (isRemoved) System.out.println("Removed");
        else System.out.println("Removing element not found");
    }
}
