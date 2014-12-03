/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2014, UltimateGames Staff <https://github.com/UltimateGames//>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.arenas.Arena;
import me.ampayne2.ultimategames.api.arenas.scoreboards.Scoreboard;
import me.ampayne2.ultimategames.api.arenas.spawnpoints.PlayerSpawnPoint;
import me.ampayne2.ultimategames.api.games.Game;
import me.ampayne2.ultimategames.api.games.GamePlugin;
import me.ampayne2.ultimategames.api.players.points.PointManager;
import me.ampayne2.ultimategames.api.utils.BossBar;
import me.ampayne2.ultimategames.api.utils.UGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private Map<String, KillStreak> streaks = new HashMap<>();
    private final static ItemStack RAW_FISH;
    private final static ItemStack COOKED_FISH;
    private final static ItemStack RAW_SALMON;
    private final static ItemStack COOKED_SALMON;
    private final static ItemStack CLOWNFISH;
    private final static ItemStack PUFFERFISH;
    private final static Vector HORIZONTAL = new Vector(3, 0, 3);
    private final static Vector VERTICAL = new Vector(0, 2, 0);
    private final static Map<String, String> killers = new HashMap<>();

    @Override
    public boolean loadGame(UltimateGames ultimateGames, Game game) {
        this.ultimateGames = ultimateGames;
        this.game = game;
        game.setMessages(FSMessage.class);
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
        Scoreboard scoreBoard = ultimateGames.getScoreboardManager().createScoreboard(arena, "Kills");
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
    public boolean openArena(Arena arena) {
        return true;
    }

    @Override
    public boolean stopArena(Arena arena) {
        return true;
    }

    @Override
    public boolean addPlayer(Player player, Arena arena) {
        final String playerName = player.getName();
        PlayerSpawnPoint spawnPoint = ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena);
        spawnPoint.lock(false);
        spawnPoint.teleportPlayer(player);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        resetInventory(player);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        Scoreboard scoreBoard = ultimateGames.getScoreboardManager().getScoreboard(arena);
        if (scoreBoard != null) {
            scoreBoard.addPlayer(player);
            scoreBoard.setScore(playerName, 0);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateGames.getPlugin(), new Runnable() {
            @Override
            public void run() {
                streaks.put(playerName, new KillStreak(ultimateGames, game, ultimateGames.getPlayerManager().getArenaPlayer(playerName)));
            }
        });

        BossBar.setStatusBar(player, ChatColor.AQUA + "Welcome to Fishslap Orbit!", 1);
        return true;
    }

    @Override
    public void removePlayer(Player player, Arena arena) {
        String playerName = player.getName();
        Scoreboard scoreBoard = ultimateGames.getScoreboardManager().getScoreboard(arena);
        if (scoreBoard != null) {
            scoreBoard.resetScore(playerName);
        }
        streaks.remove(playerName);
        killers.remove(playerName);
        for (String arenaPlayer : new ArrayList<>(killers.keySet())) {
            if (killers.get(arenaPlayer).equals(playerName)) {
                killers.remove(arenaPlayer);
            }
        }
        BossBar.removeStatusBar(player);
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
            Scoreboard scoreBoard = ultimateGames.getScoreboardManager().getScoreboard(arena);
            if (scoreBoard != null) {
                scoreBoard.setScore(killerName, scoreBoard.getScore(killerName) + 1);
                ultimateGames.getPointManager().addPoint(game, killerName, "store", 1);
                ultimateGames.getPointManager().addPoint(game, killerName, "kill", 1);
                streaks.get(killerName).increaseCount();
            }
            killers.remove(playerName);
            for (String arenaPlayer : new ArrayList<>(killers.keySet())) {
                if (killers.get(arenaPlayer).equals(playerName)) {
                    killers.remove(arenaPlayer);
                }
            }
        }
        event.getDrops().clear();
        UGUtils.autoRespawn(ultimateGames.getPlugin(), player);
    }

    @Override
    public void onPlayerRespawn(Arena arena, PlayerRespawnEvent event) {
        event.setRespawnLocation(ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena).getLocation());
        resetInventory(event.getPlayer());
        final String playerName = event.getPlayer().getName();
        Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateGames.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    BossBar.removeStatusBar(player);
                    BossBar.setStatusBar(player, ChatColor.AQUA + "Welcome to Fishslap Orbit!", 1);
                }
            }
        }, 0);
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
        } else if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            event.setDamage(((Player) event.getEntity()).getHealth());
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
        player.getInventory().addItem(UGUtils.createInstructionBook(game));
        PointManager pointManager = ultimateGames.getPointManager();
        String playerName = player.getName();
        if (pointManager.hasPerk(game, playerName, "fish6")) {
            player.getInventory().addItem(PUFFERFISH);
        } else if (pointManager.hasPerk(game, playerName, "fish5")) {
            player.getInventory().addItem(CLOWNFISH);
        } else if (pointManager.hasPerk(game, playerName, "fish4")) {
            player.getInventory().addItem(COOKED_SALMON);
        } else if (pointManager.hasPerk(game, playerName, "fish3")) {
            player.getInventory().addItem(RAW_SALMON);
        } else if (pointManager.hasPerk(game, playerName, "fish2")) {
            player.getInventory().addItem(COOKED_FISH);
        } else {
            player.getInventory().addItem(RAW_FISH);
        }
        player.updateInventory();
    }

    static {
        RAW_FISH = new ItemStack(Material.RAW_FISH, 1);
        RAW_FISH.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        COOKED_FISH = new ItemStack(Material.COOKED_FISH, 1);
        COOKED_FISH.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        RAW_SALMON = new ItemStack(Material.RAW_FISH, 1, (short) 1);
        RAW_SALMON.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        COOKED_SALMON = new ItemStack(Material.COOKED_FISH, 1, (short) 1);
        COOKED_SALMON.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        CLOWNFISH = new ItemStack(Material.RAW_FISH, 1, (short) 2);
        CLOWNFISH.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        PUFFERFISH = new ItemStack(Material.RAW_FISH, 1, (short) 3);
        PUFFERFISH.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
    }
}
