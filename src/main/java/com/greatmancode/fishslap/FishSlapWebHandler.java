package com.greatmancode.fishslap;

import java.util.HashMap;
import java.util.Map;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.ArenaScoreboard;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.gson.Gson;
import me.ampayne2.ultimategames.webapi.WebHandler;
public class FishSlapWebHandler implements WebHandler {

    private Arena arena;
    private UltimateGames ug;
    
    public FishSlapWebHandler(UltimateGames ug, Arena arena) {
        this.arena = arena;
        this.ug = ug;
    }

    @Override
    public String sendResult() {
        Gson gson = new Gson();
        Map<String, Integer> map = new HashMap<String, Integer>();

        for (ArenaScoreboard scoreBoard : ug.getScoreboardManager().getArenaScoreboards(arena)) {
            if (scoreBoard.getName().equals("Kills")) {
                for (String playerName: arena.getPlayers()) {
                    map.put(playerName, scoreBoard.getScore(playerName));
                }
                break;
            }
        }
        return gson.toJson(map);
    }
}
