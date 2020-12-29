package net.explore.nosql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class NodeValueTest {
    NodeValue nodeValue = new NodeValue();

    @Test
    public void beginsWithInstruction() {
        assertTrue(nodeValue.beginsWithInstruction("$NOW$"));
        assertTrue(nodeValue.beginsWithInstruction("$NOW$something"));
        assertTrue(nodeValue.beginsWithInstruction("$$NOW$"));
        assertFalse(nodeValue.beginsWithInstruction("Something$NOW$"));
        assertFalse(nodeValue.beginsWithInstruction("Something$NOW$Something"));
        assertFalse(nodeValue.beginsWithInstruction("$Now"));
        assertFalse(nodeValue.beginsWithInstruction("Something"));
    }

    @Test
    public void hasOnlyInstruction() {
        assertTrue(nodeValue.hasOnlyInstruction("$NOW$"));
        assertTrue(nodeValue.hasOnlyInstruction("$$NOW$"));
        assertFalse(nodeValue.hasOnlyInstruction("$NOW$something"));
        assertFalse(nodeValue.hasOnlyInstruction("Something$NOW$"));
        assertFalse(nodeValue.hasOnlyInstruction("Something$NOW$Something"));
        assertFalse(nodeValue.hasOnlyInstruction("$Now"));
        assertFalse(nodeValue.hasOnlyInstruction("Something"));
    }

    @Test
    public void hasInstruction() {
        assertTrue(nodeValue.hasInstruction("$NOW$"));
        assertTrue(nodeValue.hasInstruction("$$NOW$"));
        assertTrue(nodeValue.hasInstruction("$NOW$something"));
        assertTrue(nodeValue.hasInstruction("Something$NOW$"));
        assertTrue(nodeValue.hasInstruction("Something$NOW$Something"));
        assertFalse(nodeValue.hasInstruction("$Now"));
        assertFalse(nodeValue.hasInstruction("Something"));
    }

    @Test
    public void getInstruction() {
        assertEquals("NOW", nodeValue.getInstruction("$NOW$"));
        assertEquals("NOW", nodeValue.getInstruction("$NOW$Something"));
        assertEquals("NOW", nodeValue.getInstruction("Something$NOW$"));
        assertEquals("NOW", nodeValue.getInstruction("Something$NOW$Something"));
        assertEquals("NOW", nodeValue.getInstruction("Something, $NOW$"));
        assertEquals("NOW", nodeValue.getInstruction("$NOW$, Something"));
        assertEquals("NOW", nodeValue.getInstruction("$NOW$$"));
    }

    @Test
    public void processInstructionNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ssZ");
        String processedString = dateFormat.format(new Date());
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("NOW", new String[] {""});
        assertEquals(processedString, nodeValue.processInstruction(stringHashMap));
    }

    @Test
    public void processInstructionOneOfNoArgument() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {""});
        assertEquals("", nodeValue.processInstruction(stringHashMap));
    }

    @Test
    public void processInstructionOneOf() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {"First"});
        assertEquals("First", nodeValue.processInstruction(stringHashMap));
        stringHashMap.put("ONE_OF", new String[] {"First", "second"});
        String processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue("First".equalsIgnoreCase(processedValue) || "second".equalsIgnoreCase(processedValue));
    }

    @Test
    public void processInstructionOneOfWithInstruction() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("ONE_OF", new String[] {"$NOW$"});
        String processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{2}-\\d{2}-\\d{4}T\\d{2}:\\d{2}:\\d{2}-\\d{4}"));
        stringHashMap.put("ONE_OF", new String[] {"$UUID$"});
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}"));
        stringHashMap.put("ONE_OF", new String[] {"$RANDOM_NUMBER$"});
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d+"));
        stringHashMap.put("ONE_OF", new String[] {"$BLANK$"});
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches(""));
    }

    @Test
    public void processInstructionNowWithFormat() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("NOW", new String[] {""}); //Should use default
        String processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{2}-\\d{2}-\\d{4}T\\d{2}:\\d{2}:\\d{2}-\\d{4}"));

        stringHashMap.put("NOW", new String[] {"%tF"}); //Should use ISO 8601 date format
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{4}-\\d{2}-\\d{2}"));

        stringHashMap.put("NOW", new String[] {"%1$td %1$tb, %1$tY %1$tT"}); //Should use this
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{2}\\s[A-Za-z]{3},\\s\\d{4}\\s\\d{2}:\\d{2}:\\d{2}"));

        stringHashMap.put("NOW", new String[] {"%1$ts"}); //epoc seconds
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d+"));
    }

    @Test
    public void processInstructionRandomNumberWithFormat() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("RANDOM_NUMBER", new String[] {""}); //Should use default
        String processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{9}"));

        stringHashMap.put("RANDOM_NUMBER", new String[] {"%.3f"}); //with fraction
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{9}\\.\\d{3}"));

        stringHashMap.put("RANDOM_NUMBER", new String[] {"%012.0f"}); // padded with 0s, no fraction
        processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{12}"));
    }

    @Test
    public void processInstructionRandomDateTime() {
        HashMap<String, String[]> stringHashMap = new HashMap<>();
        stringHashMap.put("RANDOM_DATE_TIME", new String[] {""}); //Should use default
        String processedValue = nodeValue.processInstruction(stringHashMap);
        assertTrue(processedValue.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void getLeafNodeValueWithOnlyInstruction() {
        String processedNodeValue = nodeValue.getLeafNodeValue("$ONE_OF${A$NOW${%ts}, B$NOW${%ts}}");
        assertTrue(processedNodeValue.matches("\\w\\d+"));

        processedNodeValue = nodeValue.getLeafNodeValue("$BLANK$");
        assertTrue(processedNodeValue.matches(""));
    }

    @Test
    public void getLeafNodeValueWithBeginningInstruction() {
        String processedNodeValue = nodeValue.getLeafNodeValue("$NOW${%ts}.pdf");
        assertTrue(processedNodeValue.matches("\\d+\\.pdf"));
    }

    @Test
    public void getLeafNodeValueWithInstructionInMiddle() {
        String processedNodeValue = nodeValue.getLeafNodeValue("Today is $NOW${%tD}.");
        assertTrue(processedNodeValue.matches("Today is \\d{2}/\\d{2}/\\d{2}\\."));

        processedNodeValue = nodeValue.getLeafNodeValue("ABC$BLANK$DEF");
        assertTrue(processedNodeValue.matches("ABCDEF"));
    }

    @Test
    public void getLeafNodeValueWithEndingInstruction() {
        String processedNodeValue = nodeValue.getLeafNodeValue("Time is $NOW$");
        assertTrue(processedNodeValue.matches("Time is \\d{2}-\\d{2}-\\d{4}T\\d{2}:\\d{2}:\\d{2}-\\d{4}"));

        processedNodeValue = nodeValue.getLeafNodeValue("A$RANDOM_NUMBER$");
        assertTrue(processedNodeValue.matches("A\\d+"));

        processedNodeValue = nodeValue.getLeafNodeValue("This is $ONE_OF${Yash, Hush}");
        assertTrue(processedNodeValue.matches("This is \\w{4}"));
    }

    @Test
    public void getLeafNodeValueWithEmptyString() {
        String processedNodeValue = nodeValue.getLeafNodeValue("");
        assertEquals("", processedNodeValue);
    }
}