package top.jonakls.playerkits.api.manager;

public interface Manager<T> {

    default void manage() {

    }

    default T get() {
        return null;
    }
}
