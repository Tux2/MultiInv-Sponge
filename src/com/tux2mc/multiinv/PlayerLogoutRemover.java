package com.tux2mc.multiinv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.tux2mc.multiinv.listener.MIPlayerListener;

public class PlayerLogoutRemover implements Runnable {
	
	String playername;
	
	public PlayerLogoutRemover(String player) {
		playername = player;
	}

	@Override
	public void run() {
		Player player = Bukkit.getPlayerExact(playername);
		if(player == null || !player.isOnline()) {
			MIPlayerListener.removePlayer(playername);
		}
	}

}
