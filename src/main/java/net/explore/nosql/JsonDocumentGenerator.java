package net.explore.nosql;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name="document-format", havingValue = "JSON")
public class JsonDocumentGenerator implements DocumentGenerator {


    @Override
    @Async
    public CompletableFuture<ArrayList<String>> generateDocuments(File templateFile, int numberOfDocuments) {
        ArrayList<String> documentsList = new ArrayList<>(numberOfDocuments);

        try {
            String contentString = Files.readString(templateFile.toPath());
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> jsonObjectsMap = jsonParser.parseMap(contentString);

            for (int i=0; i< numberOfDocuments; i++) {
                LinkedTreeMap<String, Object> processLinkedTreeMap = new LinkedTreeMap<>();
                for (Map.Entry<String, Object> jsonEntry : jsonObjectsMap.entrySet()) {
                    LinkedTreeMap<String, Object> inMap = (LinkedTreeMap<String, Object>) jsonEntry.getValue();
                    LinkedTreeMap<String, Object> outMap = processJsonMap(inMap);
                    processLinkedTreeMap.put(jsonEntry.getKey(), outMap);
                }

                String json = convertToJson(processLinkedTreeMap);
                documentsList.add(json);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(documentsList);
    }

    private String convertToJson(LinkedTreeMap<String, Object> processLinkedTreeMap) {
        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setPrettyPrinting();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(processLinkedTreeMap);

        return json;
    }

    private LinkedTreeMap<String, Object> processJsonMap(Map<String, Object> linkedTreeMap) {
        LinkedTreeMap<String, Object> postProcessedMap = new LinkedTreeMap<>();
        for(Map.Entry<String, Object> jsonEntry : linkedTreeMap.entrySet()) {
            String key = jsonEntry.getKey();
            Object valueObject = jsonEntry.getValue();
            if (valueObject instanceof LinkedTreeMap) {
                LinkedTreeMap<String, Object> treeMap = processJsonMap((LinkedTreeMap<String, Object>) valueObject);
                postProcessedMap.put(key, treeMap);
            } else {
                // leaf node
                String value = valueObject.toString();
                value = getLeafNodeValue(value);                
                postProcessedMap.put(key, value);
                // System.out.println(" >> " + value);
            }
        }
        return postProcessedMap;
    }

    private HashMap<String, String[]> parseInstruction(String inputInstruction) {
        String[] arguments = new String[]{""};
        String instruction = inputInstruction;
        if(Value.hasInstruction(inputInstruction)) {
            int beginIndex = inputInstruction.indexOf("$") + 1;
            int endIndex = inputInstruction.indexOf("$", beginIndex);
            instruction = inputInstruction.substring(beginIndex, endIndex);
        }

        if(inputInstruction.contains("{") && inputInstruction.contains("}")) {
            String tempString = inputInstruction.substring(inputInstruction.indexOf("{") + 1, inputInstruction.indexOf("}"));
            arguments = tempString.split(",");
        }

        HashMap<String, String[]> parsedInstruction = new HashMap<>();
        parsedInstruction.put(instruction,arguments);
        return parsedInstruction;
    }

    public String processInstruction(HashMap<String, String[]> instructionWithArgs) {
        String processedInstruction = "";
        String instruction = instructionWithArgs.keySet().iterator().next().toString().toUpperCase();
        String[] arguments = instructionWithArgs.get(instruction);

        switch (instruction) {
            case "NOW":
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ssZ");
                processedInstruction = dateFormat.format(new Date());
                break;
            case "ONE_OF":
                int randomIndex =
                    arguments.length > 1 ? (new Random()).nextInt(arguments.length) : 0;
                String pickedValue = arguments[ randomIndex];
                if(Value.hasInstruction(pickedValue)) {
                    processedInstruction = processInstruction(parseInstruction(pickedValue));
                } else {
                    processedInstruction = pickedValue;
                }
                break;
            case "UUID":
                processedInstruction = UUID.randomUUID().toString();
                break;
            case "RANDOM_NUMBER":
                processedInstruction = Integer.toString(new Random().nextInt(1000000));
                break;
            case "BLANK":
                processedInstruction = "";
                break;
        }
        return processedInstruction.trim();
    }

    public String getLeafNodeValue(String nodeValue) {
        if(!Value.hasInstruction(nodeValue)) {
            return nodeValue;
        } else {
            return processInstruction(parseInstruction(nodeValue));
        }
    }

}