package cm.ptks.craftflowers;

import cm.ptks.craftflowers.commands.CraftFlowersCommand;
import cm.ptks.craftflowers.languages.LanguageManager;
import cm.ptks.craftflowers.languages.Messages;
import cm.ptks.craftflowers.listeners.BlockPlaceListener;
import cm.ptks.craftflowers.listeners.LeftClickListener;
import cm.ptks.craftflowers.storage.FlowerStorage;
import cm.ptks.craftflowers.storage.SqLiteStorage;
import cm.ptks.craftflowers.util.Text;
import fr.minuskube.inv.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CraftFlowers extends JavaPlugin {

    private static InventoryManager inventoryManager;

    private static CraftFlowers instance;

    public static String prefix;
    public static String arrow;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private FlowerStorage flowerStorage;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;

        CraftFlowers.inventoryManager = new InventoryManager(this);
        inventoryManager.init();
        
        getConfig().setDefaults(YamlConfiguration
                .loadConfiguration(new InputStreamReader(Objects.requireNonNull(getResource("config.yml")), StandardCharsets.UTF_8)));
        getConfig().options().copyDefaults(true);

        saveConfig();
        saveDefaultConfig();

        CraftFlowers.prefix = Text.translateAmpersand(Objects.requireNonNull(getConfig().getString("prefix")));
        CraftFlowers.arrow = Text.translateAmpersand(Objects.requireNonNull(getConfig().getString("arrow")));

        if (Objects.equals(getConfig().getString("storage.type"), "sqlite")) {
            this.flowerStorage = new SqLiteStorage(new File(getDataFolder(), "database.db"));
        }

        this.languageManager = new LanguageManager(this);


        new Metrics(this, 2877);
        this.registerListener();
        this.registerCommands();
    }

    public static CraftFlowers getInstance() {
        return instance;
    }

    public FlowerStorage getFlowerStorage() {
        return flowerStorage;
    }

    @Override
    public void onDisable() {
        this.executorService.shutdown();
        this.flowerStorage.close();
    }

    

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    private void registerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new LeftClickListener(), this);
    }

    private void registerCommands() {
        this.getCommand("craftflowers").setExecutor(new CraftFlowersCommand(this));
    }

    public boolean isSurvivalMode() {
        return getConfig().getBoolean("survivalMode");
    }

    public boolean isFAWE() {
        return Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }
}
