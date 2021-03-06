package net.explore.nosql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NodeValue {
    @Value("${min-date-bound:0}")
    long minDateBound;

    @Value("${max-date-bound: 1609188965}")
    long maxDateBound;

    public boolean beginsWithInstruction(String theValue) {
        return theValue.matches("^\\$.+?\\$.*");
    }

    public boolean hasOnlyInstruction(String theValue) {
        return theValue.matches("^\\$.+?\\$$");
    }

    public boolean hasInstruction(String theValue) {
        return theValue.matches("^.*\\$.+?\\$.*");
    }

    public String getInstruction(String theValue) {
        int firstIndex = theValue.indexOf("$");
        return theValue.substring(firstIndex + 1, theValue.indexOf("$", firstIndex + 1));
    }

    /**
     * This method uses java.util.Formatter to format processed instruction. Refer to formatter syntax @ https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
     * @param instructionWithArgs map with instruction as key instruction arguments as value
     * @return processed String
     */
    public String processInstruction(HashMap<String, String[]> instructionWithArgs) {
        String processedInstruction = "";
        String instruction = instructionWithArgs.keySet().iterator().next().toUpperCase();
        String[] arguments = instructionWithArgs.get(instruction);

        switch (instruction) {
            case "NOW":
                processedInstruction = String.format(
                        (hasUsableValue(arguments) ? arguments[0] : "%1$tm-%1$td-%1$tYT%1$tT%1$tz"), new Date());
                break;
            case "ONE_OF":
                int randomIndex =
                        hasUsableValue(arguments) ? ThreadLocalRandom.current().nextInt(arguments.length) : 0;
                String pickedValue = hasUsableValue(arguments) ? arguments[ randomIndex]: "";
                if(hasInstruction(pickedValue)) {
                    processedInstruction = getLeafNodeValue(pickedValue);
                } else {
                    processedInstruction = pickedValue;
                }
                break;
            case "UUID":
                processedInstruction = UUID.randomUUID().toString();
                break;
            case "RANDOM_NUMBER":
                processedInstruction = String.format(
                        (hasUsableValue(arguments) ? arguments[0] : "%.0f"), ThreadLocalRandom.current().nextDouble(999999999));
                break;
            case "RANDOM_DATE_TIME":
                long minBound = minDateBound;
                long maxBound = (maxDateBound < 86400) ? (new Date().getTime())/1000 : maxDateBound;
                long randomEpocTime = ThreadLocalRandom.current().nextLong(minBound, maxBound);
                processedInstruction = String.format(
                        (hasUsableValue(arguments) ? arguments[0] : "%1$tFT%1$tT"), (randomEpocTime * 1000));
                break;
            case "BLANK":
                processedInstruction = "";
                break;
        }
        return processedInstruction.trim();
    }

    public HashMap<String, String[]> parseInstruction(String inputInstruction) {
        String[] arguments = null; //new String[]{""};
        String instruction = inputInstruction;
        if(hasInstruction(inputInstruction)) {
            int beginIndex = inputInstruction.indexOf("$") + 1;
            int endIndex = inputInstruction.indexOf("$", beginIndex);
            instruction = inputInstruction.substring(beginIndex, endIndex);
        }

        if(inputInstruction.contains("{") && inputInstruction.contains("}")) {
            String tempString = inputInstruction.substring(inputInstruction.indexOf("{") + 1, inputInstruction.lastIndexOf("}"));
            arguments = tempString.split(",");
        }

        HashMap<String, String[]> parsedInstruction = new HashMap<>();
        parsedInstruction.put(instruction,arguments);
        return parsedInstruction;
    }

    public String getLeafNodeValue(String nodeValue) {
        if(!hasInstruction(nodeValue)) {
            return nodeValue;
        } else {
            int instStartIndex = nodeValue.indexOf("$");
            int instEndIndex = nodeValue.indexOf("$", instStartIndex + 1);
            int argStartIndex = nodeValue.indexOf("{", instEndIndex + 1);
            int argEndIndex = (argStartIndex > 0) ? nodeValue.lastIndexOf("}") : -1;
            int instWithArgEndIndex = ((argEndIndex > 0) ? argEndIndex: instEndIndex) + 1;

            String instructionWithArgs = nodeValue.substring(instStartIndex, instWithArgEndIndex);
            String processedInstruction = processInstruction(parseInstruction(instructionWithArgs));

            String prefix = (instStartIndex > 0) ? nodeValue.substring(0, instStartIndex): "";
            String postfix = (instWithArgEndIndex < nodeValue.length()) ? nodeValue.substring(instWithArgEndIndex) : "";
            return getLeafNodeValue(prefix + processedInstruction + postfix);
        }
    }
    private boolean hasUsableValue(String[] arrayToCheck) {
        return arrayToCheck != null && arrayToCheck.length > 0 && arrayToCheck[0] != null && arrayToCheck[0].trim().length() > 0;
    }
}
