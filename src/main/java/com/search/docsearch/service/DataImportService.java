package com.search.docsearch.service;

import java.io.IOException;

public interface DataImportService {

    void refreshDoc() throws IOException;

    void asyncrefreshDoc() throws IOException;

    void sendKafka(String data, String parameter);

    void listenKafka();

    void synchronousData();

    void refreshSynIndex() ;
}
