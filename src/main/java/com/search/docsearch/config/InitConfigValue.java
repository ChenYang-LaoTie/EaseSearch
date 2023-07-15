package com.search.docsearch.config;

public class InitConfigValue {

    // public String elasticsearchHost;

    // public int elasticsearchPort;

    // public String elasticsearchUserName;

    // public String elasticsearchPassword;

    // public boolean isDev;

    
    public static void deletEvnformApplication() {
        System.out.println("deletEvnformApplication");
        
        //delete evn
        System.getProperties().remove("eshost");
        System.getProperties().remove("esport");
        System.getProperties().remove("esusername");
        System.getProperties().remove("espassword");
    }

    
}
