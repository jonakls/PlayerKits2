package top.jonakls.playerkits.api;

public interface Service {

    /**
     * Called when the service is initialized.
     */
    default void init() {
    }

    /**
     * Called when the service is closed.
     */
    default void close() {
    }
}
