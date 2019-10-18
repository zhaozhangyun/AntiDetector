package com.z.zz.zzz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is intended to be used with the SystemCommandExecutor class to let
 * users execute system commands from Java applications.
 * <p>
 * This class is based on work that was shared in a JavaWorld article named
 * "When System.exec() won't". That article is available at this url:
 * <p>
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * <p>
 * Documentation for this class is available at this URL:
 * <p>
 * http://devdaily.com/java/java-processbuilder-process-system-exec
 * <p>
 * <p>
 * Copyright 2010 alvin j. alexander, devdaily.com.
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Please ee the following page for the LGPL license:
 * http://www.gnu.org/licenses/lgpl.txt
 */
class ThreadedStreamHandler extends Thread {
    InputStream inputStream;
    OutputStream outputStream;
    StringBuilder outputBuffer = new StringBuilder();
    List<String> outputBufferList = new ArrayList<>();

    /**
     * A simple constructor for when the sudo command is not necessary. This
     * constructor will just run the command you provide, without running sudo
     * before the command, and without expecting a password.
     *
     * @param inputStream
     */
    ThreadedStreamHandler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Use this constructor when you want to invoke the 'sudo' command. The
     * outputStream must not be null. If it is, you'll regret it. :)
     * <p>
     * TODO this currently hangs if the admin password given for the sudo
     * command is wrong.
     *
     * @param inputStream
     * @param outputStream
     */
    ThreadedStreamHandler(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outputBuffer.append(line + "\n");
                outputBufferList.add(line);
            }
        } catch (IOException ioe) {
            // TODO handle this better
            ioe.printStackTrace();
        } catch (Throwable t) {
            // TODO handle this better
            t.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // ignore this one
                }
            }
        }
    }

    private void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    StringBuilder getOutputBuffer() {
        return outputBuffer;
    }

    List<String> getOutputBufferList() {
        return outputBufferList;
    }
}
