package net.explore.nosql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name="document-writer", havingValue = "file")
public class DocumentFileWriter implements DocumentWriter{
    Logger logger = LoggerFactory.getLogger(DocumentFileWriter.class);

    @Value("${folder-path}")
    String folderPathString;

    @Value("${document-format}")
    String fileExtension;

    @Override
    public CompletableFuture<ArrayList<String>> WriteDocuments(ArrayList<String> fileContents) {
        ArrayList<String> documentIdentifier = new ArrayList<>();
        try {
            if (!new File(folderPathString).exists()) {
                try {
                    Files.createDirectory(Paths.get(folderPathString));
                } catch (IOException ie) {
                    if (!new File(folderPathString).exists()) { // no need to worry otherwise
                        throw ie;
                    }
                }
            }
            for (String fileContent: fileContents) {
                String fileName = folderPathString + "/" + UUID.randomUUID() + "." + fileExtension;
                BufferedWriter bufferedFileWriter = new BufferedWriter(new FileWriter(fileName));
                bufferedFileWriter.write(fileContent);
                bufferedFileWriter.flush();
                documentIdentifier.add(fileName);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(documentIdentifier);
    }
}
