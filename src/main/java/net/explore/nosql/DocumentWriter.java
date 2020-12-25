package net.explore.nosql;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public interface DocumentWriter {
    public CompletableFuture<ArrayList<String>> WriteDocuments(ArrayList<String> fileContent);
}
