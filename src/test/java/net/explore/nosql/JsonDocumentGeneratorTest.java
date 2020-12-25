package net.explore.nosql;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonDocumentGeneratorTest {

    JsonDocumentGenerator jsonDataGenerator = new JsonDocumentGenerator();

    @Test
    public void processInstructionNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ssZ");
        String processedString = dateFormat.format(new Date());
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("NOW", new String[] {""});
        assertEquals(processedString, jsonDataGenerator.processInstruction(stringHashMap));
    }

    @Test
    public void processInstructionOneOfNoArgument() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {""});
        assertEquals("", jsonDataGenerator.processInstruction(stringHashMap));
    }

    @Test
    public void processInstructionOneOf() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {"First"});
        assertEquals("First", jsonDataGenerator.processInstruction(stringHashMap));
        stringHashMap.put("ONE_OF", new String[] {"First", "second"});
        String processedValue = jsonDataGenerator.processInstruction(stringHashMap);
        assertTrue("First".equalsIgnoreCase(processedValue) || "second".equalsIgnoreCase(processedValue));
    }

    @Test
    public void processInstructionOneOfWithInstruction() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {"$NOW$"});
        String processedValue = jsonDataGenerator.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{2}\\-\\d{2}\\-\\d{4}T\\d{2}:\\d{2}:\\d{2}\\-\\d{4}"));
        stringHashMap.put("ONE_OF", new String[] {"$UUID$"});
        processedValue = jsonDataGenerator.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("[a-z0-9]{8}\\-[a-z0-9]{4}\\-[a-z0-9]{4}\\-[a-z0-9]{4}\\-[a-z0-9]{12}"));
        stringHashMap.put("ONE_OF", new String[] {"$RANDOM_NUMBER$"});
        processedValue = jsonDataGenerator.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d+"));
        stringHashMap.put("ONE_OF", new String[] {"$BLANK$"});
        processedValue = jsonDataGenerator.processInstruction(stringHashMap);
        assertTrue(processedValue.matches(""));

    }
}
