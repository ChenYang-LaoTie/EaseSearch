package com.search.docsearch.config;

import java.io.File;

public class InitConfigValue {
    
    public static void deletEvnformApplication() {
        System.out.println("deletEvnformApplication");

        try{
            File buildConfig = new File("/EaseSearch/target/classes/application.yml");
            if(buildConfig.delete()){
                System.out.println(buildConfig.getName() + " 文件已被删除！");
            }else{
                System.out.println("文件删除失败！");
            }
            
            File originConfig = new File("/EaseSearch/src/main/resources/application.yml");
            if(originConfig.delete()){
                System.out.println(originConfig.getName() + " 文件已被删除！");
            }else{
                System.out.println("文件删除失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
    }

    
}
