package com.example.theorypractice;

enum TheoryMode {
    INTERVALS("Intervals", "intervals"),
    GUITAR_TRIADS("Guitar Triads", "guitar_triads"),
    CHORD_SPELLING("Chord Spelling", "chord_spelling");

    final String displayName;
    final String directoryName;

    TheoryMode(String displayName, String directoryName) {
        this.displayName = displayName;
        this.directoryName = directoryName;
    }

    static TheoryMode fromDisplayName(String displayName) {
        for (TheoryMode mode : values()) {
            if (mode.displayName.equals(displayName)) {
                return mode;
            }
        }
        return INTERVALS;
    }
}
