package com.tux2mc.multiinv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import com.tux2mc.debugreport.DebugReport;

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.Player;
import org.spongepowered.api.event.SpongeEventHandler;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.World;

import com.tux2mc.multiinv.command.MICommand;
import com.tux2mc.multiinv.inventory.MIInventory;
import com.tux2mc.multiinv.listener.MIPlayerListener;
import com.tux2mc.multiinv.logger.MILogger;

@Plugin(id = "MultiInv", name = "MultiInv", version="3.1.8")
public class MultiInv {
    
    // Initialize logger (auto implements enable/disable messages to console)
    public static MILogger log;
    public int xpversion = 0;
    private MultiInvAPI api;
    
    private static Game currentgame = null;
    
    //public DebugReport dreport = null;
    
    private ArrayList<String> grouplist = new ArrayList<String>();
    
    // Listeners
    MIPlayerListener playerListener;
    
    @SpongeEventHandler
    public void onDisable(ServerStoppingEvent event) {
        MIYamlFiles.saveLogoutWorlds();

        //If we save on quit we also want to save on disable!
		if(MIYamlFiles.saveonquit) {
			Collection<Player> players = event.getGame().getOnlinePlayers();
			for(Player player : players) {
				String currentworld = MIPlayerListener.getGroup(player.getLocation().getWorld());
	        	if(!player.hasPermission("multiinv.enderchestexempt")) {
	                // Load the enderchest inventory for this world from file.
	                playerListener.saveEnderchestState(player, currentworld);
	            }
	            if(!player.hasPermission("multiinv.exempt")) {
	                // Load the inventory for this world from file.
	                playerListener.savePlayerState(player, currentworld);
	            }
			}
		}
	
    }
    
    @SpongeEventHandler
    public void onEnable(ServerStartedEvent event) {
    	currentgame = event.getGame();
        // Initialize Logger
        log = new MILogger();
        
        // Load yaml files
        MIYamlFiles.loadConfig();
        MIYamlFiles.loadGroups();
        MIYamlFiles.loadPlayerLogoutWorlds();
        
        // Adding in metrics
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch(IOException e) {
            // Failed to submit the stats :-(
        }
        
        // An easy way to set the default logging levels
        if(MIYamlFiles.config.contains("loglevel")) {
            try {
                log.setLogLevel(MILogger.Level.valueOf(MIYamlFiles.config.getString("loglevel").toUpperCase()));
            } catch(Exception e) {
                log.warning("Log level value invalid! Valid values are: NONE, SEVERE, WARNING, INFO and DEBUG.");
                log.warning("Setting log level to INFO.");
                log.setLogLevel(MILogger.Level.INFO);
            }
        } else {
            // Set a sane level for logging
            log.setLogLevel(MILogger.Level.INFO);
        }
        
        // Initialize listeners
        playerListener = new MIPlayerListener(this);
        
        // Register required events
        currentgame.getEventManager().register(playerListener);
        String[] cbversionstring = getServer().getVersion().split(":");
        String[] versionstring = cbversionstring[1].split("\\.");
        try {
            int majorversion = Integer.parseInt(versionstring[0].trim());
            int minorversion = Integer.parseInt(versionstring[1].trim());
            if(majorversion == 1) {
                if(minorversion > 2) {
                    xpversion = 1;
                    log.info("MC 1.3 or above found, enabling version 2 XP handling.");
                } else {
                    log.info("MC 1.2 or below found, enabling version 1 XP handling.");
                }
            } else if(majorversion > 1) {
                xpversion = 1;
                log.info("MC 1.3 or above found, enabling version 2 XP handling.");
            }
        } catch(Exception e) {
            log.severe("Unable to get server version! Inaccurate XP handling may occurr!");
            log.severe("Server Version String: " + getServer().getVersion());
        }
        
        api = new MultiInvAPI(this);
        if(!MIYamlFiles.usesql) {
            File groupsfolder = new File(getDataFolder(), "Groups");
            if(groupsfolder.exists()) {
            	//Let's convert!
            	log.info("Older data folder detected. Converting users to UUID, please wait...");
            	convertToUUID();
            	log.info("Conversion complete!");
            }
        }else if(MIYamlFiles.con != null) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, MIYamlFiles.con, 20, 20);
        }
        
        getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			
			@Override
			public void run() {
				MIYamlFiles.saveLogoutWorlds();
			}
		}, 60, 20);
        scanWorlds();
        loadReportPlugin();
    }
    
    private void loadReportPlugin() {
    	if(Bukkit.getPluginManager().isPluginEnabled("DebugReport")) {
    		dreport = DebugReport.getInstance();
    	}
    }
    
    private void convertToUUID() {
        File groupsfolder = new File(getDataFolder(), "Groups");
        File uuidgroupsfolder = new File(getDataFolder(), "UUIDGroups");
        groupsfolder.renameTo(uuidgroupsfolder);
        if(uuidgroupsfolder.exists() && uuidgroupsfolder.isDirectory()) {
        	File[] groups = uuidgroupsfolder.listFiles();
        	for(File gfolder : groups) {
        		if(gfolder.isDirectory()) {
        			File[] users = gfolder.listFiles();
        			for(File user : users) {
        				if(user.isFile()) {
        					String filename = user.getName();
        					if(filename.endsWith(".ec.yml")) {
        						String username = filename.substring(0, filename.indexOf("."));
        						log.debug("Converting " + username + "'s enderchest file.");
        						OfflinePlayer ouser = Bukkit.getOfflinePlayer(username);
        						File newname = new File(user.getParent(), ouser.getUniqueId().toString() + ".ec.yml");
        						user.renameTo(newname);
        					}else if(filename.endsWith(".yml")) {
        						String username = filename.substring(0, filename.lastIndexOf("."));
        						log.debug("Converting " + username + "'s inventory file.");
        						OfflinePlayer ouser = Bukkit.getOfflinePlayer(username);
        						File newname = new File(user.getParent(), ouser.getUniqueId().toString() + ".yml");
        						user.renameTo(newname);
        					}
        				}
        			}
        		}
        	}
        }
    }
    
    public MultiInvAPI getAPI() {
        return api;
    }
    
    public int[] getXP(int totalxp) {
        int level = 0;
        int leftoverexp = totalxp;
        int xpneededforlevel = 0;
        if(xpversion == 1) {
            xpneededforlevel = 17;
            while(leftoverexp >= xpneededforlevel) {
                level++;
                leftoverexp -= xpneededforlevel;
                if(level >= 16) {
                    xpneededforlevel += 3;
                }
            }
            // We only have 2 versions at the moment
        } else {
            xpneededforlevel = 7;
            boolean odd = true;
            while(leftoverexp >= xpneededforlevel) {
                level++;
                leftoverexp -= xpneededforlevel;
                if(odd) {
                    xpneededforlevel += 3;
                    odd = false;
                } else {
                    xpneededforlevel += 4;
                    odd = true;
                }
            }
        }
        return new int[]{level, leftoverexp, xpneededforlevel};
    }
    
    public int getTotalXP(int level, float xp) {
        int atlevel = 0;
        int totalxp = 0;
        int xpneededforlevel = 0;
        if(xpversion == 1) {
            xpneededforlevel = 17;
            while(atlevel < level) {
                atlevel++;
                totalxp += xpneededforlevel;
                if(atlevel >= 16) {
                    xpneededforlevel += 3;
                }
            }
            // We only have 2 versions at the moment
        } else {
            xpneededforlevel = 7;
            boolean odd = true;
            while(atlevel < level) {
                atlevel++;
                totalxp += xpneededforlevel;
                if(odd) {
                    xpneededforlevel += 3;
                    odd = false;
                } else {
                    xpneededforlevel += 4;
                    odd = true;
                }
            }
        }
        totalxp = (int) (totalxp + (xp * xpneededforlevel));
        return totalxp;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        MICommand.command(args, sender, this);
        return true;
    }
    
    public void scanWorlds() {
    	grouplist.clear();
    	Collection<World> worlds = currentgame.getWorlds();
    	for(World world : worlds) {
    		String group = playerListener.getGroup(world);
    		if(!grouplist.contains(group)) {
    			grouplist.add(group);
    		}
    	}
    }
    
    public void addWorld(World world) {
    	String group = playerListener.getGroup(world);
		if(!grouplist.contains(group)) {
			grouplist.add(group);
		}
    }
    
    public ArrayList<String> getAllGroups() {
    	return grouplist;
    }
    
    public static Game getCurrentGame() {
    	return currentgame;
    }
}
