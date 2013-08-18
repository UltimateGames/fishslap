package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.GamePlugin;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.arenas.SpawnPoint;
import me.ampayne2.ultimategames.enums.ArenaStatus;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.scoreboards.ArenaScoreboard;

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
    public Boolean unloadGame() {
        return true;
    }

    @Override
    public Boolean stopGame() {
        return true;
    }

    @Override
    public Boolean loadArena(Arena arena) {
        ultimateGames.addAPIHandler("/" + game.getGameDescription().getName() + "/" +arena.getName(), new FishSlapWebHandler(ultimateGames, arena));
        return true;
    }

    @Override
    public Boolean unloadArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean isStartPossible(Arena arena) {
        if (arena.getStatus() == ArenaStatus.OPEN) {
            return true;
        }
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
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Kills");
        scoreBoard.setVisible(true);
        return true;
    }

    @Override
    public Boolean stopArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean addPlayer(Player player, Arena arena) {
        SpawnPoint spawnPoint = ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena);
        spawnPoint.lock(false);
        spawnPoint.teleportPlayer(player);
        resetInventory(player);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(arena)) {
            if (scoreBoard.getName().equals("Kills")) {
                scoreBoard.addPlayer(player);
                scoreBoard.setScore(player.getName(), 0);
            }
        }
        return true;
    }

    @Override
    public void removePlayer(Player player, Arena arena) {
        for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(arena)) {
            if (scoreBoard.getName().equals("Kills")) {
                scoreBoard.removePlayer(player);
                scoreBoard.resetScore(player.getName());
                scoreBoard.resetPlayerColor(player);
            }
        }
    }

    @Override
    public void onPlayerDeath(Arena arena, PlayerDeathEvent event) {
        Player player = event.getEntity();
        String killerName = null;
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            killerName = killer.getName();
        }
        for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(arena)) {
            if (scoreBoard.getName().equals("Kills") && killerName != null) {
                scoreBoard.setScore(killerName, scoreBoard.getScore(killerName) + 1);
            }
        }
        event.getDrops().clear();
        ultimateGames.getUtils().autoRespawn(player);
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
        fish.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
        player.getInventory().addItem(fish, ultimateGames.getUtils().createInstructionBook(game));
        player.updateInventory();
    }
}
