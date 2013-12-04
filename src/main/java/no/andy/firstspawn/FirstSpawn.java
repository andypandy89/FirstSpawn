package no.andy.firstspawn;

import java.io.File;
import java.util.logging.Level;
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
    private File pluginFolder = new File("plugins/FirstSpawn");
    public static final Logger log = Logger.getLogger("Minecraft");

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
            FirstSpawn.log.log(Level.INFO, "[{0}] First spawn location set to current spawn in default world.", this.getDescription().getName());
        }
    }
    
    @Override
    public void onDisable(){
        getServer().getScheduler().cancelTasks(this);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().hasPlayedBefore() && !getConfig().getString("location.world").equals("NotConfigured")) {
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
        if (cmd.getName().equalsIgnoreCase("firstspawn")) {
            String prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + this.getDescription().getName() + ChatColor.GRAY + "] ";
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!sender.hasPermission("firstspawn.spawn")) {
                        sender.sendMessage(prefix + ChatColor.RED + "You do not have permission for that command.");
                        return true;
                    } else {
                        sender.sendMessage(prefix + ChatColor.WHITE + "Teleporting to first spawn...");
                        player.teleport(new Location(Bukkit.getWorld(getConfig().getString("location.world")), getConfig().getDouble("location.x"), getConfig().getDouble("location.y"), getConfig().getDouble("location.z"), (float) getConfig().getDouble("location.yaw"), (float) getConfig().getDouble("location.pitch")));
                        return true;
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "Console usage: /firstspawn <player>");
                    return true;
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("set")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (sender.hasPermission("firstspawn.set")) {
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
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "You do not have permission for that command.");
                        return true;
                    }
                } else {
                    return false;
                }
            } 
            else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                // TODO: Implement help menu
            }
            else if (args.length == 1) {
                if (sender instanceof Player) {
                    if (!sender.hasPermission("firstspawn.send")) {
                        sender.sendMessage(prefix + ChatColor.RED + "You do not have permission for that command.");
                        return true;
                    }
                }
                Player player = getServer().getPlayerExact(args[0]);
                if (player == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "Player " + args[0] + " not found.");
                    return true;
                } else {
                    player.sendMessage(prefix + ChatColor.WHITE + "Teleporting to first spawn...");
                    player.teleport(new Location(Bukkit.getWorld(getConfig().getString("location.world")), getConfig().getDouble("location.x"), getConfig().getDouble("location.y"), getConfig().getDouble("location.z"), (float) getConfig().getDouble("location.yaw"), (float) getConfig().getDouble("location.pitch")));
                    sender.sendMessage(prefix + ChatColor.WHITE + "Teleported " + args[0] +" to first spawn.");
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
        if (!player.hasPlayedBefore() && !getConfig().getString("location.world").equals("NotConfigured")) {
            // Reason for waiting two ticks before execution is to make sure other plugins don't get in the way.
            getServer().getScheduler().runTaskLater(this, new Runnable() {

                public void run() {
                    player.teleport(new Location(Bukkit.getWorld(getConfig().getString("location.world")), getConfig().getDouble("location.x"), getConfig().getDouble("location.y"), getConfig().getDouble("location.z"), (float) getConfig().getDouble("location.yaw"), (float) getConfig().getDouble("location.pitch")));
                }
            }, 2L);
        }

    }
}
