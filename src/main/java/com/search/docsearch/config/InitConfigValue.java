package com.search.docsearch.config;

import java.io.File;

public class InitConfigValue {

    // public String elasticsearchHost;

    // public int elasticsearchPort;

    // public String elasticsearchUserName;

    // public String elasticsearchPassword;

    // public boolean isDev;

    
    public static void deletEvnformApplication() {
        System.out.println("deletEvnformApplication");

        try{
            File file = new File("/EaseSearch/target/classes/application.yml");
            if(file.delete()){
                System.out.println(file.getName() + " 文件已被删除！");
            }else{
                System.out.println("文件删除失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
    }

    
}
