package com.kmdc.mdu.utils
        ;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Crc32Util {

    public static String crc(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            return getApkFileSFCrc32(packageInfo.applicationInfo.sourceDir);
        } catch (NameNotFoundException e) {
        }
        return "null";
    }

    public static String md5(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            return getApkMd5(packageInfo.applicationInfo.sourceDir);
        } catch (NameNotFoundException e) {
        }
        return "null";
    }

    public static String getApkMd5(String filePath) {
        long md5 = 0xffffffff;
        String value = Long.toHexString(md5);
        FileInputStream inputStream = null;
        try {
            File file = new File(filePath);
            inputStream = new FileInputStream(file);
            MappedByteBuffer byteBuffer = inputStream.getChannel().map(
                    FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest digestMd5 = MessageDigest.getInstance("MD5");
            digestMd5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, digestMd5.digest());
            value = bi.toString(16);
        } catch (Exception ignore) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        return value;
    }

    public static String getApkFileSFCrc32(String ApkFilePath) {
        long crc = 0xffffffff;

        try {
            File f = new File(ApkFilePath);
            ZipFile z = new ZipFile(f);
            Enumeration<? extends ZipEntry> zList = z.entries();
            ZipEntry ze = null;
            while (zList.hasMoreElements()) {
                ze = (ZipEntry) zList.nextElement();
                if (ze.isDirectory()
                        || ze.getName().indexOf("META-INF") == -1
                        || ze.getName().indexOf(".SF") == -1) {
                    continue;
                } else {
                    crc = ze.getCrc();
                    break;
                }
            }
            z.close();
        } catch (Exception e) {
        }
        return Long.toHexString(crc);
    }
}
