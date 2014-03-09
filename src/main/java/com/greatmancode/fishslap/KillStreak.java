package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.games.Game;
import me.ampayne2.ultimategames.api.players.ArenaPlayer;
import me.ampayne2.ultimategames.api.players.streaks.Streak;

public class KillStreak extends Streak {
    private UltimateGames ultimateGames;
    private Game game;

    public KillStreak(UltimateGames ultimateGames, Game game, ArenaPlayer player) {
        super(player, new KillStreakAction(ultimateGames, game, 5, FSMessage.KILLING_SPREE),
                new KillStreakAction(ultimateGames, game, 10, FSMessage.RAMPAGE),
                new KillStreakAction(ultimateGames, game, 15, FSMessage.DOMINATION),
                new KillStreakAction(ultimateGames, game, 20, FSMessage.UNSTOPPABLE),
                new KillStreakAction(ultimateGames, game, 25, FSMessage.GOD));

        this.ultimateGames = ultimateGames;
        this.game = game;
    }

    @Override
    public void reset() {
        if (getCount() >= 5) {
            ultimateGames.getMessenger().sendGameMessage(getPlayer().getArena(), game, FSMessage.SHUTDOWN, getPlayer().getPlayerName());
        }
        super.reset();
    }
}
