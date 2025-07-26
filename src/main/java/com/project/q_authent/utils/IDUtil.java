package com.project.q_authent.utils;

import com.project.q_authent.constances.TableIdHeader;

import java.util.UUID;

/**
 * Create ID base on table header
 * Last updated at 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
public class IDUtil {
    public static String getID(TableIdHeader header){
        return header.getValue() + UUID.randomUUID().toString().replace("-", "");
    }
}
