package top.jonakls.playerkits.cache;

import org.jetbrains.annotations.NotNull;
import pk.ajneb97.api.model.player.PlayerModel;
import top.jonakls.playerkits.api.cache.ObjectCache;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache implements ObjectCache<PlayerModel> {

    private final Map<String, PlayerModel> cache = new ConcurrentHashMap<>();

    @Override
    public void add(PlayerModel object) {
        this.cache.put(object.id(), object);
    }

    @Override
    public void remove(PlayerModel object) {
        this.cache.remove(object.id());
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public PlayerModel get(String id) {
        return this.cache.get(id);
    }

    @Override
    public boolean contains(String id) {
        return this.cache.containsKey(id);
    }

    @Override
    public int size() {
        return this.cache.size();
    }

    @Override
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    @Override
    public void update(PlayerModel object) {
        this.cache.put(object.id(), object);
    }

    @NotNull
    @Override
    public Iterator<PlayerModel> iterator() {
        return this.cache.values().iterator();
    }
}
