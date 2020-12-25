package net.explore.nosql;

import org.junit.jupiter.api.Test;

import javax.management.ValueExp;

import static org.junit.jupiter.api.Assertions.*;

class ValueTest {

    @Test
    void beginsWithInstruction() {
        assertTrue(Value.beginsWithInstruction("$NOW$"));
        assertTrue(Value.beginsWithInstruction("$NOW$something"));
        assertTrue(Value.beginsWithInstruction("$$NOW$"));
        assertFalse(Value.beginsWithInstruction("Something$NOW$"));
        assertFalse(Value.beginsWithInstruction("Something$NOW$Something"));
        assertFalse(Value.beginsWithInstruction("$Now"));
        assertFalse(Value.beginsWithInstruction("Something"));

    }

    @Test
    void hasOnlyInstruction() {
        assertTrue(Value.hasOnlyInstruction("$NOW$"));
        assertTrue(Value.hasOnlyInstruction("$$NOW$"));
        assertFalse(Value.hasOnlyInstruction("$NOW$something"));
        assertFalse(Value.hasOnlyInstruction("Something$NOW$"));
        assertFalse(Value.hasOnlyInstruction("Something$NOW$Something"));
        assertFalse(Value.hasOnlyInstruction("$Now"));
        assertFalse(Value.hasOnlyInstruction("Something"));
    }

    @Test
    void hasInstruction() {
        assertTrue(Value.hasInstruction("$NOW$"));
        assertTrue(Value.hasInstruction("$$NOW$"));
        assertTrue(Value.hasInstruction("$NOW$something"));
        assertTrue(Value.hasInstruction("Something$NOW$"));
        assertTrue(Value.hasInstruction("Something$NOW$Something"));
        assertFalse(Value.hasInstruction("$Now"));
        assertFalse(Value.hasInstruction("Something"));
    }

    @Test
    void getInstruction() {
        assertEquals("NOW", Value.getInstruction("$NOW$"));
        assertEquals("NOW", Value.getInstruction("$NOW$Something"));
        assertEquals("NOW", Value.getInstruction("Something$NOW$"));
        assertEquals("NOW", Value.getInstruction("Something$NOW$Something"));
        assertEquals("NOW", Value.getInstruction("Something, $NOW$"));
        assertEquals("NOW", Value.getInstruction("$NOW$, Something"));
        assertEquals("NOW", Value.getInstruction("$NOW$$"));
    }
}