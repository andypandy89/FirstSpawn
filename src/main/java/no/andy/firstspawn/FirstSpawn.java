package no.andy.firstspawn;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FirstSpawn extends JavaPlugin implements Listener {
    public String prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + "FirstSpawn" + ChatColor.GRAY + "] ";
    private File pluginFolder = new File("plugins/FirstSpawn");
    public final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable(){
        createPluginFolder();
        getConfig().options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if ((getConfig().getString("location.world")).equals("NotConfigured")) {
            World defWorld = getServer().getWorlds().get(0);
            Location loc = defWorld.getSpawnLocation();
            getConfig().set("location.world", loc.getWorld().getName());
            getConfig().set("location.x", loc.getX());
            getConfig().set("location.y", loc.getY());
            getConfig().set("location.z", loc.getZ());
            getConfig().set("location.yaw", loc.getYaw());
            getConfig().set("location.pitch", loc.getPitch());
            saveConfig();
            this.log.info("[FirstSpawn] First spawn location set to current spawn in default world.");
        }
    }
    
    @Override
    public void onDisable(){
        getServer().getScheduler().cancelTasks(this);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().hasPlayedBefore() && getConfig().getDouble("location.x") != 0) {
            String command = event.getMessage().toLowerCase();
            if (command.startsWith("/spawn")) {
                event.setMessage("/firstspawn");
                event.setCancelled(true);
                event.getPlayer().teleport(new Location(Bukkit.getWorld(getConfig().getString("location.world")), getConfig().getDouble("location.x"), getConfig().getDouble("location.y"), getConfig().getDouble("location.z"), (float) getConfig().getDouble("location.yaw"), (float) getConfig().getDouble("location.pitch")));
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (cmd.getName().equalsIgnoreCase("firstspawn")) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    if (!sender.hasPermission("firstspawn.spawn")) {
                        player.sendMessage(prefix + ChatColor.RED + "You do not have permission for that command.");
                        return true;
                    }
                    else {
                        sender.sendMessage(ChatColor.WHITE + "Teleporting to first spawn...");
                        player.teleport(new Location(Bukkit.getWorld(getConfig().getString("location.world")), getConfig().getDouble("location.x"), getConfig().getDouble("location.y"), getConfig().getDouble("location.z"), (float) getConfig().getDouble("location.yaw"), (float) getConfig().getDouble("location.pitch")));
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("set") && sender.hasPermission("firstspawn.set")) {
                    Location loc = player.getLocation();
                    getConfig().set("location.world", loc.getWorld().getName());
                    getConfig().set("location.x", loc.getX());
                    getConfig().set("location.y", loc.getY());
                    getConfig().set("location.z", loc.getZ());
                    getConfig().set("location.yaw", loc.getYaw());
                    getConfig().set("location.pitch", loc.getPitch());
                    saveConfig();
                    sender.sendMessage(prefix + ChatColor.WHITE + "Set first spawn point.");
                    return true;
                }
            }
        }
        return false;
    }
    
    private void createPluginFolder() {
        if (!this.pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerLogin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPlayedBefore() && getConfig().getDouble("location.x") != 0) {
            getServer().getScheduler().runTaskLater(this, new Runnable() {

                public void run() {
                    player.teleport(new Location(Bukkit.getWorld(getConfig().getString("location.world")), getConfig().getDouble("location.x"), getConfig().getDouble("location.y"), getConfig().getDouble("location.z"), (float) getConfig().getDouble("location.yaw"), (float) getConfig().getDouble("location.pitch")));
                }
            }, 2L);
        }

    }
}
