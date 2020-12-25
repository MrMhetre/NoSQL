package net.explore.nosql;

import java.util.HashMap;
import java.util.regex.Pattern;

public class Value {
    public static boolean beginsWithInstruction(String theValue) {
        if(theValue.matches("^\\$.+?\\$.*")) {
            return true;
        }

        return false;
    }

    public static boolean hasOnlyInstruction(String theValue) {
        if(theValue.matches("^\\$.+?\\$$")) {
            return true;
        }

        return false;
    }

    public static boolean hasInstruction(String theValue) {
        if(theValue.matches("^.*\\$.+?\\$.*")) {
            return true;
        }

        return false;
    }

    public static String getInstruction(String theValue) {
        int firstIndex = theValue.indexOf("$");
        return theValue.substring(firstIndex + 1, theValue.indexOf("$", firstIndex + 1));
    }
}
