package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.ArenaPlayer;
import me.ampayne2.ultimategames.players.streaks.StreakAction;

public class KillStreakAction extends StreakAction {
    private UltimateGames ultimateGames;
    private Game game;
    private String messagePath;

    public KillStreakAction(UltimateGames ultimateGames, Game game, int requiredKills, String messagePath) {
        super(requiredKills);
        this.ultimateGames = ultimateGames;
        this.game = game;
        this.messagePath = messagePath;
    }

    @Override
    public void perform(ArenaPlayer player) {
        ultimateGames.getMessageManager().sendGameMessage(player.getArena(), game, messagePath, player.getPlayerName());
        ultimateGames.getPointManager().addPoint(game, player.getPlayerName(), "store", 1);
    }
}
