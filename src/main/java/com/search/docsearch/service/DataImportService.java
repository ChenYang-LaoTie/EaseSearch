package com.search.docsearch.service;

import java.util.Set;

public interface DataImportService {

    void refreshDoc();

    void sendKafka(String data, String parameter);

    void listenKafka();

    void deleteExpired(Set<String> idSet);

    void globalLock();

    void globalUnlock();
}
