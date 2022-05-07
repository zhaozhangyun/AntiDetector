package com.kmdc.mdu.oaid;

import android.content.Context;

import com.bun.miitmdid.core.InfoCode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import zizzy.zhao.bridgex.l.L;

class MsaSdkClient2 {

    static String getOaid(Context context, long maxWaitTimeInMilli) {
        final BlockingQueue<String> oaidHolder = new LinkedBlockingQueue<>(1);

        try {
            boolean msaInternalLogging = true;
//            int result = MdidSdkHelper.InitSdk(context, msaInternalLogging, idSupplier -> {
//                try {
//                    if (idSupplier == null || idSupplier.getOAID() == null) {
//                        // so to avoid waiting for timeout
//                        oaidHolder.offer("");
//                    } else {
//                        oaidHolder.offer(idSupplier.getOAID());
//                    }
//                } catch (Exception e) {
//                    L.e(+ "-" + SDK_CODE, "Fail to add " + e.getMessage());
//                }
//            });

            int result = -1;

            /* Implementation using reflection */
//            Method initSdk = MdidSdkHelper.class.getDeclaredMethod("InitSdk",
//                    Context.class, boolean.class,
//                    IIdentifierListener.class);
//            result = (int) initSdk.invoke(MdidSdkHelper.class, context, msaInternalLogging,
//                    (IIdentifierListener) idSupplier -> {
//                        try {
//                            if (idSupplier == null || idSupplier.getOAID() == null) {
//                                // so to avoid waiting for timeout
//                                oaidHolder.offer("");
//                            } else {
//                                oaidHolder.offer(idSupplier.getOAID());
//                            }
//                        } catch (Exception e) {
//                            L.e(+ "-" + SDK_CODE, "Fail to add " + e.getMessage());
//                        }
//                    });

            Class iIdentifierListenerV23 = null;
            Class iIdentifierListenerV13 = null;
            boolean isMiitMdidAboveV23 = false;
            boolean isMiitMdidOnV13 = false;
            Class iIdentifierListener = null;

            try {
                iIdentifierListenerV23 = Reflection.forName(
                        "com.bun.miitmdid.interfaces.IIdentifierListener");
            } catch (ClassNotFoundException e) {
                L.e("call forName error: " + e);
                try {
                    iIdentifierListenerV13 = Reflection.forName(
                            "com.bun.supplier.IIdentifierListener");
                } catch (ClassNotFoundException e1) {
                    L.e("call forName error: " + e1);
                }
            }

            isMiitMdidAboveV23 = iIdentifierListenerV23 != null;
            if (isMiitMdidAboveV23) {
                L.i("Using MIIT MID Above V1.0.23");
            } else {
                isMiitMdidOnV13 = iIdentifierListenerV13 != null;
                if (isMiitMdidOnV13) {
                    L.i("Using MIIT MID V1.0.13");
                }
            }

            if (isMiitMdidAboveV23) {
                iIdentifierListener = iIdentifierListenerV23;
            } else if (isMiitMdidOnV13) {
                iIdentifierListener = iIdentifierListenerV13;
            }

            Object iIdentifierListenerProxy = Proxy.newProxyInstance(
                    iIdentifierListener.getClassLoader(),
                    new Class[]{iIdentifierListener},
                    new HookHandler(oaidHolder)
            );

            Class mdidSdkHelper = null;
            try {
                mdidSdkHelper = Reflection.forName("com.bun.miitmdid.core.MdidSdkHelper");
            } catch (ClassNotFoundException e) {
                L.e("call forName error: " + e);
            }
            try {
                result = (int) Reflection.invokeMethod(
                        mdidSdkHelper,
                        "InitSdk",
                        null,
                        new Class[]{Context.class, boolean.class, iIdentifierListener},
                        context, msaInternalLogging, iIdentifierListenerProxy
                );
            } catch (Throwable t) {
                L.e("call invokeMethod error: " + t);
            }

            if (!isError(result)) {
                return oaidHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);
            }
        } catch (NoClassDefFoundError ex) {
            L.e("Couldn't find msa sdk: " + ex.getMessage());
        } catch (InterruptedException e) {
            L.e("Waiting to read oaid from callback interrupted: " + e.getMessage());
        } catch (Throwable t) {
            L.e("Oaid reading process failed: " + t.getMessage());
        }

        return null;
    }

    private static boolean isError(int result) {
        switch (result) {
            case InfoCode.INIT_ERROR_CERT_ERROR:
                L.e("msa sdk error - INIT_ERROR_CERT_ERROR");
                return true;
            case InfoCode.INIT_ERROR_DEVICE_NOSUPPORT:
                L.e("msa sdk error - INIT_ERROR_DEVICE_NOSUPPORT");
                return true;
            case InfoCode.INIT_ERROR_LOAD_CONFIGFILE:
                L.e("msa sdk error - INIT_ERROR_LOAD_CONFIGFILE");
                return true;
            case InfoCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                L.e("msa sdk error - INIT_ERROR_MANUFACTURER_NOSUPPORT");
                return true;
            case InfoCode.INIT_ERROR_SDK_CALL_ERROR:
                L.e("msa sdk error - INIT_ERROR_SDK_CALL_ERROR");
                return true;
            case InfoCode.INIT_INFO_RESULT_DELAY:
                L.e("msa sdk error - INIT_INFO_RESULT_DELAY");
                return true;
            case InfoCode.INIT_INFO_RESULT_OK:
                L.i("msa sdk success - INIT_INFO_RESULT_OK");
                return false;
            default:
                L.e("MdidSdkHelper.InitSdk result: " + result);
                return true;
        }
    }

    static class HookHandler implements InvocationHandler {
        private BlockingQueue<String> oaidHolder;

        public HookHandler(BlockingQueue<String> oaidHolder) {
            this.oaidHolder = oaidHolder;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            L.v("--- [" + method + "] called with args "
                    + Arrays.toString(args));
            Object idSupplier = null;

            // com.bun.miitmdid.interfaces.IIdentifierListener.onSupport(com.bun.miitmdid.interfaces.IdSupplier)
            if ("onSupport".equals(method.getName())) {
                idSupplier = args[0];
            }
            // com.bun.miitmdid.interfaces.IIdentifierListener.OnSupport(boolean, com.bun.miitmdid.interfaces.IdSupplier)
            else if ("OnSupport".equals(method.getName())) {
                idSupplier = args[1];
            }

            try {
                boolean isSupported = (boolean) Reflection.invokeInstanceMethod(
                        idSupplier,
                        "isSupported",
                        new Class[]{}
                );
                L.d("isSupported: " + isSupported);
            } catch (Throwable t) {
                L.e("Failed to invoke method: " + t);
            }

            try {
                boolean isLimited = (boolean) Reflection.invokeInstanceMethod(
                        idSupplier,
                        "isLimited",
                        new Class[]{}
                );
                L.d("isLimited: " + isLimited);
            } catch (Throwable t) {
                L.e("Failed to invoke method: " + t);
            }

            try {
                String vaid = (String) Reflection.invokeInstanceMethod(
                        idSupplier,
                        "getVAID",
                        new Class[]{}
                );
                L.d("vaid: " + vaid);
            } catch (Throwable t) {
                L.e("Failed to invoke method: " + t);
            }

            try {
                String aaid = (String) Reflection.invokeInstanceMethod(
                        idSupplier,
                        "getAAID",
                        new Class[]{}
                );
                L.d("aaid: " + aaid);
            } catch (Throwable t) {
                L.e("Failed to invoke method: " + t);
            }

            try {
                String oaid = (String) Reflection.invokeInstanceMethod(
                        idSupplier,
                        "getOAID",
                        new Class[]{}
                );
                L.d("oaid: " + oaid);

                if (oaid == null) {
                    // so to avoid waiting for timeout
                    oaidHolder.offer("");
                } else {
                    oaidHolder.offer(oaid);
                }
            } catch (Throwable t) {
                L.e("Failed to invoke method: " + t);
            }

            return null;
        }
    }
}
