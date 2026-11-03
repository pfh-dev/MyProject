package com.petadopt.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM 加解密工具，用于敏感字段（手机号、联系方式等）加密。
 *
 * <p>为什么需要它：领养人、志愿者等实体的联系方式属于个人隐私，
 * 不能明文存进数据库。这里采用 AES-GCM 对称加密，把明文加密成密文再落库，
 * 读取时再解密成明文返回给前端。</p>
 *
 * <p>AES-GCM 相比普通 AES 多了一层"完整性校验"（认证标签），
 * 如果密文被篡改，解密时会直接抛异常，从而防止数据被偷偷修改。</p>
 */
@Component
public class AesUtil {

    // GCM 模式推荐的随机数(IV/Nonce)长度固定为 12 字节
    private static final int IV_LENGTH = 12;
    // 认证标签长度 128 位，用来校验密文是否被篡改
    private static final int TAG_BITS = 128;

    // 从配置文件 application.yml 里读取密钥（security.aes-key），
    // 密钥不能写死在代码里，方便部署时替换。
    @Value("${security.aes-key}")
    private String aesKey;

    /**
     * 根据配置的密钥字符串构造 AES 密钥对象。
     * 密钥长度必须满足 AES 的要求（128/192/256 位）。
     */
    private SecretKeySpec keySpec() {
        return new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
    }

    /**
     * 加密：把明文加密成 Base64 字符串（密文）。
     *
     * @param plain 明文，例如手机号 "13800000000"
     * @return 加密后的 Base64 字符串；如果明文为空则原样返回
     */
    public String encrypt(String plain) {
        // 空值直接返回，避免对空字段做无意义加密
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        try {
            // 1. 生成一个 12 字节的随机数 IV。每次加密都不同，
            //    这样同一个明文两次加密会得到不同密文，防止"撞库"分析。
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 2. 创建 AES-GCM 加密器，并传入密钥和 IV
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_BITS, iv));

            // 3. 执行加密，得到密文字节（末尾还附带认证标签）
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            // 4. 把 IV 和密文拼在一起：IV 需要随密文一起保存，
            //    否则解密时拿不到当初的随机数就无法还原。
            byte[] result = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);

            // 5. 转成 Base64 字符串，方便存数据库（二进制无法直接存文本字段）
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密：把密文还原成明文。
     *
     * @param cipherText 加密后的 Base64 字符串
     * @return 解密后的明文；如果入参为空，或入参本来就不是密文（历史明文数据），
     *         则原样返回以保证兼容
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            // 1. 先做 Base64 解码，得到 IV + 密文的字节数组
            byte[] data = Base64.getDecoder().decode(cipherText);

            // 2. 从前面拆出 12 字节的 IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, IV_LENGTH);

            // 3. 用同样的密钥和 IV 创建解密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_BITS, iv));

            // 4. 解密 IV 之后的那部分数据
            byte[] decrypted = cipher.doFinal(data, IV_LENGTH, data.length - IV_LENGTH);

            // 5. 还原成 UTF-8 字符串返回
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 非密文（如历史明文数据）直接返回原值，保证兼容
            return cipherText;
        }
    }
}
