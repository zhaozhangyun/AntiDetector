package com.kmdc.mdu.utils;

import static com.kmdc.mdu.utils.Base64Utils.base64Decode;
import static com.kmdc.mdu.utils.Base64Utils.base64Encode;
import static com.kmdc.mdu.utils.Base64Utils.base64EncodeToStr;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import zizzy.zhao.bridgex.l.L;

/**
 * 常规加密，加密内容不能超过2048，超过的话会报错。
 *
 * <p>
 * 移动端和WEB端的混合加密(RSA和AES)
 * <p>
 * 流程
 * <p>
 * 是先由服务器创建RSA密钥对，RSA公钥保存在安卓的so文件里面，服务器保存RSA私钥。而安卓创建AES密钥(这个密钥也是在so文件里面)，
 * 并用该AES密钥加密待传送的明文数据，同时用接受的RSA公钥加密AES密钥，最后把用RSA公钥加密后的AES密钥同密文一起传输发送到服务器。
 * 当服务器收到这个被加密的AES密钥和密文后，首先调用服务器保存的RSA私钥，并用该私钥解密加密的AES密钥，得到AES密钥。
 * 最后用该AES密钥解密密文得到明文。
 */
public class RSAUtils {

    /**
     * RSA算法
     */
    private static final String RSA = "RSA";
    /**
     * 加密方式，android的
     */
//  private static final String TRANSFORMATION = "RSA/None/NoPadding";
    /**
     * 加密方式，标准jdk的
     */
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    private static final int MAX_BLOCK = 256;
    public static final String PUBLIC_KEY_TEST = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArveZpSmfs9YXgM8NjhKjIyUFMUkKyQ/F9Ebey0p3yanD0wnmsdtxLyi8yIhXG2Yhzyt+SgoGNQQUQvPGDPhjGpIa2xbmQClr+9lpv0upzbvTMjEOGHxXbApF7ylGfy3yz6iteIFcLalcxGjYBn8v3RqULYJyyaRYW9w7O988ShZPdZfJXe+KS4rjXlU4zVHHOVlp3irog1ePmq35iOOOKSrHFfJYjOOIIRO5QGDqBTn5KFG/wXFiHNPdXkOS+BqYZyy1FoqFc0oI/c7SFtech2JlABkGHPU1W/wl1+9u91AlbQ4V/8Lq6q8EmNuIRpQ2044wyDsWVOlNlYI99YFnlQIDAQAB";
//    public static final String PRIVATE_KEY_TEST = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCu95mlKZ+z1heAzw2OEqMjJQUxSQrJD8X0Rt7LSnfJqcPTCeax23EvKLzIiFcbZiHPK35KCgY1BBRC88YM+GMakhrbFuZAKWv72Wm/S6nNu9MyMQ4YfFdsCkXvKUZ/LfLPqK14gVwtqVzEaNgGfy/dGpQtgnLJpFhb3Ds73zxKFk91l8ld74pLiuNeVTjNUcc5WWneKuiDV4+arfmI444pKscV8liM44ghE7lAYOoFOfkoUb/BcWIc091eQ5L4GphnLLUWioVzSgj9ztIW15yHYmUAGQYc9TVb/CXX7273UCVtDhX/wurqrwSY24hGlDbTjjDIOxZU6U2Vgj31gWeVAgMBAAECggEAA5GNPVnMphBXHmDFYqvCpAmHQkATy5IEyZIAd/Om3yK/7w1OpTtX4vcDvVtP24Ezw1PxxB98N5dDtTHfojgWsrNe1NpyC8ZMgNsoVacnaXN2lUQm3hz7HiWqKNHrXCtbE8fZnJGW3bTh7Xzt8FlREpi42Pdw0T1cMqjFPy1dv20NwQlJKzHVCPOAA0nDwM9y04BtQE/BmJ8q+S6hCRLEkouQC+j4WDxyjiH7Uh+eaJF13HOJiLfywwbrjmbQIqOWgAq4sYRmUslckJg6QS54S4YzZsA+KU2Ov9rKNBZYGu9Fvdx0nC0KKZOjrcj2TARK9I+KLpE00sGK9YtEQzaOaQKBgQDeW+xrULZ7sShpxZf1+YwK5z+ZRErNtOPXRqVjcBJ5kvoNzQ40wSEDDxTW2gTvzw9cCt3XRq/kaKNrYQFkPbWujr7GyBhJWaDBhXkP5D4dlfDAqGc55tVHBHu4a371TNw+l5N0dxr3sm2FhDvKDza8LfBfXqi9k0+wneKVyBvh6wKBgQDJcCpe2ng3DjdjvW0iKJPhzfdzw28f+A6spGvAq/FS5he2cvV0pGj933V6EIFp5X9PweSnL4X2r6dVAgonz4O4wlyuSurLxPEIRsVRDtZ/9zjm7/VLNo/E9UaTsAFbKRqF9mIGqeuOXI4eqgP07nlh+ADiPupvfZqPvFYmXJv8fwKBgA60aYgXSFoZtyB3jrsXi6lU7aHHIGNGKSMdauaOjMo5bAvpmZzngLVhE/G5bUYmnU9q8IzCAfPK77O0MJFee1tV2UvwA5smMDlcCEuCvpDaT5eOC3WKzGPr6fqiMT0rng0NnRTAo6npxNBllEaihu4e45yK+AHBci2t40QgfaeNAoGAUAERgvzIFC28SAvbBV8SZPN13W94ejz+Lwlalnpz7VXfgyIjZMFLxdDziph1ncZ9iwLaOqCAV1qqXfPibq9XhJEFWF/+4xGEHaJmCeyXutlIq0gQp1+zOCmmb0/PKhKoKx6RDk58dN1DwOALlEMGyKdESpdsav163q2RlcjgVycCgYEAmLkVG4A9vRU0q8HKMP3mVDcZE3rQd4Jbe49UNd9Jd6hDbf+E/3cZNXK5ZVV/gIXN+F6yyyx6gxQ+7z13douo/BzXlz2511jy7P0XWN4W5R2+0Lbt9TduaGJQrBzTCIUFMg86QC0aHKrcbafO2EiPDsdnOCv9Lzm+ElhV/k4Ltjg=";

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       加密数据
     * @param privateKey 私钥
     */
    public static String sign(byte[] data, String privateKey) {
        try {
            byte[] keyBytes = base64Decode(privateKey);
            PrivateKey priKey = KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(
                    keyBytes));
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initSign(priKey);
            sig.update(data);
            return base64EncodeToStr(sig.sign());
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    /**
     * 校验数字签名
     *
     * @param data      加密数据
     * @param publicKey 公钥
     * @param sign      数字签名
     * @return 校验成功返回 true 失败返回 false
     */
    public static boolean verify(byte[] data, String publicKey, String sign) {
        try {
            PublicKey pubKey = KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(
                    base64Decode(publicKey)));
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(pubKey);
            sig.update(data);
            return sig.verify(base64Decode(sign));
        } catch (Exception e) {
            L.e(e);
        }
        return false;
    }

    /**
     * 解密<br>
     * 用私钥解密
     * <br>
     *
     * @param base64Data
     * @param key
     */
    public static String decryptByPrivateKey(String base64Data, String key) {
        try {
            Key privateKey = KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(
                    base64Decode(key)));
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] data = base64Decode(base64Data);
//          byte[] result = cipher.doFinal(data); // 常规加密，加密内容不能超过2048
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_BLOCK;
            }
            byte[] result = out.toByteArray();
            out.flush();
            out.close();
            String retval = new String(result, StandardCharsets.UTF_8);
//            L.logF("base64Data: %s, retval: %s", base64Data, retval);
            return retval;
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    /**
     * 解密<br>
     * 用公钥解密
     * <br>
     *
     * @param base64Data
     * @param key
     */
    public static String decryptByPublicKey(String base64Data, String key) {
        try {
            Key publicKey = KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(
                    base64Decode(key)));
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] data = base64Decode(base64Data);
//        byte[] result = cipher.doFinal(data); // 常规加密，加密内容不能超过2048
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_BLOCK;
            }
            byte[] result = out.toByteArray();
            out.flush();
            out.close();
            String retval = new String(result, StandardCharsets.UTF_8);
//            L.logF("base64Data: %s, retval: %s", base64Data, retval);
            return retval;
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    /**
     * 加密<br>
     * 用公钥加密
     * <br>
     *
     * @param rawData
     * @param key
     */
    public static String encryptByPublicKey(String rawData, String key) {
        try {
            Key publicKey = KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(
                    base64Decode(key)));
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] data = rawData.getBytes(StandardCharsets.UTF_8);
//        byte[] encryptByte = cipher.doFinal(data); // 常规加密，加密内容不能超过2048
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] buffer;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_BLOCK) {
                    buffer = cipher.doFinal(data, offSet, MAX_BLOCK);
                } else {
                    buffer = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(buffer, 0, buffer.length);
                i++;
                offSet = i * MAX_BLOCK;
            }
            byte[] encryptByte = out.toByteArray();
            out.flush();
            out.close();
            // 将加密以后的数据进行 Base64 编码
            byte[] plainText = base64Encode(encryptByte);
            String retval = new String(plainText, StandardCharsets.UTF_8);
//            L.logF("rawData: %s, retval: %s", rawData, retval);
            return retval;
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    /**
     * 加密<br>
     * 用私钥加密
     * <br>
     *
     * @param rawData
     * @param key
     */
    public static String encryptByPrivateKey(String rawData, String key) {
        try {
            Key privateKey = KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(
                    base64Decode(key)));
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] data = rawData.getBytes(StandardCharsets.UTF_8);
//        byte[] encryptByte = cipher.doFinal(data); // 常规加密，加密内容不能超过2048
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] buffer;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_BLOCK) {
                    buffer = cipher.doFinal(data, offSet, MAX_BLOCK);
                } else {
                    buffer = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(buffer, 0, buffer.length);
                i++;
                offSet = i * MAX_BLOCK;
            }
            byte[] encryptByte = out.toByteArray();
            out.flush();
            out.close();
            // 将加密以后的数据进行 Base64 编码
            byte[] plainText = base64Encode(encryptByte);
            String retval = new String(plainText, StandardCharsets.UTF_8);
//            L.logF("rawData: %s, retval: %s", rawData, retval);
            return retval;
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    /**
     * 取得私钥
     *
     * @param keyMap
     */
    public static String getPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return base64EncodeToStr(key.getEncoded());
    }

    /**
     * 取得公钥
     *
     * @param keyMap
     */
    public static String getPublicKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return base64EncodeToStr(key.getEncoded());
    }

    /**
     * 初始化密钥
     */
    public static Map<String, Object> initKey() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String, Object> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, publicKey);
            keyMap.put(PRIVATE_KEY, privateKey);
            L.d(keyMap);
            return keyMap;
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

//    public static void main(String[] args) {
//        try {
//            Map<String, Object> key = initKey();
//            String publicKey = getPublicKey(key);
//            String privateKey = getPrivateKey(key);
//            String signbypub = encryptByPublicKey("wenfei", PUBLIC_KEY_TEST);
//            String ecodeStr = decryptByPrivateKey(signbypub, PRIVATE_KEY_TEST);
//            Log.i("rsa", publicKey);
//            Log.i("rsa", privateKey);
//            Log.i("RSA", signbypub);
//            Log.i("RSA", ecodeStr);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}