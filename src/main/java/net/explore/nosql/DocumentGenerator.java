package net.explore.nosql;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public interface DocumentGenerator {
    public CompletableFuture<ArrayList<String>> generateDocuments(File templateFile, int numberOfDocuments);
}
