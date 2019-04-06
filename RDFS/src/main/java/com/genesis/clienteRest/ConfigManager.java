/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.genesis.clienteRest;

import java.io.IOException;

/**
 *
 * @author Javier
 */

public class ConfigManager {

    public static String getProperty(String property) {
        java.util.Properties prop = new java.util.Properties();
        try {
            //System.out.println("GetProperty jj ");
            prop.load(ConfigManager.class.getResourceAsStream("/config.properties"));
            return prop.getProperty(property);
        } catch (IOException ex) {
            return null;
        }
    }

}

