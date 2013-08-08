package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.ArenaScoreboard;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.json.JSONArray;
import me.ampayne2.ultimategames.json.JSONException;
import me.ampayne2.ultimategames.json.JSONObject;
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
        JSONArray jsonArray = new JSONArray();


        for (ArenaScoreboard scoreBoard : ug.getScoreboardManager().getArenaScoreboards(arena)) {
            if (scoreBoard.getName().equals("Kills")) {
                for (String playerName: arena.getPlayers()) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.append(playerName, scoreBoard.getScore(playerName));
                        jsonArray.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
        return jsonArray.toString();
    }
}
