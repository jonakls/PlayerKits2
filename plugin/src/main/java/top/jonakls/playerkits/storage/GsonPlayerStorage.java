package top.jonakls.playerkits.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import pk.ajneb97.api.model.player.PlayerDataKit;
import pk.ajneb97.api.model.player.PlayerModel;
import top.jonakls.playerkits.api.storage.ObjectStorage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GsonPlayerStorage implements ObjectStorage<PlayerModel, PlayerDataKit> {

    private final File folder;
    private final ComponentLogger logger;
    private final static String EXTENSION = ".json";
    private final static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public GsonPlayerStorage(final @NotNull String dataFolder, final ComponentLogger logger) {
        this.folder = new File(dataFolder + "/players");
        this.logger = logger;

        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Cannot create folder " + dataFolder + "/players");
        }
    }


    @Override
    public void create(PlayerModel object) {
        try {
            File file = new File(folder, object.id() + EXTENSION);
            if (!file.exists() && !file.createNewFile()) {
                throw new RuntimeException("Cannot create file " + file.getName());
            }

            GSON.toJson(object, PlayerModel.class);
        } catch (IOException e) {
            logger.error("An error occurred while creating player data", e);
        }
    }

    @Override
    public void saveOne(PlayerModel object, PlayerDataKit __) {
        CompletableFuture.runAsync(() -> this.saveOneSync(object, __));
    }

    @Override
    public void saveOneSync(PlayerModel object, PlayerDataKit __) {
        this.create(object);
    }

    @Override
    public void deleteOne(PlayerModel object, PlayerDataKit __) {
        CompletableFuture.runAsync(() -> this.deleteOneSync(object, __));
    }

    @Override
    public void deleteOneSync(PlayerModel object, PlayerDataKit __) {
        File file = new File(folder, object.id() + EXTENSION);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Cannot delete file " + file.getName());
        }
    }

    @Override
    public PlayerModel findSync(String id) {
        File file = new File(folder, id + EXTENSION);
        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, PlayerModel.class);
        } catch (IOException e) {
            logger.error("An error occurred while finding player data", e);
        }
        return null;
    }

    @Override
    public PlayerModel find(String id) {
        return CompletableFuture.supplyAsync(() -> this.findSync(id)).join();
    }

    @Override
    public boolean contains(String id) {
        File file = new File(folder, id + EXTENSION);
        return file.exists();
    }

    @Override
    public void clear() {
    }

    @Override
    public void clearSync() {
    }
}
