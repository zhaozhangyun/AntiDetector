package com.kmdc.mdu.utils;

import static com.kmdc.mdu.utils.Base64Utils.base64Decode;
import static com.kmdc.mdu.utils.Base64Utils.base64Encode;

import android.util.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import zizzy.zhao.bridgex.l.L;

public class AESUtils {

    /**
     * 采用AES加密算法
     */
    private static final String KEY_ALGORITHM = "AES";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "#157Dk{5?jga/V2F";
    private static final String IV = "h4A({q$x9)s7@9[v";

    public static String encrypt(String rawData) {
        return encrypt(rawData, SECRET_KEY, IV);
    }

    /**
     * AES 加密
     *
     * @param rawData   原始内容
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @param ivs       加密向量，长度：16
     * @return 返回 Base64 转码后的加密数据
     */
    public static String encrypt(String rawData, String secretKey, String ivs) {
        try {
            if (!(secretKey.length() == 16 || secretKey.length() == 32)) {
                throw new IllegalStateException("expected SecretKey length of 16/32 but was "
                        + secretKey.length());
            }

            if (ivs.length() != 16) {
                throw new IllegalStateException("expected IV length of 16 but was " + ivs.length());
            }

            // 创建AES秘钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
                    KEY_ALGORITHM);
            // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            IvParameterSpec iv = new IvParameterSpec(ivs.getBytes());
            // 创建密码器
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化加密器
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encryptByte = cipher.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            // 将加密以后的数据进行 Base64 编码
            byte[] plainText = base64Encode(encryptByte);
            String retval = new String(plainText, StandardCharsets.UTF_8);
            L.logF("rawData: %s, retval: %s", rawData, retval);
            return retval;
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    public static String decrypt(String base64Data) {
        return decrypt(base64Data, SECRET_KEY, IV);
    }

    /**
     * AES 解密
     *
     * @param base64Data 加密的密文 Base64 字符串
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     * @param ivs        加密向量，长度：16
     * @return 返回原始数据
     */
    public static String decrypt(String base64Data, String secretKey, String ivs) {
        try {
            if (!(secretKey.length() == 16 || secretKey.length() == 32)) {
                throw new IllegalStateException("expected SecretKey length of 16/32 but was "
                        + secretKey.length());
            }

            if (ivs.length() != 16) {
                throw new IllegalStateException("expected IV length of 16 but was " + ivs.length());
            }

            byte[] rawData = base64Decode(base64Data);
            // 创建AES秘钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
                    KEY_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(ivs.getBytes());
            // 创建密码器
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
            // 执行解密操作
            byte[] result = cipher.doFinal(rawData);
            String retval = new String(result, StandardCharsets.UTF_8);
            L.logF("base64Data: %s, retval: %s", base64Data, retval);
            return retval;
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }
}