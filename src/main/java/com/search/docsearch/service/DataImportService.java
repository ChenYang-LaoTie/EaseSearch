package com.search.docsearch.service;

import java.io.IOException;
import java.util.Set;

public interface DataImportService {

    void refreshDoc();

    void addForum(String data, String parameter);

    void deleteExpired(Set<String> idSet);

    void globalLock() throws IOException;

    void globalUnlock();
}
