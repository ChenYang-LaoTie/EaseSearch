package com.search.docsearch.config;

public class InitConfigValue {

    // public String elasticsearchHost;

    // public int elasticsearchPort;

    // public String elasticsearchUserName;

    // public String elasticsearchPassword;

    // public boolean isDev;

    
    public static void deletEvnformApplication() {
        System.out.println("deletEvnformApplication");
        
        //update env    
        System.setProperty("eshost","***");
        System.setProperty("esport","***");
        System.setProperty("esusername","***");
        System.setProperty("espassword","***");
    }

    
}
