package com.search.docsearch.config;
public class mySystem {

    public String system;
    public String docsVersion;
    public String index;
    public String mappingPath;
    public String basePath;
    public String initDoc;
    public String updateDoc;

    public mySystem(String system, String docsVersion, String index, String mappingPath, String basePath, String initDoc, String updateDoc) {
        this.system = system;
        this.docsVersion = docsVersion;
        this.index = index;
        this.mappingPath = mappingPath;
        this.basePath = basePath;
        this.initDoc = initDoc;
        this.updateDoc = updateDoc;
    }
}
