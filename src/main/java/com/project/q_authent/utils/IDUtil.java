package com.project.q_authent.utils;

import com.project.q_authent.constances.TableIdHeader;

import java.util.UUID;

public class IDUtil {
    public static String getID(TableIdHeader header){
        return header.getValue() + UUID.randomUUID().toString().replace("-", "");
    }
}
