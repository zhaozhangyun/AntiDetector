package com.kmdc.mdu.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static File saveFile(Context context, String content) {
        return saveFile(context, content.getBytes(StandardCharsets.UTF_8));
    }

    public static File saveFile(Context context, byte[] stream) {
        File localFile = null;
        FileOutputStream fos = null;
        String fileName = "deviceInfo.txt";

        try {
            localFile = new File(context.getFilesDir() + File.separator + fileName);
            fos = new FileOutputStream(localFile);
            fos.write(stream);
            fos.flush();
            return localFile;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return localFile;
    }

    public static boolean createOrExistsDir(File file) {
        if (file != null) {
            return file.exists() ? file.isDirectory() : file.mkdirs();
        }
        return false;
    }

    public static boolean createOrExistsDir(String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    public static boolean createOrExistsFile(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteFile(File file) {
        if (file != null) {
            file.delete();
        }
    }

    public static File getFileByPath(String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    public static boolean isFileExists(File file) {
        return file != null && (file.exists());
    }

    private static boolean isSpace(String s) {
        if (s == null) {
            return true;
        }
        int i = 0;
        int len = s.length();
        while (i < len) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
            ++i;
        }
        return true;
    }
}

