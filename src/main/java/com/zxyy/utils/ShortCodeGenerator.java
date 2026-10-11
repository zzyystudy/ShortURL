package com.zxyy.utils;

import cn.hutool.core.lang.hash.MurmurHash;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** MurmurHash3 的 32 位结果转为固定 6 位 Base62，不使用码池。 */
@Component
public class ShortCodeGenerator {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generate(String longUrl, int attempt) {
        // 第一次直接计算长网址的哈希，发生冲突后改变输入，避免一直得到相同短码。
        String input = attempt == 0 ? longUrl : longUrl + "#" + UUID.randomUUID();
        int hash = MurmurHash.hash32(input.getBytes(StandardCharsets.UTF_8));
        // int 可能是负数，按无符号整数转换，不能用 Math.abs（MIN_VALUE 会溢出）。
        long value = Integer.toUnsignedLong(hash);
        char[] code = new char[6];
        for (int index = code.length - 1; index >= 0; index--) {
            code[index] = ALPHABET.charAt((int) (value % 62));
            value /= 62;
        }
        return new String(code);
    }
}
