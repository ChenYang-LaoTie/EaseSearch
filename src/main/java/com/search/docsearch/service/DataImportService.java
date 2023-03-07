package com.search.docsearch.service;

import java.io.IOException;
import java.util.Set;

public interface DataImportService {

    void refreshDoc() throws IOException;

    void sendKafka(String data, String parameter);

    void listenKafka();

    void deleteExpired(Set<String> idSet);
}
