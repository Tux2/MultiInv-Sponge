package com.tux2mc.multiinv.inventory;

import java.io.File;
import java.io.IOException;

import org.spongepowered.api.entity.HumanEntity;
import org.spongepowered.api.entity.Player;

import com.tux2mc.multiinv.MIYamlFiles;
import com.tux2mc.multiinv.MultiInv;

public class DeferredEnderchestSave implements Runnable {
    
    Inventory inventory;
    Player player;
    String inventoryName;
    String group;
    
    public DeferredEnderchestSave(Inventory inventory, HumanEntity player, String group, String inventoryName) {
        this.inventory = inventory;
        this.player = (Player) player;
        this.inventoryName = inventoryName;
        this.group = group;
    }
    
    @Override
    public void run() {
        MIEnderchestInventory miinventory = new MIEnderchestInventory(inventory);
        if(MIYamlFiles.usesql) {
            MIYamlFiles.con.saveEnderchestInventory(player, group, miinventory, inventoryName);
        } else {
            // Find and load configuration file for the player's enderchest
            File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
            File worldsFolder = new File(dataFolder, "UUIDGroups");
            File file = new File(worldsFolder, group + File.separator + player.getUniqueId().toString() + ".ec.yml");
            String playername = player.getName();
            YamlConfiguration playerFile = new YamlConfiguration();
            if(file.exists()) {
                try {
                    playerFile.load(file);
                } catch(Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            String inventoryString = new MIEnderchestInventory(inventory).toString();
            playerFile.set(inventoryName, inventoryString);
            String folder = file.getParentFile().getName();
            MultiInv.log.debug("Saving " + playername + "'s " + inventoryName + " Enderchest inventory to " + folder);
            try {
                playerFile.save(file);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
