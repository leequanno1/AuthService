package com.project.q_authent.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Json util
 * Last updated at 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
public class JsonUtils {

    /**
     * Convert object to json String
     * @param obj {@link Object}
     * @return json string
     * @since 1.00
     */
    public static String toJson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Convert json string to object
     * @param json {@link String}
     * @return Converted object
     * @param <T> Destination object type
     * @since 1.00
     */
    public static <T> T fromJson(String json) {
        try {
            TypeReference<T> typeRef = new TypeReference<>() {};
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
}
