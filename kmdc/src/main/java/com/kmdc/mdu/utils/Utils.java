package com.kmdc.mdu.utils;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import zizzy.zhao.bridgex.l.L;

public class Utils {

    public static <T> Map<String, T> mergeParameters(Map<String, T> target,
                                                     Map<String, Object> source,
                                                     String parameterName) {
        if (target == null) {
            return (Map<String, T>) source;
        }
        if (source == null) {
            return target;
        }
        Map<String, T> mergedParameters = new HashMap<>(target);
        for (Map.Entry<String, Object> parameterSourceEntry : source.entrySet()) {
            T oldValue = mergedParameters.put(parameterSourceEntry.getKey(),
                    (T) parameterSourceEntry.getValue());
            if (oldValue != null) {
                L.logF("Key %s with value %s from %s parameter was replaced by value %s",
                        parameterSourceEntry.getKey(),
                        oldValue,
                        parameterName,
                        parameterSourceEntry.getValue());
            }
        }
        return mergedParameters;
    }

    public static String formatString(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    public static String getExtendedString(Map<String, ?> parameters) {
        return getExtendedString(parameters, false);
    }

    public synchronized static String getExtendedString(Map<String, ?> parameters, boolean sorted) {
        StringBuilder builder = new StringBuilder();
        if (parameters != null) {
            builder.append("\n")
                    .append("Parameters ---------------------------------------------------------");
            if (sorted) {
                SortedMap<String, ?> sortedParameters = new TreeMap<>(parameters);
                for (Map.Entry<String, ?> entry : sortedParameters.entrySet()) {
                    String key = entry.getKey();
                    builder.append(formatString("\n\t%-32s %s", key, entry.getValue()));
                }
            } else {
                for (Map.Entry<String, ?> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    builder.append(formatString("\n\t%-32s %s", key, entry.getValue()));
                }
            }
            builder.append("\n")
                    .append("--------------------------------------------------------------------");
        }
        return builder.toString();
    }

    public static CmdResult doCommands(String cmd) {
        CmdResult cr = new CmdResult();
        try {
            Process sh = Runtime.getRuntime().exec("sh");
            DataOutputStream dos = new DataOutputStream(sh.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            sh.waitFor();
            InputStream is = sh.getInputStream();
            cr.result = inputToString(is, "utf8").trim();
            cr.code = sh.exitValue();
            is.close();
        } catch (Throwable t) {
            L.e("Failed to exec commands: " + t);
        }
        return cr;
    }

    private static String inputToString(InputStream is, String charset2) throws IOException {
        byte[] bytesResult = readFully(is, Integer.MAX_VALUE, false);
        if (charset2 == null) {
            return new String(bytesResult);
        }
        return new String(bytesResult, charset2);
    }

    private static byte[] readFully(InputStream is, int length, boolean readAll)
            throws IOException {
        byte[] output = {};
        if (length == -1) length = Integer.MAX_VALUE;
        int pos = 0;
        while (pos < length) {
            int bytesToRead;
            if (pos >= output.length) { // Only expand when there's no room
                bytesToRead = Math.min(length - pos, output.length + 1024);
                if (output.length < pos + bytesToRead) {
                    output = Arrays.copyOf(output, pos + bytesToRead);
                }
            } else {
                bytesToRead = output.length - pos;
            }
            int cc = is.read(output, pos, bytesToRead);
            if (cc < 0) {
                if (readAll && length != Integer.MAX_VALUE) {
                    throw new EOFException("Detect premature EOF");
                } else {
                    if (output.length != pos) {
                        output = Arrays.copyOf(output, pos);
                    }
                    break;
                }
            }
            pos += cc;
        }
        return output;
    }

    public static class CmdResult {
        public int code;
        public String result;

        public String toString() {
            return "CmdResult{code=" + code + ", result='" + result + '\'' + '}';
        }
    }

    public static Map<String, Object> fetchFieldClass(String className)
            throws ClassNotFoundException {
        return fetchFieldClass(Class.forName(className));
    }

    public static Map<String, Object> fetchFieldClass(Class clazz) {
        Map<String, Object> result = new HashMap<>();

        try {
            Object obj = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i < fields.length; ++i) {
                fields[i].setAccessible(true);
                String fieldName = fields[i].getName();
                Class fieldType = fields[i].getType();

                try {
                    Field field = clazz.getDeclaredField(fields[i].getName());
                    field.setAccessible(true);
                    Object fieldVal = field.get(obj);
//                    L.v("fieldName: " + fieldName
//                            + ", fieldType: " + fieldType.getName()
//                            + ", fieldVal: " + fieldVal);
                    if (fieldVal != null) {
                        if (fieldType.getName().startsWith("[")) {
                            result.put(fieldName, Arrays.toString((Object[]) fieldVal));
                        } else {
                            result.put(fieldName, fieldVal);
                        }
                    }
                } catch (Throwable t) {
                    L.e("Failed to get declared field: " + t);
                }
            }
        } catch (Throwable t) {
            L.e("Failed to reflect field class: " + t);
        }

        return result;
    }
}
