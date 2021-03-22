package com.hai046.builder.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.CryptoException;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author h
 * date 2020-11-26
 */
public class UuidUtils {

    private final static byte[] DEFAULT_KEY = "423121:HU14:3568".getBytes();

    public static <T extends Number> T convertUuidToId(String uuid) {
        return getIdByUuid(DEFAULT_KEY, uuid);
    }

    private static <T extends Number> T getIdByUuid(byte[] key, String uuid) {
        T t = null;
        if (uuid != null) {
            ByteBuffer buffer;
            try {
                buffer = ByteBuffer.wrap(getAES(key).decrypt(Base64.decode(uuid)));
            } catch (CryptoException e) {
                throw new RuntimeException("uuid -> id convert error, uuid=" + uuid, e);
            }
            if (buffer.capacity() == Long.BYTES) {
                t = (T) new Long(buffer.getLong());
            } else if (buffer.capacity() == Integer.BYTES) {
                t = (T) new Integer(buffer.getInt());
            } else if (buffer.capacity() == Short.BYTES) {
                t = (T) new Short(buffer.getShort());
            } else if (buffer.capacity() == Byte.BYTES) {
                t = (T) new Byte(buffer.get());
            } else {
                throw new RuntimeException("uuid 反解不支持该类型  BYTES=" + buffer.capacity());
            }
        }
        return t;
    }

    private final static Map<byte[], AES> acsCache = new HashMap<>();

    /**
     * 获取对应key的加密算法
     *
     * @param key
     * @return
     */
    private static AES getAES(byte[] key) {
        return acsCache.computeIfAbsent(key, n -> SecureUtil.aes(key));
    }

    public static String convertIdToUUid(Number id) {
        return getUuidById(DEFAULT_KEY, id);
    }

    private static String getUuidById(byte[] key, Number id) {
        String uuid = null;
        if (id != null) {
            ByteBuffer buffer;
            if (id instanceof Long) {
                buffer = ByteBuffer.allocate(8);
                buffer.putLong(id.longValue());
            } else if (id instanceof Integer) {
                buffer = ByteBuffer.allocate(4);
                buffer.putInt(id.intValue());
            } else if (id instanceof Short) {
                buffer = ByteBuffer.allocate(2);
                buffer.putShort(id.shortValue());
            } else if (id instanceof Byte) {
                buffer = ByteBuffer.allocate(1);
                buffer.put(id.byteValue());
            } else {
                throw new RuntimeException("id加密只支持long,int,short,byte类型");
            }
            uuid = Base64.encodeUrlSafe(getAES(key).encrypt(buffer.array()));
        }
        return uuid;
    }

    public static <T extends Number> T getUserId(String uuid) {
        return getIdByUuid(DEFAULT_KEY, uuid);
    }

    public static String getUserUuId(Number id) {
        return getUuidById(DEFAULT_KEY, id);
    }

}
