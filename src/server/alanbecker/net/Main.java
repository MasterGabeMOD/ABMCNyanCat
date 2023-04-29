package server.alanbecker.net;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {

    HashMap<Player, Integer> playerNyanType = new HashMap<>();
    Material[] woolColors = {Material.RED_WOOL, Material.ORANGE_WOOL, Material.YELLOW_WOOL, Material.GREEN_WOOL, Material.BLUE_WOOL, Material.PURPLE_WOOL};

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("nyan") && player.hasPermission("nyan.normal")) {
                if (playerNyanType.containsKey(player)) {
                    playerNyanType.remove(player);
                    player.sendMessage(ChatColor.GOLD + "Nyan disabled...");
                } else {
                    playerNyanType.put(player, 0);
                    player.sendMessage(ChatColor.GOLD + "Nyan [Rainbow] enabled...");
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (playerNyanType.containsKey(player)) {
            Location loc = player.getLocation();
            Block behind = loc.getBlock().getRelative(player.getFacing().getOppositeFace());

            for (int i = 0; i < woolColors.length; i++) { // Draw a rainbow
                Location blockLocation = behind.getLocation().add(0, i, 0);
                Material material = woolColors[(i + playerNyanType.get(player)) % woolColors.length];
                blockLocation.getBlock().setType(material);

                new BukkitRunnable() {
                    public void run() {
                        blockLocation.getBlock().setType(Material.AIR);
                    }
                }.runTaskLater(this, 40L); // Decay after 40 ticks (2 seconds)
            }

            // Update the player's current color index
            playerNyanType.put(player, (playerNyanType.get(player) + 1) % woolColors.length);
        }
    }

    private class PluginListener implements Listener {
        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            // Remove all NyanCat blocks on plugin disable
            for (Player player : playerNyanType.keySet()) {
                Location loc = player.getLocation();
                Block behind = loc.getBlock().getRelative(player.getFacing().getOppositeFace());

                for (int i = 0; i < woolColors.length; i++) {
                    Location blockLocation = behind.getLocation().add(0, i, 0);
                    blockLocation.getBlock().setType(Material.AIR);
                }
            }
        }
    }
}
