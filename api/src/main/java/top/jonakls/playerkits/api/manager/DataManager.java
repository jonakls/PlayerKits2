package top.jonakls.playerkits.api.manager;

import pk.ajneb97.api.model.Model;

public interface DataManager<T extends Model> extends Manager<T> {

    T load(String id);

    void save(T object);

    void delete(T object);

    boolean contains(String id);

    void clearCache();

    void saveAll();

    T createNew(String id);

    T get(String id);
}
