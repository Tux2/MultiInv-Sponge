package com.tux2mc.multiinv.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.entity.Player;

import com.tux2mc.multiinv.MIYamlFiles;
import com.tux2mc.multiinv.MultiInv;
import com.tux2mc.multiinv.books.MIBook;
import com.tux2mc.multiinv.inventory.MIEnderchestInventory;
import com.tux2mc.multiinv.inventory.MIInventory;
import com.tux2mc.multiinv.player.MIPlayerFile;

/**
 * Created by IntelliJ IDEA. User: Pluckerpluck Date: 19/12/11 Time: 22:58 To change this template use File | Settings | File Templates.
 */
public class MICommand {
    
    MultiInv plugin;
    
    public MICommand(MultiInv plugin) {
        this.plugin = plugin;
    }
    
    public static void command(String[] strings, CommandSender sender, MultiInv plugin) {
        Player player = null;
        if(sender instanceof Player) {
            player = (Player) sender;
        }
        if(strings.length > 0) {
            String command = strings[0];
            
            // Check to see if the player has the permission to run this command.
            if(player != null && !player.hasPermission("multiinv." + command.toLowerCase())) {
                return;
            }
            
            // Populate a new args array
            String[] args = new String[strings.length - 1];
            for(int i = 1; i < strings.length; i++) {
                args[i - 1] = strings[i];
            }
            if(command.equalsIgnoreCase("report")) {
            	if(plugin.dreport != null) {
            		LinkedList<String> customdata = new LinkedList<String>();
            		customdata.add("MultiInv Custom Data");
            		customdata.add("================================");
            		customdata.add("-----------config.yml-----------");
            		BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "config.yml"));
	            		String line = null;
	            		while ((line = reader.readLine()) != null) {
	            		    if(line.startsWith("  password:")) {
	            		    	line = "  password: NOTSHOWN";
	            		    }
	            		    customdata.add(line);
	            		}
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}finally {
						if(reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
							}
							reader = null;
						}
					}
            		customdata.add("-----------groups.yml-----------");
					try {
						reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "groups.yml"));
	            		String line = null;
	            		while ((line = reader.readLine()) != null) {
	            		    customdata.add(line);
	            		}
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}finally {
						if(reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
							}
						}
					}
					plugin.dreport.createReport(sender, customdata);
            	}else {
            		sender.sendMessage(ChatColor.RED + "In order to generate a debug report you need the plugin DebugReport!");
            	}
            }else if(command.equalsIgnoreCase("reload")) {
                MIYamlFiles.loadConfig();
                MIYamlFiles.loadGroups();
                sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv configs reloaded!");
            } else if(command.equalsIgnoreCase("import")) {
                if(importInventories()) {
                    sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv flat files converted to mysql!");
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, something isn't set up right... Import aborted.");
                }
                
            }
        }else {
        	if(sender.hasPermission("multiinv.import")) {
        		sender.sendMessage(ChatColor.GOLD + "Import Commands:");
        		sender.sendMessage(ChatColor.GOLD + "/multiinv import" + ChatColor.AQUA + " - Import from fat file to mySQL");
        	}
        	if(sender.hasPermission("multiinv.reload")) {
        		sender.sendMessage(ChatColor.GOLD + "/multiinv reload" + ChatColor.AQUA + " - Reloads config files.");
        	}
        }
    }
    
    private static boolean importInventories() {
        if(MIYamlFiles.con == null) {
            System.out.println("[MultiInv] No sql connection, not converting.");
            return false;
        }
        System.out.println("getting World Inventories Directory");
        String worldinventoriesdir = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder().getAbsolutePath() + File.separator + "UUIDGroups";
        File worldinvdir = new File(worldinventoriesdir);
        if(worldinvdir.exists()) {
            File[] thedirs = worldinvdir.listFiles();
            for(File fdir : thedirs) {
                if(fdir.isDirectory()) {
                    String group = fdir.getName();
                    System.out.println("In group directory " + group);
                    File[] playerfiles = fdir.listFiles();
                    for(File pfile : playerfiles) {
                        if(pfile.getName().endsWith(".yml") && !pfile.getName().endsWith(".ec.yml")) {
                            String suuid = pfile.getName().substring(0, pfile.getName().lastIndexOf("."));
                            OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(suuid));
                            System.out.println("Importing player " + player1.getName() + " with UUID: " + suuid);
                            MIPlayerFile playerfile = new MIPlayerFile(player1, fdir.getName());
                            MIYamlFiles.con.saveExperience(player1, group, playerfile.getTotalExperience());
                            if(playerfile.getGameMode() != null) {
                                MIYamlFiles.con.saveGameMode(player1, group, playerfile.getGameMode());
                            }
                            MIYamlFiles.con.saveHealth(player1, group, playerfile.getHealth());
                            MIYamlFiles.con.saveHunger(player1, group, playerfile.getHunger());
                            if(playerfile.getInventory("SURVIVAL") != null) {
                                try {
                                    MIYamlFiles.con.saveInventory(player1, group, playerfile.getInventory("SURVIVAL"), "SURVIVAL");
                                } catch(NullPointerException e) {
                                    // We need to catch this, otherwise it goes wild sometimes... not a pretty sight to see...
                                }
                            }
                            if(playerfile.getInventory("CREATIVE") != null) {
                                try {
                                    MIYamlFiles.con.saveInventory(player1, group, playerfile.getInventory("CREATIVE"), "CREATIVE");
                                } catch(NullPointerException e) {
                                    // We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
                                }
                            }
                            if(playerfile.getInventory("ADVENTURE") != null) {
                                try {
                                    MIYamlFiles.con.saveInventory(player1, group, playerfile.getInventory("ADVENTURE"), "ADVENTURE");
                                } catch(NullPointerException e) {
                                    // We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
                                }
                            }
                            if(playerfile.getEnderchestInventory("SURVIVAL") != null) {
                                try {
                                    MIYamlFiles.con.saveEnderchestInventory(player1, group, playerfile.getEnderchestInventory("SURVIVAL"), "SURVIVAL");
                                } catch(NullPointerException e) {
                                    // We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
                                }
                            }
                            if(playerfile.getEnderchestInventory("CREATIVE") != null) {
                                try {
                                    MIYamlFiles.con.saveEnderchestInventory(player1, group, playerfile.getEnderchestInventory("CREATIVE"), "CREATIVE");
                                } catch(NullPointerException e) {
                                    // We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
                                }
                            }
                            if(playerfile.getEnderchestInventory("ADVENTURE") != null) {
                                try {
                                    MIYamlFiles.con.saveEnderchestInventory(player1, group, playerfile.getEnderchestInventory("ADVENTURE"), "ADVENTURE");
                                } catch(NullPointerException e) {
                                    // We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
                                }
                            }
                            MIYamlFiles.con.saveSaturation(player1, group, playerfile.getSaturation());
                            
                        }
                    }
                }
            }
            String booksdir = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder().getAbsolutePath() + File.separator + "books";
            File fbooksdir = new File(booksdir);
            if(fbooksdir.exists()) {
                System.out.println("books directory found, importing books.");
                File[] thebooks = fbooksdir.listFiles();
                for(File fdir : thebooks) {
                    if(fdir.isFile() && fdir.getName().endsWith(".yml")) {
                        System.out.println("Importing book " + fdir.getName());
                        MIBook thebook = new MIBook(fdir);
                        MIYamlFiles.con.saveBook(thebook, true);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
