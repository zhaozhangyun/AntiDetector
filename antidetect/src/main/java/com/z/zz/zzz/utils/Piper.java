package com.z.zz.zzz.utils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zhangyun.zhao on 2017/10/26.
 */

public class Piper implements Runnable {

    private InputStream input;
    private OutputStream output;

    private Piper(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    public static InputStream pipe(Process... proc) throws InterruptedException {
        // Start Piper between all processes
        Process p1;
        Process p2;
        InputStream is;
        OutputStream os;

        for (int i = 0; i < proc.length; i++) {
            p1 = proc[i];
            is = p1.getInputStream();

            // If there's one more process
            if (i + 1 < proc.length) {
                p2 = proc[i + 1];
                os = p2.getOutputStream();
                // Start piper
                new Thread(new Piper(is, os)).start();
            }
        }
        Process last = proc[proc.length - 1];
        // Wait for last process in chain; may throw InterruptedException
        last.waitFor();
        // Return its InputStream
        return last.getInputStream();
    }

    public static void pipeNoWait(Process... proc) throws InterruptedException {
        // Start Piper between all processes
        Process p1;
        Process p2;
        for (int i = 0; i < proc.length; i++) {
            p1 = proc[i];
            // If there's one more process
            if (i + 1 < proc.length) {
                p2 = proc[i + 1];
                // Start piper
                new Thread(new Piper(p1.getInputStream(), p2.getOutputStream())).start();
            }
        }
        // Process last = proc[proc.length - 1];
        // Wait for last process in chain; may throw InterruptedException
        // last.waitFor();
    }

    @Override
    public void run() {
        try {
            // Create 512 <a class="zem_slink"
            // href="http://en.wikipedia.org/wiki/Byte" title="Byte"
            // rel="wikipedia" target="_blank">bytes</a> buffer
            byte[] b = new byte[2048];
            int read = 1;
            // As long as data is read; -1 means <a class="zem_slink"
            // href="http://en.wikipedia.org/wiki/End-of-file"
            // title="End-of-file" rel="wikipedia" target="_blank">EOF</a>
            while (read > -1) {
                // <a class="zem_slink"
                // href="http://en.wikipedia.org/wiki/Read_%28system_call%29"
                // title="Read (system call)" rel="wikipedia"
                // target="_blank">Read</a> bytes into buffer
                read = input.read(b, 0, b.length);
                // System.out.println("-------------------------------------- read >>> \n"
                // + new String(b));
                if (read > -1) {
                    // Write bytes to output
                    output.write(b, 0, read);
                    output.flush();
                }
            }
        } catch (Exception e) {
            // Something happened while reading or writing streams; pipe is
            // broken
            throw new RuntimeException("Broken pipe", e);
        } finally {
            // Don't close here
//            if (input != null) {
//                try {
//                    input.close();
//                } catch (IOException e) {
//                }
//            }
//
//            if (output != null) {
//                try {
//                    output.close();
//                } catch (IOException e) {
//                }
//            }
        }
    }
}
