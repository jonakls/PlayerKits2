package top.jonakls.playerkits.api.storage;

import pk.ajneb97.api.model.Model;

public interface ObjectStorage<T extends Model, E extends Model> {

    void create(T object);

    void saveOne(T object, E kit);

    void saveOneSync(T object, E kit);

    void deleteOne(T object, E kit);

    void deleteOneSync(T object, E kit);

    T findSync(String id);

    T find(String id);

    boolean contains(String id);

    void clear();

    void clearSync();
}
