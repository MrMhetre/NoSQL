package net.explore.nosql;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class NodeValueTest {

    @Test
    public void beginsWithInstruction() {
        assertTrue(NodeValue.beginsWithInstruction("$NOW$"));
        assertTrue(NodeValue.beginsWithInstruction("$NOW$something"));
        assertTrue(NodeValue.beginsWithInstruction("$$NOW$"));
        assertFalse(NodeValue.beginsWithInstruction("Something$NOW$"));
        assertFalse(NodeValue.beginsWithInstruction("Something$NOW$Something"));
        assertFalse(NodeValue.beginsWithInstruction("$Now"));
        assertFalse(NodeValue.beginsWithInstruction("Something"));
    }

    @Test
    public void hasOnlyInstruction() {
        assertTrue(NodeValue.hasOnlyInstruction("$NOW$"));
        assertTrue(NodeValue.hasOnlyInstruction("$$NOW$"));
        assertFalse(NodeValue.hasOnlyInstruction("$NOW$something"));
        assertFalse(NodeValue.hasOnlyInstruction("Something$NOW$"));
        assertFalse(NodeValue.hasOnlyInstruction("Something$NOW$Something"));
        assertFalse(NodeValue.hasOnlyInstruction("$Now"));
        assertFalse(NodeValue.hasOnlyInstruction("Something"));
    }

    @Test
    public void hasInstruction() {
        assertTrue(NodeValue.hasInstruction("$NOW$"));
        assertTrue(NodeValue.hasInstruction("$$NOW$"));
        assertTrue(NodeValue.hasInstruction("$NOW$something"));
        assertTrue(NodeValue.hasInstruction("Something$NOW$"));
        assertTrue(NodeValue.hasInstruction("Something$NOW$Something"));
        assertFalse(NodeValue.hasInstruction("$Now"));
        assertFalse(NodeValue.hasInstruction("Something"));
    }

    @Test
    public void getInstruction() {
        assertEquals("NOW", NodeValue.getInstruction("$NOW$"));
        assertEquals("NOW", NodeValue.getInstruction("$NOW$Something"));
        assertEquals("NOW", NodeValue.getInstruction("Something$NOW$"));
        assertEquals("NOW", NodeValue.getInstruction("Something$NOW$Something"));
        assertEquals("NOW", NodeValue.getInstruction("Something, $NOW$"));
        assertEquals("NOW", NodeValue.getInstruction("$NOW$, Something"));
        assertEquals("NOW", NodeValue.getInstruction("$NOW$$"));
    }

    @Test
    public void processInstructionNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ssZ");
        String processedString = dateFormat.format(new Date());
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("NOW", new String[] {""});
        assertEquals(processedString, NodeValue.processInstruction(stringHashMap));
    }

    @Test
    public void processInstructionOneOfNoArgument() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {""});
        assertEquals("", NodeValue.processInstruction(stringHashMap));
    }

    @Test
    public void processInstructionOneOf() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {"First"});
        assertEquals("First", NodeValue.processInstruction(stringHashMap));
        stringHashMap.put("ONE_OF", new String[] {"First", "second"});
        String processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue("First".equalsIgnoreCase(processedValue) || "second".equalsIgnoreCase(processedValue));
    }

    @Test
    public void processInstructionOneOfWithInstruction() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {"$NOW$"});
        String processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{2}-\\d{2}-\\d{4}T\\d{2}:\\d{2}:\\d{2}-\\d{4}"));
        stringHashMap.put("ONE_OF", new String[] {"$UUID$"});
        processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}"));
        stringHashMap.put("ONE_OF", new String[] {"$RANDOM_NUMBER$"});
        processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d+"));
        stringHashMap.put("ONE_OF", new String[] {"$BLANK$"});
        processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches(""));
    }

    @Test
    public void processInstructionNowWithFormat() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("NOW", new String[] {""}); //Should use default
        String processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{2}-\\d{2}-\\d{4}T\\d{2}:\\d{2}:\\d{2}-\\d{4}"));

        stringHashMap.put("NOW", new String[] {"%tF"}); //Should use ISO 8601 date format
        processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{4}-\\d{2}-\\d{2}"));

        stringHashMap.put("NOW", new String[] {"%1$td %1$tb, %1$tY %1$tT"}); //Should use this
        processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{2}\\s[A-Za-z]{3},\\s\\d{4}\\s\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void processInstructionRandomNumberWithFormat() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("RANDOM_NUMBER", new String[] {""}); //Should use default
        String processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{9}"));

        stringHashMap.put("RANDOM_NUMBER", new String[] {"%.3f"}); //with fraction
        processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{9}\\.\\d{3}"));

        stringHashMap.put("RANDOM_NUMBER", new String[] {"%012.0f"}); // padded with 0s, no fraction
        processedValue = NodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{12}"));
    }
}