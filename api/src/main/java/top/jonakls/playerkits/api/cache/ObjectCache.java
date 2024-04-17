package top.jonakls.playerkits.api.cache;

import pk.ajneb97.api.model.Model;

public interface ObjectCache<T extends Model> extends Iterable<T> {

    void add(T object);

    void remove(T object);

    void clear();

    T get(String id);

    boolean contains(String id);

    int size();

    boolean isEmpty();

    void update(T object);
}
