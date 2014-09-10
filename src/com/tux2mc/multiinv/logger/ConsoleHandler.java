package com.tux2mc.multiinv.logger;

import org.apache.logging.log4j.Logger;

import com.tux2mc.multiinv.MultiInv;

/**
 * Created with IntelliJ IDEA. User: Pluckerpluck Date: 29/04/12
 */
public class ConsoleHandler implements Handler {
    
    private Logger log = MultiInv.getCurrentGame().getLogger();
    
    @Override
    public void info(String message) {
        log.info(message);
    }
    
    @Override
    public void warning(String message) {
        log.warn(message);
    }
    
    @Override
    public void severe(String message) {
        log.error(message);
    }
    
    @Override
    public void debug(String message) {
        log.info("[DEBUG] " + message);
    }
}
