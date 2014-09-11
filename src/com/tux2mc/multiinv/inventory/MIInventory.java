package com.tux2mc.multiinv.inventory;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import org.spongepowered.api.entity.Player;
import org.spongepowered.api.inventory.ItemStack;

/**
 * Created by IntelliJ IDEA. User: Pluckerpluck Date: 17/12/11 Time: 12:32 To change this template use File | Settings | File Templates.
 */
public class MIInventory implements Serializable {
    
    /**
	 * 
	 */
    private static final long serialVersionUID = 8695822035863770397L;
    protected MIItemStack[] MIInventoryContents = new MIItemStack[36];
    protected MIItemStack[] MIArmourContents = new MIItemStack[4];
    protected LinkedList<PotionEffect> potioneffects = new LinkedList<PotionEffect>();
    
    // Create an MIInventory from a Player
    public MIInventory(Player player) {
        // Iterate and store inventory contents
        ItemStack[] inventoryContents = player.getInventory().getContents();
        for(int i = 0; i < inventoryContents.length; i++) {
            MIInventoryContents[i] = new MIItemStack(inventoryContents[i]);
        }
        
        // Iterate and store armour contents
        ItemStack[] armourContents = player.getInventory().getArmorContents();
        
        for(int i = 0; i < armourContents.length; i++) {
            MIArmourContents[i] = new MIItemStack(armourContents[i]);
        }
        
        potioneffects = new LinkedList<PotionEffect>(player.getActivePotionEffects());
    }
    
    public MIInventory(ItemStack[] inventory, ItemStack[] armor, Collection<PotionEffect> effects) {
        if(inventory != null) {
            for(int i = 0; i < inventory.length && i < 36; i++) {
                MIInventoryContents[i] = new MIItemStack(inventory[i]);
            }
        } else {
            for(int i = 0; i < MIInventoryContents.length; i++) {
                MIInventoryContents[i] = new MIItemStack("");
            }
        }
        if(armor != null) {
            for(int i = 0; i < armor.length && i < 4; i++) {
                MIArmourContents[i] = new MIItemStack(armor[i]);
            }
        } else {
            for(int i = 0; i < MIArmourContents.length; i++) {
                MIArmourContents[i] = new MIItemStack("");
            }
        }
        potioneffects = new LinkedList<PotionEffect>(effects);
        
    }
    
    // Create an MIInventory from a string containing inventory data
    public MIInventory(String inventoryString) {
        if(inventoryString != null && !inventoryString.equals("")) {
            // data[0] = inventoryContents
            // data[1] = armourContents
            // data[2] = potionEffects
            String[] data = inventoryString.split(":");
            
            // Fill MIInventoryContents
            String[] inventoryData = data[0].split(";");
            for(int i = 0; i < inventoryData.length; i++) {
                MIInventoryContents[i] = new MIItemStack(inventoryData[i]);
            }
            
            // Fill MIArmourContents
            if(data.length > 1) {
                String[] armourData = data[1].split(";");
                for(int i = 0; i < armourData.length; i++) {
                    MIArmourContents[i] = new MIItemStack(armourData[i]);
                }
                if(data.length > 2) {
                    String[] activepotions = data[2].split(";");
                    for(String potion : activepotions) {
                        String[] thepotion = potion.split(",");
                        if(thepotion.length >= 3) {
                            String potionname = thepotion[0];
                            int duration = Integer.parseInt(thepotion[1]);
                            int amplifier = Integer.parseInt(thepotion[2]);
                            PotionEffect effect = new PotionEffect(PotionEffectType.getByName(potionname), duration, amplifier);
                            potioneffects.add(effect);
                        }
                    }
                }
            } else {
                for(int i = 0; i < 4; i++) {
                    MIArmourContents[i] = new MIItemStack("");
                }
            }
        }
    }
    
    public MIInventory() {
    }
    
    public void loadIntoInventory(PlayerInventory inventory) {
        // Iterate and get inventory contents
        ItemStack[] inventoryContents = new ItemStack[MIInventoryContents.length];
        for(int i = 0; i < inventoryContents.length; i++) {
            if(MIInventoryContents[i] != null) {
                inventoryContents[i] = MIInventoryContents[i].getItemStack();
            } else {
                inventoryContents[i] = null;
            }
            inventory.setContents(inventoryContents);
        }
        
        // Iterate and get armour contents
        ItemStack[] armourContents = new ItemStack[MIArmourContents.length];
        for(int i = 0; i < armourContents.length; i++) {
            if(MIArmourContents[i] != null) {
                armourContents[i] = MIArmourContents[i].getItemStack();
            } else {
                armourContents[i] = null;
            }
            inventory.setArmorContents(armourContents);
        }
    }
    
    public MIItemStack[] getInventoryContents() {
        return MIInventoryContents;
    }
    
    public MIItemStack[] getArmorContents() {
        return MIArmourContents;
    }
    
    public LinkedList<PotionEffect> getPotions() {
        return potioneffects;
    }
    
    public String toString() {
        // Initial capacity = (20 + 4) * 7 - 1
        StringBuilder inventoryString = new StringBuilder(167);
        
        // Add MIInventoryContents
        for(MIItemStack itemStack : MIInventoryContents) {
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }
        
        // Replace last ";" with ":" (makes string look nicer)
        inventoryString.deleteCharAt(inventoryString.length() - 1);
        inventoryString.append(":");
        
        // Add MIArmourContents
        for(MIItemStack itemStack : MIArmourContents) {
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }
        
        if(potioneffects.size() > 0) {
            
            // Replace last ";" with ":" (makes string look nicer)
            inventoryString.deleteCharAt(inventoryString.length() - 1);
            inventoryString.append(":");
            
            for(PotionEffect effect : potioneffects) {
                String type = effect.getType().getName();
                int duration = effect.getDuration();
                int amplifier = effect.getAmplifier();
                inventoryString.append(type + "," + duration + "," + amplifier + ";");
            }
        }
        
        // Remove final ";"
        inventoryString.deleteCharAt(inventoryString.length() - 1);
        
        return inventoryString.toString();
    }
    
    public void setPotionEffects(Player player) {
        Collection<PotionEffect> currenteffects = player.getActivePotionEffects();
        for(PotionEffect effect : currenteffects) {
            player.removePotionEffect(effect.getType());
        }
        player.addPotionEffects(potioneffects);
        
    }
}
