package fr.phoenix.aibuilds;

import fr.phoenix.aibuilds.command.AIBuildsTreeRoot;
import fr.phoenix.aibuilds.compat.Metrics;
import fr.phoenix.aibuilds.compat.placeholder.DefaultPlaceholderParser;
import fr.phoenix.aibuilds.compat.placeholder.PlaceholderAPIParser;
import fr.phoenix.aibuilds.compat.placeholder.PlaceholderParser;
import fr.phoenix.aibuilds.listener.PlayerListener;
import fr.phoenix.aibuilds.manager.ConfigManager;
import fr.phoenix.aibuilds.manager.PaletteManager;
import fr.phoenix.aibuilds.manager.TokenManager;
import fr.phoenix.aibuilds.version.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class AIBuilds extends JavaPlugin {
    public static AIBuilds plugin;

    public PaletteManager paletteManager = new PaletteManager();
    public ConfigManager configManager = new ConfigManager();
    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    public TokenManager tokenManager = new TokenManager();

    @Override
    public void onEnable() {


        // Register the root command
        getCommand("aibuilds").setExecutor(new AIBuildsTreeRoot("aibuilds", "aibuilds.aibuilds"));


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderParser = new PlaceholderAPIParser();
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        }
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);


        // Metrics data
        new Metrics(this, 17747);


        // Update checker
        new SpigotPlugin(107911, this).checkForUpdate();

        // Save default config if it doesn't exist
        saveDefaultConfig();
        initializePlugin(false);

    }

    @Override
    public void onDisable() {
        //Executes all the pending asynchronous task (like saving the playerData)
        Bukkit.getScheduler().getPendingTasks().forEach(worker -> {
            if (worker.getOwner().equals(this)) {
                ((Runnable) worker).run();
            }
        });
        Bukkit.getOnlinePlayers().forEach(player -> tokenManager.save(player));

    }


    @Override
    public void onLoad() {
        plugin = this;
    }

    public void initializePlugin(boolean clearBefore) {
        if (clearBefore)
            reloadConfig();
        //Load manager
        configManager.load(clearBefore);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            paletteManager.load(clearBefore);
        });
        Bukkit.getOnlinePlayers().forEach(player -> tokenManager.load(player));
    }


    public static void log(String message) {
        plugin.getLogger().log(Level.WARNING, message);
    }
}
