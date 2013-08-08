package com.greatmancode.fishslap;

import java.util.ArrayList;
import java.util.List;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.ArenaScoreboard;
import me.ampayne2.ultimategames.api.GamePlugin;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.enums.ArenaStatus;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.SpawnPoint;
import org.bukkit.Bukkit;
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
        ultimateGames.getJettyServer().getHandler().addHandler("/Fishslap/"+arena.getName(), new FishSlapWebHandler(ultimateGames, arena));
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
        ultimateGames.getCountdownManager().createEndingCountdown(arena, ultimateGames.getConfigManager().getGameConfig(game).getConfig().getInt("CustomValues.GameTime"), true);
        for (ArenaScoreboard scoreBoard : new ArrayList<ArenaScoreboard>(ultimateGames.getScoreboardManager().getArenaScoreboards(arena))) {
            ultimateGames.getScoreboardManager().removeArenaScoreboard(arena, scoreBoard.getName());
        }
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Kills");
        for (String playerName : arena.getPlayers()) {
            scoreBoard.addPlayer(playerName);
            scoreBoard.setScore(playerName, 0);
        }
        scoreBoard.setVisible(true);
        return true;
    }

    @Override
    public void endArena(Arena arena) {
        String highestScorer = "Nobody";
        Integer highScore = 0;
        List<String> players = arena.getPlayers();
        for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(arena)) {
            if (scoreBoard.getName().equals("Kills")) {
                for (String playerName : players) {
                    Integer playerScore = scoreBoard.getScore(playerName);
                    if (playerScore > highScore) {
                        highestScorer = playerName;
                        highScore = playerScore;
                    }
                }
            }
        }
        ultimateGames.getScoreboardManager().removeArenaScoreboard(arena, "Kills");
        ultimateGames.getMessageManager().broadcastReplacedGameMessage(game, "GameEnd", highestScorer, game.getGameDescription().getName(), arena.getName());
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
    public Boolean addPlayer(Arena arena, String playerName) {
        if (arena.getPlayers().size() >= arena.getMinPlayers() && !ultimateGames.getCountdownManager().isStartingCountdownEnabled(arena)) {
            ultimateGames.getCountdownManager().createStartingCountdown(arena, ultimateGames.getConfigManager().getGameConfig(game).getConfig().getInt("CustomValues.StartWaitTime"));
        }
        SpawnPoint spawnPoint = ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena);
        spawnPoint.lock(false);
        spawnPoint.teleportPlayer(playerName);
        Player player = Bukkit.getPlayer(playerName);
        resetInventory(player);
        return true;
    }

    @Override
    public Boolean removePlayer(Arena arena, String playerName) {
        return true;
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
        if (arena.getStatus() != ArenaStatus.RUNNING) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setDamage(0.0);
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

    @SuppressWarnings("deprecation")
    private void resetInventory(Player player) {
        player.getInventory().clear();
        ItemStack stack = new ItemStack(Material.RAW_FISH);
        stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
        player.getInventory().addItem(stack);
        String playerName = player.getName();
        if (ultimateGames.getPlayerManager().isPlayerInArena(playerName)) {
            player.getInventory().addItem(ultimateGames.getUtils().createInstructionBook(ultimateGames.getPlayerManager().getPlayerArena(playerName).getGame()));
        }
        player.updateInventory();
    }
}
