package net.explore.nosql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@ConditionalOnProperty(name="document-writer", havingValue = "zip")
public class ZipFileWriter implements DocumentWriter{
    Logger logger = LoggerFactory.getLogger(ZipFileWriter.class);

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

            String zipFileName = folderPathString + "/" + UUID.randomUUID() + ".zip";
            logger.info("Generating zip file {} for the batch", zipFileName);
            try (
                FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
                for (String fileContent: fileContents) {
                    String fileName = UUID.randomUUID() + "." + fileExtension;
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(fileContent.getBytes());
                    zipOutputStream.flush();
                    logger.debug("Writing {} to {}", fileName, zipFileName);
                }
            }
            logger.info("Completed Generating {}", zipFileName);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(documentIdentifier);
    }
}
