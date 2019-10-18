package com.z.zz.zzz;

import android.os.Debug;

import com.z.zz.zzz.utils.L;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to determine functionality specific to the Android debuggers
 *
 * @author tstrazzere
 */
class Debugger {

    private static String tracerpid = "TracerPid";

    /**
     * Believe it or not, there are packers that use this...
     */
    static boolean isBeingDebugged() {
        return Debug.isDebuggerConnected();
    }

    /**
     * This is used by Alibaba to detect someone ptracing the application.
     * <p>
     * Easy to circumvent, the usage ITW was a native thread constantly doing this every three seconds - and would cause
     * the application to crash if it was detected.
     */
    static boolean hasTracerPid() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/self/status")), 1000);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.length() > tracerpid.length()) {
                    if (line.substring(0, tracerpid.length()).equalsIgnoreCase(tracerpid)) {
                        if (Integer.decode(line.substring(tracerpid.length() + 1).trim()) > 0) {
                            return true;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            L.e(AntiDetector.TAG, "hasTracerPid error: ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    /**
     * This was reversed from a sample someone was submitting to sandboxes for a thesis, can't find paper anymore
     */
    static boolean hasAdbInEmulator() {
        boolean adbInEmulator = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/net/tcp")), 1000);
            String line;
            // Skip column names
            reader.readLine();

            List<tcp> tcpList = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                tcpList.add(tcp.create(line.split("\\W+")));
            }

            // Adb is always bounce to 0.0.0.0 - though the port can change
            // real devices should be != 127.0.0.1
            int adbPort = -1;
            for (tcp tcpItem : tcpList) {
                if (tcpItem.localIp == 0) {
                    adbPort = tcpItem.localPort;
                    break;
                }
            }

            if (adbPort != -1) {
                for (tcp tcpItem : tcpList) {
                    if ((tcpItem.localIp != 0) && (tcpItem.localPort == adbPort)) {
                        adbInEmulator = true;
                    }
                }
            }
        } catch (Exception e) {
            L.e(AntiDetector.TAG, "hasAdbInEmulator error: ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return adbInEmulator;
    }

    static class tcp {

        int id;
        long localIp;
        int localPort;
        int remoteIp;
        int remotePort;

        tcp(String id, String localIp, String localPort, String remoteIp, String remotePort, String state,
            String tx_queue, String rx_queue, String tr, String tm_when, String retrnsmt, String uid,
            String timeout, String inode) {
            this.id = Integer.parseInt(id, 16);
            this.localIp = Long.parseLong(localIp, 16);
            this.localPort = Integer.parseInt(localPort, 16);
        }

        static tcp create(String[] params) {
            return new tcp(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8],
                    params[9], params[10], params[11], params[12], params[13], params[14]);
        }
    }
}
