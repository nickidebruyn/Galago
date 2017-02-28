/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.listener;

import java.util.Properties;

/**
 *
 * @author nidebruyn
 */
public interface RemoteActionListener {
    
    /**
     * Send properties to the method and return a string value.
     * @param properties
     * @return 
     */
    public String doAction(Properties properties);
    
}
