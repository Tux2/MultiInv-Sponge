package com.tux2mc.multiinv;

import java.util.UUID;

import org.spongepowered.api.entity.Player;

import com.tux2mc.multiinv.listener.MIPlayerListener;

public class PlayerLogoutRemover implements Runnable {
	
	String playername;
	UUID playerUUID;
	
	public PlayerLogoutRemover(String player, UUID uuid) {
		playername = player;
		playerUUID = uuid;
	}

	@Override
	public void run() {
		Player player = MultiInv.getCurrentGame().getPlayer(playerUUID);
		if(player == null || !player.isOnline()) {
			MIPlayerListener.removePlayer(playername);
		}
	}

}
