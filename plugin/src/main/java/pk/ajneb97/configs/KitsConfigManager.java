package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.model.kit.KitModel;
import pk.ajneb97.configuration.CustomConfiguration;
import pk.ajneb97.managers.KitItemManager;
import pk.ajneb97.api.model.kit.KitAction;
import pk.ajneb97.api.model.kit.KitRequirements;
import pk.ajneb97.api.model.kit.item.KitItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KitsConfigManager {
    private ArrayList<CustomConfiguration> configFiles;
    private String folderName;
    private PlayerKits2 plugin;

    public KitsConfigManager(PlayerKits2 plugin, String folderName){
        this.plugin = plugin;
        this.folderName = folderName;
        this.configFiles = new ArrayList<>();
    }

    public void configure() {
        createFolder();
        reloadConfigs();
    }

    public void reloadConfigs(){
        this.configFiles = new ArrayList<>();
        registerConfigFiles();
        loadConfigs();
    }

    public void createFolder(){
        File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + folderName);
            if(!folder.exists()){
                folder.mkdirs();
                createDefaultConfigs();
            }
        } catch(SecurityException e) {
            folder = null;
        }
    }

    public void createDefaultConfigs(){
        new CustomConfiguration("diamond.yml",plugin,folderName,false).registerConfig();
        new CustomConfiguration("iron.yml",plugin,folderName,false).registerConfig();
        new CustomConfiguration("food.yml",plugin,folderName,false).registerConfig();
    }

    public void saveConfigFiles() {
        for(int i=0;i<configFiles.size();i++) {
            configFiles.get(i).saveConfig();
        }
    }

    public void registerConfigFiles(){
        String path = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (int i=0;i<listOfFiles.length;i++) {
            if(listOfFiles[i].isFile()) {
                String pathName = listOfFiles[i].getName();
                CustomConfiguration config = new CustomConfiguration(pathName, plugin, folderName, true);
                config.registerConfig();
                configFiles.add(config);
            }
        }
    }

    public ArrayList<CustomConfiguration> getConfigs(){
        return this.configFiles;
    }

    public boolean fileAlreadyRegistered(String pathName) {
        for(int i=0;i<configFiles.size();i++) {
            if(configFiles.get(i).getPath().equals(pathName)) {
                return true;
            }
        }
        return false;
    }

    public CustomConfiguration getConfigFile(String pathName) {
        for(int i=0;i<configFiles.size();i++) {
            if(configFiles.get(i).getPath().equals(pathName)) {
                return configFiles.get(i);
            }
        }
        return null;
    }

    public boolean registerConfigFile(String pathName) {
        if(!fileAlreadyRegistered(pathName)) {
            CustomConfiguration config = new CustomConfiguration(pathName, plugin, folderName, true);
            config.registerConfig();
            configFiles.add(config);
            return true;
        }else {
            return false;
        }
    }

    public void removeConfigKit(String path) {
        for(int i=0;i<configFiles.size();i++) {
            if(configFiles.get(i).getPath().equals(path)) {
                configFiles.remove(i);
            }
        }
    }

    public void loadConfigs(){
        ArrayList<KitModel> kitModels = new ArrayList<KitModel>();
        for(CustomConfiguration configFile : configFiles){
            FileConfiguration config = configFile.get();

            String name = configFile.getPath().replace(".yml","");
            KitModel kitModel = getKitFromConfig(config,plugin,name,"");
            kitModels.add(kitModel);
        }

        plugin.getKitsManager().setKits(kitModels);
    }

    public void saveConfigs(){

    }

    public void saveConfig(KitModel kitModel){
        String kitName = kitModel.getName();
        CustomConfiguration kitConfig = getConfigFile(kitName+".yml");
        if(kitConfig == null) {
            registerConfigFile(kitName+".yml");
            kitConfig = getConfigFile(kitName+".yml");
        }

        FileConfiguration config = kitConfig.get();

        config.set("cooldown", kitModel.getCooldown());
        config.set("one_time", kitModel.isOneTime());
        config.set("auto_armor", kitModel.isAutoArmor());
        config.set("permission_required", kitModel.isPermissionRequired());

        KitItemManager kitItemManager = plugin.getKitItemManager();
        int currentPos = 1;
        config.set("items",null);
        for(KitItem kitItem : kitModel.getItems()){
            kitItemManager.saveKitItemOnConfig(kitItem,config,"items."+currentPos);
            currentPos++;
        }


        config.set("actions",null);
        saveActions(kitModel.getClaimActions(),"claim",config,kitItemManager);
        saveActions(kitModel.getErrorActions(),"error",config,kitItemManager);

        config.set("display.default",null);
        if(kitModel.getDisplayItemDefault() != null){
            kitItemManager.saveKitItemOnConfig(kitModel.getDisplayItemDefault(),config,"display.default");
        }
        config.set("display.no_permission",null);
        if(kitModel.getDisplayItemNoPermission() != null){
            kitItemManager.saveKitItemOnConfig(kitModel.getDisplayItemNoPermission(),config,"display.no_permission");
        }
        config.set("display.cooldown",null);
        if(kitModel.getDisplayItemCooldown() != null){
            kitItemManager.saveKitItemOnConfig(kitModel.getDisplayItemCooldown(),config,"display.cooldown");
        }
        config.set("display.one_time",null);
        if(kitModel.getDisplayItemOneTime() != null){
            kitItemManager.saveKitItemOnConfig(kitModel.getDisplayItemOneTime(),config,"display.one_time");
        }
        config.set("display.one_time_requirements",null);
        if(kitModel.getDisplayItemOneTimeRequirements() != null){
            kitItemManager.saveKitItemOnConfig(kitModel.getDisplayItemOneTimeRequirements(),config,"display.one_time_requirements");
        }

        KitRequirements requirements = kitModel.getRequirements();
        if(requirements != null){
            config.set("requirements.one_time_requirements",requirements.isOneTimeRequirements());
            if(requirements.getExtraRequirements() != null){
                config.set("requirements.extra_requirements",requirements.getExtraRequirements());
            }
            config.set("requirements.message",requirements.getMessage());
            if(requirements.getActionsOnBuy() != null){
                config.set("requirements.actions_on_buy",requirements.getActionsOnBuy());
            }
            if(requirements.getPrice() != 0){
                config.set("requirements.price",requirements.getPrice());
            }
        }

        kitConfig.saveConfig();
    }

    public void saveActions(List<KitAction> kitActions,String actionType,FileConfiguration config,KitItemManager kitItemManager){
        int currentPos = 1;
        for(KitAction kitAction : kitActions){
            String path = "actions."+actionType+"."+currentPos;
            config.set(path+".action",kitAction.getAction());
            config.set(path+".execute_before_items",kitAction.isExecuteBeforeItems());
            config.set(path+".count_as_item",kitAction.isCountAsItem());
            if(kitAction.getDisplayItem() != null){
                kitItemManager.saveKitItemOnConfig(kitAction.getDisplayItem(),config,path+".display_item");
            }

            currentPos++;
        }
    }

    public void removeKitFile(String kitName){
        CustomConfiguration kitConfig = getConfigFile(kitName+".yml");
        if(kitConfig == null){
            return;
        }

        File file = new File(plugin.getDataFolder()+File.separator+folderName,kitConfig.getPath());
        file.delete();

        removeConfigKit(kitConfig.getPath());
    }

    // mainPath must include a "." at the end
    public static KitModel getKitFromConfig(FileConfiguration config, PlayerKits2 plugin, String name, String mainPath){
        KitItemManager kitItemManager = plugin.getKitItemManager();
        int cooldown = config.contains(mainPath+"cooldown") ? config.getInt(mainPath+"cooldown") : 0;
        boolean permissionRequired = config.contains(mainPath+"permission_required") ? config.getBoolean(mainPath+"permission_required") : false;
        boolean autoArmor = config.contains(mainPath+"auto_armor") ? config.getBoolean(mainPath+"auto_armor") : false;
        boolean oneTime = config.contains(mainPath+"one_time") ? config.getBoolean(mainPath+"one_time") : false;

        ArrayList<KitItem> items = new ArrayList<KitItem>();
        if(config.contains(mainPath+"items")){
            for(String key : config.getConfigurationSection(mainPath+"items").getKeys(false)){
                KitItem item = kitItemManager.getKitItemFromConfig(config,mainPath+"items."+key);
                items.add(item);
            }
        }
        ArrayList<KitAction> claimActions = getActions(config,"claim",mainPath,kitItemManager);
        ArrayList<KitAction> errorActions = getActions(config,"error",mainPath,kitItemManager);

        KitItem displayItemDefault = kitItemManager.getKitItemFromConfig(config,mainPath+"display.default");
        KitItem displayItemNoPermission = config.contains(mainPath+"display.no_permission") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.no_permission") : null;
        KitItem displayItemCooldown = config.contains(mainPath+"display.cooldown") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.cooldown") : null;
        KitItem displayItemOneTime = config.contains(mainPath+"display.one_time") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.one_time") : null;
        KitItem displayItemOneTimeRequirements = config.contains(mainPath+"display.one_time_requirements") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.one_time_requirements") : null;

        KitRequirements kitRequirements = null;
        if(config.contains("requirements")){
            boolean oneTimeRequirements = config.getBoolean("requirements.one_time_requirements");
            List<String> extraRequirements = config.getStringList("requirements.extra_requirements");
            List<String> message = config.getStringList("requirements.message");
            List<String> actionsOnBuy = config.getStringList("requirements.actions_on_buy");
            double price = config.contains("requirements.price") ? config.getDouble("requirements.price") : 0;
            kitRequirements = new KitRequirements(oneTimeRequirements,extraRequirements,message,actionsOnBuy,price);
        }


        KitModel kitModel = new KitModel(name);
        kitModel.setCooldown(cooldown);

        kitModel.setAutoArmor(autoArmor);
        kitModel.setOneTime(oneTime);
        kitModel.setPermissionRequired(permissionRequired);
        kitModel.setItems(items);
        kitModel.setClaimActions(claimActions);
        kitModel.setErrorActions(errorActions);
        kitModel.setDisplayItemDefault(displayItemDefault);
        kitModel.setDisplayItemNoPermission(displayItemNoPermission);
        kitModel.setDisplayItemCooldown(displayItemCooldown);
        kitModel.setDisplayItemOneTime(displayItemOneTime);
        kitModel.setDisplayItemOneTimeRequirements(displayItemOneTimeRequirements);
        kitModel.setRequirements(kitRequirements);

        return kitModel;
    }

    public static ArrayList<KitAction> getActions(FileConfiguration config,String actionType,String mainPath,KitItemManager kitItemManager){
        ArrayList<KitAction> actions = new ArrayList<>();
        if(config.contains(mainPath+"actions."+actionType)){
            for(String key : config.getConfigurationSection(mainPath+"actions."+actionType).getKeys(false)){
                String path = mainPath+"actions."+actionType+"."+key;
                String action = config.getString(path+".action");
                boolean executeBeforeItem = config.contains(path+".execute_before_items") ? config.getBoolean(path+".execute_before_items") : false;
                boolean countAsItem = config.contains(path+".count_as_item") ? config.getBoolean(path+".count_as_item") : false;
                KitItem item = config.contains(path+".display_item") ? kitItemManager.getKitItemFromConfig(config,path+".display_item") : null;

                actions.add(new KitAction(action,item,executeBeforeItem,countAsItem));
            }
        }
        return actions;
    }
}
