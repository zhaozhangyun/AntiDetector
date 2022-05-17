package com.z.zz.zzz.antidetector;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class StreamUtils {
    private static String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36";

    public static void arrayList2File(ArrayList arg6, String destPath) {
        FileWriter fw2;
        try {
            File file = new File(destPath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            fw2 = new FileWriter(file);
            Iterator it = arg6.iterator();
            while (true) {
                if (!it.hasNext()) {
                    try {
                        fw2.flush();
                        fw2.close();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                Object v5 = it.next();
                fw2.write(v5 + "\n");
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        } catch (Throwable th) {
            throw th;
        }
    }

    public static void bytesToFile(byte[] datas, File destFile) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
        try {
            bos.write(datas, 0, datas.length);
            bos.flush();
        } finally {
            closeStream(new Closeable[]{null, bos});
        }
    }

    public static void closeStream(Closeable[] is) {
        if (is != null) {
            for (int i = 0; i < is.length; ++i) {
                Closeable closeable = is[i];
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static ArrayList file2ArrayList(String filePath) {
        ArrayList list = new ArrayList();
        BufferedReader br2 = null;
        try {
            br2 = new BufferedReader(new FileReader(filePath));
            while (true) {
                String readLine = br2.readLine();
                if (readLine == null) {
                    br2.close();
                    return list;
                }
                list.add(readLine);
            }
        } catch (Exception e2) {
            if (br2 != null) {
                try {
                    br2.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                return list;
            }
            return list;
        }
    }

    public static String file2String(String path) {
        String text = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(path));
            while (true) {
                String readLine = reader2.readLine();
                if (readLine == null) {
                    break;
                }
                text = text + readLine;
            }
            reader2.close();
            return text;
        } catch (IOException e2) {
            e2.printStackTrace();
            return text;
        }
    }

    public static String getFromAssets(String fileName, Context context) {
        String v4 = "";
        try {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                    context.getResources().getAssets().open(fileName)));
            while (true) {
                String readLine = bufReader.readLine();
                if (readLine == null) {
                    break;
                }
                v4 = readLine + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v4;
    }

    public static byte[] inputToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream arrayOutputStream2 = new ByteArrayOutputStream();
        byte[] buff = new byte[0x400];
        while (true) {
            int read = is.read(buff);
            if (read == -1) {
                byte[] result = arrayOutputStream2.toByteArray();
                try {
                    arrayOutputStream2.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
            arrayOutputStream2.write(buff, 0, read);
        }
    }

    public static void inputToFile(InputStream is, File destFile) throws IOException {
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(destFile));
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buff = new byte[0x800];
            while (true) {
                int read = bis.read(buff);
                if (read == -1) {
                    break;
                }
                bos.write(buff, 0, read);
            }
            bos.flush();
        } finally {
            closeStream(new Closeable[]{is, bos});
        }
    }

    public static String inputToString(InputStream is) throws IOException {
        return inputToString(is, null);
    }

    public static String inputToString(InputStream is, String charset) throws IOException {
        byte[] bytesResult = inputToBytes(is);
        return charset == null ? new String(bytesResult) : new String(bytesResult, charset);
    }

    private static String inputToString(InputStream is, boolean gzip, String charset) throws IOException {
        String v5_1;
        if (!gzip) {
            return inputToString(is, charset);
        }
        Closeable gzipInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(inputToBytes(is)));
            v5_1 = inputToString((InputStream) gzipInputStream, charset);
        } finally {
            closeStream(new Closeable[]{gzipInputStream, null});
        }
        return v5_1;
    }

    public static void stringToFile(String str, File file) throws IOException {
        Closeable is = null;
        try {
            is = new ByteArrayInputStream(str.getBytes());
            inputToFile((InputStream) is, file);
        } finally {
            closeStream(new Closeable[]{is, null});
        }
    }

    public static String urlToString(String urlStr, String charset) throws IOException {
        String v10;
        InputStream is = null;
        try {
            HttpURLConnection conn2 = (HttpURLConnection) new URL(urlStr).openConnection();
            conn2.setRequestProperty("User-Agent", userAgent);
            conn2.setReadTimeout(10000);
            if ((conn2 instanceof HttpsURLConnection)) {
                ((HttpsURLConnection) conn2).setHostnameVerifier(new HostnameVerifier() {
                    @Override  // javax.net.ssl.HostnameVerifier
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            }
            if (conn2.getResponseCode() == 302) {
                String location = conn2.getHeaderField("Location");
                conn2.disconnect();
                HttpURLConnection v6_2 = (HttpURLConnection) new URL(location).openConnection();
                v6_2.setConnectTimeout(15000);
                v6_2.setReadTimeout(15000);
            }
            is = conn2.getInputStream();
            boolean gzip = false;
            String ce = conn2.getHeaderField("Content-Encoding");
            if (ce != null && (ce.equalsIgnoreCase("gzip"))) {
                gzip = true;
            }
            v10 = inputToString(is, gzip, charset);
        } finally {
            closeStream(new Closeable[]{is, null});
        }
        return v10;
    }

    public static void writeEndLine(String url, String text) {
        try {
            FileReader fr = new FileReader(url);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            while (true) {
                String readLine = br.readLine();
                if (readLine == null) {
                    break;
                }
                sb.append(readLine);
                sb.append("\n");
            }
            System.out.println(sb.substring(0, sb.substring(0, sb.length() - 1).lastIndexOf("\n"))
                    + "\n" + text);
            fr.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFileToOs(OutputStream os, File file) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buff = new byte[0x800];
            while (true) {
                int read = bis.read(buff);
                if (read == -1) {
                    break;
                }
                os.write(buff, 0, read);
                os.flush();
            }
        } finally {
            closeStream(new Closeable[]{bis, null});
        }
    }

    public static void zipEntryToFile(ZipFile zipFile, File outDirFile) throws IOException {
        if (!outDirFile.exists()) {
            outDirFile.mkdirs();
        }
        Enumeration enumeration = zipFile.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
            File zipEntryFile = new File(outDirFile, zipEntry.getName());
            if (!zipEntry.isDirectory()) {
                BufferedInputStream v3 = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                inputToFile(v3, zipEntryFile);
                closeStream(new Closeable[]{v3, null});
            } else if (!zipEntryFile.exists()) {
                zipEntryFile.mkdirs();
            }
        }
        zipFile.close();
    }
}

