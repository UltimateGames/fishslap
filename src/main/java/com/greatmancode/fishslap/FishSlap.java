package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.GamePlugin;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.arenas.PlayerSpawnPoint;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.scoreboards.ArenaScoreboard;

import me.ampayne2.ultimategames.utils.UGUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class FishSlap extends GamePlugin {

    private UltimateGames ultimateGames;
    private Game game;

    @Override
    public Boolean loadGame(UltimateGames ultimateGames, Game game) {
        this.ultimateGames = ultimateGames;
        this.game = game;
        return true;
    }

    @Override
    public void unloadGame() {

    }

    @Override
    public Boolean reloadGame() {
        return true;
    }

    @Override
    public Boolean stopGame() {
        return true;
    }

    @Override
    public Boolean loadArena(Arena arena) {
        ultimateGames.addAPIHandler("/" + game.getName() + "/" + arena.getName(), new FishSlapWebHandler(ultimateGames, arena));
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Kills");
        scoreBoard.setVisible(true);
        return true;
    }

    @Override
    public Boolean unloadArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean isStartPossible(Arena arena) {
        return false;
    }

    @Override
    public Boolean startArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean beginArena(Arena arena) {
        return true;
    }

    @Override
    public void endArena(Arena arena) {

    }

    @Override
    public Boolean resetArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean openArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean stopArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean addPlayer(Player player, Arena arena) {
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
            scoreBoard.setScore(player.getName(), 0);
        }
        return true;
    }

    @Override
    public void removePlayer(Player player, Arena arena) {
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(arena);
        if (scoreBoard != null) {
            scoreBoard.resetScore(player.getName());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Boolean addSpectator(Player player, Arena arena) {
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
        String killerName = null;
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            killerName = killer.getName();
        }
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(arena);
        if (scoreBoard != null && killerName != null) {
            scoreBoard.setScore(killerName, scoreBoard.getScore(killerName) + 1);
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

    @SuppressWarnings("deprecation")
    private void resetInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        ItemStack fish = new ItemStack(Material.RAW_FISH);
        fish.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        player.getInventory().addItem(fish, UGUtils.createInstructionBook(game));
        player.updateInventory();
    }
}
