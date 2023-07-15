package com.search.docsearch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeeeController {

    @Value("${elasticsearch.username}")
    private String userName;

    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;


    @GetMapping("/test")
    public String test() {
        System.out.println("userName = " + userName);
        System.out.println("password = " + password);
        System.out.println("host = " + host);
        System.out.println("port = " + port);
        
        return userName + password + host + port;
    }


    
}
