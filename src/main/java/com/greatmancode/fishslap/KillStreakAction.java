package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.games.Game;
import me.ampayne2.ultimategames.api.players.ArenaPlayer;
import me.ampayne2.ultimategames.api.players.streaks.StreakAction;

public class KillStreakAction extends StreakAction {
    private UltimateGames ultimateGames;
    private Game game;
    private FSMessage message;

    public KillStreakAction(UltimateGames ultimateGames, Game game, int requiredKills, FSMessage message) {
        super(requiredKills);
        this.ultimateGames = ultimateGames;
        this.game = game;
        this.message = message;
    }

    @Override
    public void perform(ArenaPlayer player) {
        ultimateGames.getMessenger().sendGameMessage(player.getArena(), game, message, player.getPlayerName());
        ultimateGames.getPointManager().addPoint(game, player.getPlayerName(), "store", 1);
    }
}
