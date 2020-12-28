package net.explore.nosql;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NodeValue {
    public static boolean beginsWithInstruction(String theValue) {
        return theValue.matches("^\\$.+?\\$.*");
    }

    public static boolean hasOnlyInstruction(String theValue) {
        return theValue.matches("^\\$.+?\\$$");
    }

    public static boolean hasInstruction(String theValue) {
        return theValue.matches("^.*\\$.+?\\$.*");
    }

    public static String getInstruction(String theValue) {
        int firstIndex = theValue.indexOf("$");
        return theValue.substring(firstIndex + 1, theValue.indexOf("$", firstIndex + 1));
    }

    /**
     * This method uses java.util.Formatter to format processed instruction. Refer to formatter syntax @ https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
     * @param instructionWithArgs map with instruction as key instruction arguments as value
     * @return processed String
     */
    public static String processInstruction(HashMap<String, String[]> instructionWithArgs) {
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
                if(NodeValue.hasInstruction(pickedValue)) {
                    processedInstruction = processInstruction(parseInstruction(pickedValue));
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
            case "BLANK":
                processedInstruction = "";
                break;
        }
        return processedInstruction.trim();
    }

    public static HashMap<String, String[]> parseInstruction(String inputInstruction) {
        String[] arguments = null; //new String[]{""};
        String instruction = inputInstruction;
        if(NodeValue.hasInstruction(inputInstruction)) {
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

    public static String getLeafNodeValue(String nodeValue) {
        if(!NodeValue.hasInstruction(nodeValue)) {
            return nodeValue;
        } else {
            return processInstruction(parseInstruction(nodeValue));
        }
    }
    private static boolean hasUsableValue(String[] arrayToCheck) {
        return arrayToCheck != null && arrayToCheck.length > 0 && arrayToCheck[0] != null && arrayToCheck[0].trim().length() > 0;
    }
}
