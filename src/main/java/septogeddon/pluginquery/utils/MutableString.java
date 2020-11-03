package septogeddon.pluginquery.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;

public class MutableString {
    private static Field valueField, hashField;
    static {
        try {
            valueField = String.class.getDeclaredField("value");
            hashField = String.class.getDeclaredField("hash");
            valueField.setAccessible(true);
            hashField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.set(valueField, valueField.getModifiers() & ~Modifier.FINAL);
            modifiersField.set(hashField, hashField.getModifiers() & ~Modifier.FINAL);
        } catch (Throwable t) {
        }
    }
    private String instance;
    public MutableString(String instance) {
        this.instance = instance;
    }

    public char[] getRawCharArray() {
        try {
            char[] source = (char[]) valueField.get(instance);
            return source;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue() {
        return instance;
    }

    public MutableString setValue(String newValue) {
        try {
            char[] source = (char[]) valueField.get(newValue);
            setValue(source);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public MutableString setValue(char[] newValue) {
        resetCachedHash();
        try {
            valueField.set(instance, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public int length() {
        return instance.length();
    }

    public MutableString substring(int startIndex) {
        return substring(startIndex, length());
    }

    public MutableString substring(int startIndex, int endIndex) {
        char[] newValue = Arrays.copyOfRange(getRawCharArray(), startIndex, endIndex);
        setValue(newValue);
        return this;
    }

    public MutableString toLowerCase() {
        setValue(getValue().toLowerCase());
        return this;
    }

    public MutableString toUpperCase() {
        setValue(getValue().toUpperCase());
        return this;
    }

    public MutableString toLowerCase(Locale locale) {
        setValue(getValue().toLowerCase(locale));
        return this;
    }

    public MutableString toUpperCase(Locale locale) {
        setValue(getValue().toUpperCase(locale));
        return this;
    }

    public MutableString concat(String newValue) {
        try {
            char[] newCharArray = Arrays.copyOf(getRawCharArray(), instance.length() + newValue.length());
            System.arraycopy(valueField.get(newValue), 0, newCharArray, instance.length(), newValue.length());
            setValue(newCharArray);
        } catch (IllegalAccessException e) {
        }
        return this;
    }

    private void resetCachedHash() {
        try {
            hashField.setInt(instance, 0);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
