package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.GamePlugin;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.arenas.scoreboards.ArenaScoreboard;
import me.ampayne2.ultimategames.arenas.spawnpoints.PlayerSpawnPoint;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.utils.UGUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FishSlap extends GamePlugin {
    private UltimateGames ultimateGames;
    private Game game;
    private Map<String, KillStreak> streaks = new HashMap<String, KillStreak>();
    private final static ItemStack FISH;
    private final static Vector HORIZONTAL = new Vector(3, 0, 3);
    private final static Vector VERTICAL = new Vector(0, 2, 0);
    private final static Map<String, String> killers = new HashMap<String, String>();

    @Override
    public boolean loadGame(UltimateGames ultimateGames, Game game) {
        this.ultimateGames = ultimateGames;
        this.game = game;
        return true;
    }

    @Override
    public void unloadGame() {
    }

    @Override
    public boolean reloadGame() {
        return true;
    }

    @Override
    public boolean stopGame() {
        return true;
    }

    @Override
    public boolean loadArena(Arena arena) {
        ultimateGames.addAPIHandler("/" + game.getName() + "/" + arena.getName(), new FishSlapWebHandler(ultimateGames, arena));
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Kills");
        scoreBoard.setVisible(true);
        return true;
    }

    @Override
    public boolean unloadArena(Arena arena) {
        return true;
    }

    @Override
    public boolean isStartPossible(Arena arena) {
        return false;
    }

    @Override
    public boolean startArena(Arena arena) {
        return true;
    }

    @Override
    public boolean beginArena(Arena arena) {
        return true;
    }

    @Override
    public void endArena(Arena arena) {

    }

    @Override
    public boolean resetArena(Arena arena) {
        return true;
    }

    @Override
    public boolean openArena(Arena arena) {
        return true;
    }

    @Override
    public boolean stopArena(Arena arena) {
        return true;
    }

    @Override
    public boolean addPlayer(Player player, Arena arena) {
        String playerName = player.getName();
        PlayerSpawnPoint spawnPoint = ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena);
        spawnPoint.lock(false);
        spawnPoint.teleportPlayer(player);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        resetInventory(player);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(arena);
        if (scoreBoard != null) {
            scoreBoard.addPlayer(player);
            scoreBoard.setScore(playerName, 0);
        }
        streaks.put(playerName, new KillStreak(ultimateGames, game, ultimateGames.getPlayerManager().getArenaPlayer(playerName)));
        return true;
    }

    @Override
    public void removePlayer(Player player, Arena arena) {
        String playerName = player.getName();
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(arena);
        if (scoreBoard != null) {
            scoreBoard.resetScore(playerName);
        }
        streaks.remove(playerName);
        killers.remove(playerName);
        for (String arenaPlayer : new ArrayList<String>(killers.keySet())) {
            if (killers.get(arenaPlayer).equals(playerName)) {
                killers.remove(arenaPlayer);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean addSpectator(Player player, Arena arena) {
        ultimateGames.getSpawnpointManager().getSpectatorSpawnPoint(arena).teleportPlayer(player);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().addItem(UGUtils.createInstructionBook(game));
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        return true;
    }

    @Override
    public void removeSpectator(Player player, Arena arena) {

    }

    @Override
    public void onPlayerDeath(Arena arena, PlayerDeathEvent event) {
        Player player = event.getEntity();
        String playerName = player.getName();
        ultimateGames.getPointManager().addPoint(game, playerName, "death", 1);
        streaks.get(playerName).reset();
        if (killers.containsKey(playerName)) {
            String killerName = killers.get(playerName);
            ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(arena);
            if (scoreBoard != null) {
                scoreBoard.setScore(killerName, scoreBoard.getScore(killerName) + 1);
                ultimateGames.getPointManager().addPoint(game, killerName, "store", 1);
                ultimateGames.getPointManager().addPoint(game, killerName, "kill", 1);
                streaks.get(killerName).increaseCount();
            }
            killers.remove(playerName);
        }
        event.getDrops().clear();
        UGUtils.autoRespawn(player);
    }

    @Override
    public void onPlayerRespawn(Arena arena, PlayerRespawnEvent event) {
        event.setRespawnLocation(ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena).getLocation());
        resetInventory(event.getPlayer());
    }

    @Override
    public void onEntityDamageByEntity(Arena arena, EntityDamageByEntityEvent event) {
        event.setDamage(0.0);
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity instanceof Player && damager instanceof Player) {
            killers.put(((Player) entity).getName(), ((Player) damager).getName());
        }
    }

    @Override
    public void onEntityDamage(Arena arena, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerFoodLevelChange(Arena arena, FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onItemPickup(Arena arena, PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onItemDrop(Arena arena, PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onPlayerMove(Arena arena, PlayerMoveEvent event) {
        if (event.getTo().getBlock().getType() == Material.IRON_PLATE) {
            Player player = event.getPlayer();
            player.setVelocity(player.getEyeLocation().getDirection().multiply(HORIZONTAL).add(VERTICAL));
        }
    }

    @SuppressWarnings("deprecation")
    private void resetInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().addItem(FISH, UGUtils.createInstructionBook(game));
        player.updateInventory();
    }

    static {
        FISH = new ItemStack(Material.RAW_FISH);
        FISH.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
    }
}
