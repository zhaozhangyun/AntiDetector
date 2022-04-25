package com.kmdc.mdu.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import zizzy.zhao.bridgex.l.L;

public class GZIPUtils {

    private static final int BUFFER_SIZE = 512;

    public static byte[] compress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compress(bais, baos);
        byte[] output = baos.toByteArray();
        bais.close();
        return output;
    }

    public static byte[] uncompress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        uncompress(bais, baos);
        data = baos.toByteArray();
        baos.flush();
        baos.close();
        return data;
    }

    /**
     * gzip压缩
     *
     * @param srcFile 源文件(普通文件)
     */
    public static File compress(File srcFile) {
        String srcFileName = srcFile.getAbsolutePath();
        String destFileName = srcFileName.replaceAll("\\.log", ".zip");
        File destFile = new File(destFileName);
        return compress(srcFile, destFile);
    }

    /**
     * gzip压缩
     *
     * @param srcFile  源文件(普通文件)
     * @param destFile 目标文件(压缩文件)
     */
    public static File compress(File srcFile, File destFile) {
        L.d("srcFile=" + srcFile.getAbsolutePath()
                + ", destFile=" + destFile.getAbsolutePath());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(srcFile);
            compress(fis, new FileOutputStream(destFile));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuitely(fis);
        }
        return destFile;
    }

    public static void compress(InputStream is, OutputStream os) throws Exception {
        GZIPOutputStream gos = null;
        try {
            gos = new GZIPOutputStream(os);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                gos.write(buffer, 0, len);
            }
            gos.flush();
        } finally {
            closeQuitely(gos);
        }
    }

    /**
     * 解压缩
     *
     * @param srcFile  源文件(压缩文件)
     * @param destFile 目标文件(普通文件)
     */
    public static File uncompress(File srcFile, File destFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFile);
            uncompress(new FileInputStream(srcFile), fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            closeQuitely(fos);
        }
        return destFile;
    }

    public static void uncompress(InputStream is, OutputStream os) throws Exception {
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(is);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = 0;
            while ((len = gis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            closeQuitely(gis);
        }
    }

    /**
     * 关闭流
     *
     * @param stream
     */
    private static void closeQuitely(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    public static void main(String[] args) throws IOException {
//        String str = "看甲方时点击翻身肯\n";  //内容大小控制在240byte， >240 进行压缩·否则不压··
//        System.out.println("原文大小：" + str.getBytes().length + " \n压缩前：" + str);
//
//        String compress = GZIP.compress(str);
//        System.out.println("解压大小：" + compress.getBytes().length + " \n压缩后：" + compress);
//
//        String uncompress = GZIP.unCompress(compress);
//        System.out.println("解压大小：" + uncompress.getBytes().length + " \n解压缩：" + uncompress);
//    }
}
