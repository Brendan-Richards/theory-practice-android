package com.example.theorypractice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Constants {
    static final List<List<String>> CHROMATICS = Arrays.asList(
            Arrays.asList("A"),
            Arrays.asList("A#", "Bb"),
            Arrays.asList("B"),
            Arrays.asList("C"),
            Arrays.asList("C#", "Db"),
            Arrays.asList("D"),
            Arrays.asList("D#", "Eb"),
            Arrays.asList("E"),
            Arrays.asList("F"),
            Arrays.asList("F#", "Gb"),
            Arrays.asList("G"),
            Arrays.asList("G#", "Ab")
    );

    static final Map<String, Integer> INTERVALS = new LinkedHashMap<>();
    static final Map<String, int[][]> TRIAD_FORMULAS = new HashMap<>();

    static {
        INTERVALS.put("m2", 1);
        INTERVALS.put("M2", 2);
        INTERVALS.put("m3", 3);
        INTERVALS.put("M3", 4);
        INTERVALS.put("P4", 5);
        INTERVALS.put("Tritone", 6);
        INTERVALS.put("P5", 7);
        INTERVALS.put("m6", 8);
        INTERVALS.put("M6", 9);
        INTERVALS.put("m7", 10);
        INTERVALS.put("M7", 11);

        putTriad("m_e_0", 0, 2, 3);
        putTriad("m_a_0", 0, 2, 3);
        putTriad("m_d_0", 0, 2, 2);
        putTriad("m_g_0", 0, 1, 2);
        putTriad("m_e_1", 0, 1, 1);
        putTriad("m_a_1", 0, 1, 1);
        putTriad("m_d_1", 0, 1, 0);
        putTriad("m_g_1", 0, 0, 0);
        putTriad("m_e_2", 0, 0, 2);
        putTriad("m_a_2", 0, 0, 2);
        putTriad("m_d_2", 0, 0, 1);
        putTriad("m_g_2", 0, -1, 1);

        putTriad("M_e_0", 0, 1, 3);
        putTriad("M_a_0", 0, 1, 3);
        putTriad("M_d_0", 0, 1, 2);
        putTriad("M_g_0", 0, 0, 2);
        putTriad("M_e_1", 0, 2, 2);
        putTriad("M_a_1", 0, 2, 2);
        putTriad("M_d_1", 0, 2, 1);
        putTriad("M_g_1", 0, 1, 1);
        putTriad("M_e_2", 0, 0, 1);
        putTriad("M_a_2", 0, 0, 1);
        putTriad("M_d_2", 0, 0, 0);
        putTriad("M_g_2", 0, -1, 0);

        putTriad("dim_e_0", 0, 2, 4);
        putTriad("dim_a_0", 0, 2, 4);
        putTriad("dim_d_0", 0, 2, 3);
        putTriad("dim_g_0", 0, 1, 3);
        putTriad("dim_e_1", 0, 2, 1);
        putTriad("dim_a_1", 0, 2, 1);
        putTriad("dim_d_1", 0, 2, 0);
        putTriad("dim_g_1", 0, 1, 0);
        putTriad("dim_e_2", 0, -1, 1);
        putTriad("dim_a_2", 0, -1, 1);
        putTriad("dim_d_2", 0, -1, 0);
        putTriad("dim_g_2", 0, -2, 0);

        putTriad("aug_e_0", 0, 1, 2);
        putTriad("aug_a_0", 0, 1, 2);
        putTriad("aug_d_0", 0, 1, 1);
        putTriad("aug_g_0", 0, 0, 1);
        putTriad("aug_e_1", 0, 1, 2);
        putTriad("aug_a_1", 0, 1, 2);
        putTriad("aug_d_1", 0, 1, 1);
        putTriad("aug_g_1", 0, 0, 1);
        putTriad("aug_e_2", 0, 1, 2);
        putTriad("aug_a_2", 0, 1, 2);
        putTriad("aug_d_2", 0, 1, 1);
        putTriad("aug_g_2", 0, 0, 1);
    }

    private static void putTriad(String key, int first, int second, int third) {
        TRIAD_FORMULAS.put(key, new int[][]{{0, first}, {1, second}, {2, third}});
    }

    private Constants() {
    }
}
