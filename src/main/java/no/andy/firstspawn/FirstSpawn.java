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
                tpToFirstSpawn(event.getPlayer());
                return;
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("firstspawn")) {
            String spawnPerm = "firstspawn.spawn";
            String sendPerm = "firstspawn.send";
            String setPerm = "firstspawn.set";
            String reloadPerm = "firstspawn.reload";
            String prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + this.getDescription().getName() + ChatColor.GRAY + "] ";
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!sender.hasPermission(spawnPerm)) {
                        sender.sendMessage(prefix + ChatColor.RED + "You do not have permission for that command.");
                        return true;
                    } else {
                        tpToFirstSpawn(player);
                        return true;
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "Console usage: /firstspawn <player>");
                    return true;
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("set")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (sender.hasPermission(setPerm)) {
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
                sender.sendMessage(ChatColor.WHITE + "----------------" + ChatColor.GRAY + "[" + ChatColor.GREEN + this.getDescription().getName() + " Help Menu " + ChatColor.GRAY + "]" + ChatColor.WHITE + "----------------");
                if (sender instanceof Player) {
                    if (sender.hasPermission(spawnPerm)) {
                        sender.sendMessage(ChatColor.GREEN + "/firstspawn" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Teleports you to first spawn.");
                    }
                }
                if(sender.hasPermission(sendPerm)) {
                    sender.sendMessage(ChatColor.GREEN + "/firstspawn <player>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Teleports specified player to first spawn.");
                }
                if(sender instanceof Player && sender.hasPermission(setPerm)) {
                    sender.sendMessage(ChatColor.GREEN + "/firstspawn set" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the first spawn at current location.");
                }
                if(sender.hasPermission(reloadPerm)) {
                    sender.sendMessage(ChatColor.GREEN + "/firstspawn reload" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Reloads the configuration file.");
                }
                return true;
            }
            else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission(reloadPerm)) {
                    sender.sendMessage(prefix + ChatColor.RED + "You do not have permission for that command.");
                    return true;
                }
                else {
                    this.reloadConfig();
                    sender.sendMessage(prefix + ChatColor.WHITE + "Configuration file reloaded.");
                    return true;
                }
            }
            else if (args.length == 1) {
                if (sender instanceof Player) {
                    if (!sender.hasPermission(sendPerm)) {
                        sender.sendMessage(prefix + ChatColor.RED + "You do not have permission for that command.");
                        return true;
                    }
                }
                Player player = getServer().getPlayerExact(args[0]);
                if (player == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "Player " + args[0] + " not found.");
                    return true;
                } else {
                    tpToFirstSpawn(player);
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
                    tpToFirstSpawn(player);
                }
            }, 2L);
        }
    }
    
    public void tpToFirstSpawn(Player p) {
        String prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + this.getDescription().getName() + ChatColor.GRAY + "] ";
        World w = Bukkit.getWorld(getConfig().getString("location.world"));
        double x = getConfig().getDouble("location.x");
        double y = getConfig().getDouble("location.y");
        double z = getConfig().getDouble("location.z");
        float yaw = (float) getConfig().getDouble("location.yaw");
        float pitch = (float) getConfig().getDouble("location.pitch");
        Location dest = new Location(w, x, y, z, yaw, pitch);
        p.sendMessage(prefix + ChatColor.WHITE + "Teleporting to first spawn...");
        p.teleport(dest);
    }
}
