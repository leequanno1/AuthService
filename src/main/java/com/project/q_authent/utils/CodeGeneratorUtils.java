package com.project.q_authent.utils;

import java.util.Random;

public class CodeGeneratorUtils {

    private static final int min = 100000;
    private static final int max = 999999;

    private static final Random random = new Random();

    public static Integer generateCode() {
        return random.nextInt(max - min + 1) + min;
    }
}
