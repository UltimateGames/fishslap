package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.ArenaPlayer;
import me.ampayne2.ultimategames.players.streaks.Streak;

public class KillStreak extends Streak {
    private UltimateGames ultimateGames;
    private Game game;

    public KillStreak(UltimateGames ultimateGames, Game game, ArenaPlayer player) {
        super(player, new KillStreakAction(ultimateGames, game, 5, "KillingSpree"),
                new KillStreakAction(ultimateGames, game, 10, "Rampage"),
                new KillStreakAction(ultimateGames, game, 15, "Domination"),
                new KillStreakAction(ultimateGames, game, 20, "Unstoppable"),
                new KillStreakAction(ultimateGames, game, 25, "God"));
        this.ultimateGames = ultimateGames;
        this.game = game;
    }

    @Override
    public void reset() {
        if (getCount() >= 5) {
            ultimateGames.getMessenger().sendGameMessage(getPlayer().getArena(), game, "Shutdown", getPlayer().getPlayerName());
        }
        super.reset();
    }
}
