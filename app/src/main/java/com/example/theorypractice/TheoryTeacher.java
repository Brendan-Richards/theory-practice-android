package com.example.theorypractice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

final class TheoryTeacher {
    private final ConfigRepository configRepository;
    private final Random random = new Random();

    private TheoryMode mode = TheoryMode.INTERVALS;
    private TheoryConfig config;
    private List<String> roots = new ArrayList<>();
    private List<Integer> rootIds = new ArrayList<>();
    private int correct = 0;
    private int total = 0;

    TheoryTeacher(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    List<String> getConfigNames(TheoryMode mode) throws IOException {
        return configRepository.listConfigNames(mode);
    }

    void setTheoryMode(TheoryMode mode) {
        this.mode = mode;
        correct = 0;
        total = 0;
    }

    void loadConfig(String configName) throws IOException {
        config = configRepository.loadConfig(mode, configName);
        validateConfig(config);
        roots = getRoots(config.roots);
        rootIds = findRootIds(roots);
        if (rootIds.isEmpty()) {
            throw new IllegalArgumentException("Config did not produce any playable roots.");
        }
    }

    Question generateQuestion() {
        if (mode == TheoryMode.INTERVALS) {
            return generateIntervalQuestion();
        } else if (mode == TheoryMode.GUITAR_TRIADS) {
            return generateGuitarTriadQuestion();
        }
        return generateChordSpellingQuestion();
    }

    GradeResult grade(String guess, Question question) {
        String feedback;
        if (mode == TheoryMode.GUITAR_TRIADS) {
            feedback = "Answer was:\n" + question.revealAnswer;
        } else {
            String cleanedGuess = guess == null ? "" : guess.trim();
            boolean isCorrect = question.acceptedAnswers.contains(cleanedGuess);
            if (isCorrect) {
                correct += 1;
                List<String> alternatives = new ArrayList<>(question.acceptedAnswers);
                alternatives.remove(cleanedGuess);
                feedback = alternatives.isEmpty()
                        ? "Correct"
                        : "Correct, also would have accepted: " + alternatives;
            } else {
                feedback = "Incorrect, the answer was " + question.acceptedAnswers;
            }
        }

        total += 1;
        return new GradeResult(correct, total, feedback);
    }

    GradeResult gradeTouchAnswer(boolean isCorrect, Question question) {
        String feedback;
        if (mode == TheoryMode.GUITAR_TRIADS) {
            feedback = "Answer was:\n" + question.revealAnswer;
        } else if (isCorrect) {
            correct += 1;
            feedback = question.acceptedAnswers.size() > 1
                    ? "Correct, accepted spellings: " + question.acceptedAnswers
                    : "Correct";
        } else {
            feedback = "Incorrect, the answer was " + question.revealAnswer;
        }

        total += 1;
        return new GradeResult(correct, total, feedback);
    }

    int getCorrect() {
        return correct;
    }

    int getTotal() {
        return total;
    }

    private Question generateChordSpellingQuestion() {
        int rootId = pickInteger(rootIds);
        String root = pickMatchingRoot(rootId);
        String chordType = pickString(config.chordTypes);
        int thirdIndex = mod(
                rootId + (chordType.equals("dim") || chordType.equals("m") ? 3 : 4),
                Constants.CHROMATICS.size()
        );
        int fifthIndex = mod(rootId + fifthOffset(chordType), Constants.CHROMATICS.size());
        List<String> thirdChoices = Constants.CHROMATICS.get(thirdIndex);
        List<String> fifthChoices = Constants.CHROMATICS.get(fifthIndex);

        boolean preferFlat = root.contains("b");
        String third = preferredSpelling(thirdChoices, preferFlat);
        String fifth = preferredSpelling(fifthChoices, preferFlat);
        String mainAnswer = root + "-" + third + "-" + fifth;

        Set<String> allAnswers = new LinkedHashSet<>();
        allAnswers.add(mainAnswer);
        for (String thirdChoice : thirdChoices) {
            for (String fifthChoice : fifthChoices) {
                allAnswers.add(root + "-" + thirdChoice + "-" + fifthChoice);
            }
        }

        String displayType = chordType.replace("dim", "°").replace("aug", "+");
        return new Question(root + displayType, new ArrayList<>(allAnswers), mainAnswer, -1, thirdIndex, fifthIndex);
    }

    private Question generateIntervalQuestion() {
        int rootId = pickInteger(rootIds);
        String root = pickMatchingRoot(rootId);
        int intervalIndex = random.nextInt(config.intervals.size());
        String interval = config.intervals.get(intervalIndex);
        String[] directions = config.directions.get(intervalIndex).split("/");
        String direction = directions[random.nextInt(directions.length)];
        int distance = Constants.INTERVALS.get(interval);
        int answerIndex = direction.equals("a")
                ? mod(rootId + distance, Constants.CHROMATICS.size())
                : mod(rootId - distance, Constants.CHROMATICS.size());

        List<String> answer = new ArrayList<>(Constants.CHROMATICS.get(answerIndex));
        String directionText = direction.equals("a") ? "Ascending" : "Descending";
        String prompt = "Root: " + root
                + "\nInterval: " + interval
                + "\nDirection: " + directionText;
        return new Question(prompt, answer, answer.toString(), answerIndex, -1, -1);
    }

    private Question generateGuitarTriadQuestion() {
        int rootId = pickInteger(rootIds);
        String root = pickMatchingRoot(rootId);
        String lowString = pickString(config.lowStrings);
        int inversion = pickInteger(config.inversions);
        String chordType = pickString(config.chordTypes);
        String chordTab = getChordTab(lowString, inversion, chordType, rootId);
        String displayType = chordType.replace("dim", "°").replace("aug", "+");
        String prompt = "Low String: " + lowString.toUpperCase(Locale.US)
                + "\nInversion: " + inversion
                + "\nChord Name: " + root + displayType;
        return new Question(prompt, new ArrayList<>(), chordTab, -1, -1, -1);
    }

    private String getChordTab(String lowString, int inversion, String chordType, int rootId) {
        int[][] formula = Constants.TRIAD_FORMULAS.get(chordType + "_" + lowString + "_" + inversion);
        String lowestNote;
        if (inversion == 0) {
            lowestNote = Constants.CHROMATICS.get(rootId).get(0);
        } else if (inversion == 1 && (chordType.equals("m") || chordType.equals("dim"))) {
            lowestNote = Constants.CHROMATICS.get(mod(rootId + 3, Constants.CHROMATICS.size())).get(0);
        } else if (inversion == 1) {
            lowestNote = Constants.CHROMATICS.get(mod(rootId + 4, Constants.CHROMATICS.size())).get(0);
        } else if (inversion == 2) {
            lowestNote = Constants.CHROMATICS.get(mod(rootId + 7, Constants.CHROMATICS.size())).get(0);
        } else {
            throw new IllegalArgumentException("Unsupported inversion: " + inversion);
        }

        int startingFret = distance(lowString.toUpperCase(Locale.US), lowestNote.toUpperCase(Locale.US));
        for (int[] point : formula) {
            if (startingFret - point[1] < 0) {
                startingFret += 12;
                break;
            }
        }

        List<String> stringChunks = new ArrayList<>();
        int point = 0;
        for (String stringName : new String[]{"E", "A", "D", "G", "B", "E"}) {
            if (stringName.equals(lowString.toUpperCase(Locale.US)) && point < formula.length) {
                stringChunks.add(stringName + " -" + startingFret + "-\n");
                point = 1;
            } else if (point == 0 || point >= formula.length) {
                stringChunks.add(stringName + " ---\n");
            } else {
                int y = formula[point][1];
                stringChunks.add(stringName + " -" + (startingFret - y) + "-\n");
                point += 1;
            }
        }

        StringBuilder chordTab = new StringBuilder();
        for (int i = stringChunks.size() - 1; i >= 0; i--) {
            chordTab.append(stringChunks.get(i));
        }
        return chordTab.toString();
    }

    private int distance(String n1, String n2) {
        int startIndex = -1;
        for (int i = 0; i < Constants.CHROMATICS.size(); i++) {
            if (Constants.CHROMATICS.get(i).contains(n1)) {
                startIndex = i;
                break;
            }
        }

        for (int i = startIndex; i < startIndex + Constants.CHROMATICS.size(); i++) {
            if (Constants.CHROMATICS.get(mod(i, Constants.CHROMATICS.size())).contains(n2)) {
                return i - startIndex;
            }
        }
        throw new IllegalArgumentException("Unknown note distance: " + n1 + " to " + n2);
    }

    private void validateConfig(TheoryConfig config) {
        if (mode == TheoryMode.INTERVALS) {
            if (config.intervals.isEmpty() || config.directions.isEmpty() || config.roots == null) {
                throw new IllegalArgumentException("Malformed interval config.");
            }
            if (config.intervals.size() != config.directions.size()) {
                throw new IllegalArgumentException("Intervals and directions must have the same length.");
            }
            for (String interval : config.intervals) {
                if (!Constants.INTERVALS.containsKey(interval)) {
                    throw new IllegalArgumentException("Unexpected interval: " + interval);
                }
            }
        } else {
            if (config.chordTypes.isEmpty()) {
                throw new IllegalArgumentException("Chord config must include chord types.");
            }
            if (mode == TheoryMode.GUITAR_TRIADS
                    && (config.lowStrings.isEmpty() || config.inversions.isEmpty())) {
                throw new IllegalArgumentException("Guitar triad configs must include low strings and inversions.");
            }
        }
    }

    private List<String> getRoots(List<String> configuredRoots) {
        List<String> possibleNotes = flattenChromatics();
        if (configuredRoots.isEmpty()) {
            return possibleNotes;
        } else if (configuredRoots.size() == 1 && configuredRoots.contains("naturals")) {
            return filterRoots(possibleNotes, false, false);
        } else if (configuredRoots.size() == 1 && configuredRoots.contains("accidentals")) {
            return filterRoots(possibleNotes, true, true);
        } else if (configuredRoots.size() == 1 && configuredRoots.contains("flats")) {
            return filterRoots(possibleNotes, true, false);
        } else if (configuredRoots.size() == 1 && configuredRoots.contains("sharps")) {
            return filterRoots(possibleNotes, false, true);
        }
        return new ArrayList<>(configuredRoots);
    }

    private List<String> filterRoots(List<String> possibleNotes, boolean includeFlats, boolean includeSharps) {
        List<String> filtered = new ArrayList<>();
        for (String note : possibleNotes) {
            boolean isFlat = note.contains("b");
            boolean isSharp = note.contains("#");
            if (!includeFlats && !includeSharps && !isFlat && !isSharp) {
                filtered.add(note);
            } else if (includeFlats && includeSharps && (isFlat || isSharp)) {
                filtered.add(note);
            } else if (includeFlats && isFlat) {
                filtered.add(note);
            } else if (includeSharps && isSharp) {
                filtered.add(note);
            }
        }
        return filtered;
    }

    private List<Integer> findRootIds(List<String> roots) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < Constants.CHROMATICS.size(); i++) {
            for (String note : Constants.CHROMATICS.get(i)) {
                if (roots.contains(note) && !ids.contains(i)) {
                    ids.add(i);
                }
            }
        }
        return ids;
    }

    private String pickMatchingRoot(int rootId) {
        List<String> candidates = new ArrayList<>();
        for (String note : Constants.CHROMATICS.get(rootId)) {
            if (roots.contains(note)) {
                candidates.add(note);
            }
        }
        return pickString(candidates);
    }

    private List<String> flattenChromatics() {
        List<String> all = new ArrayList<>();
        for (List<String> noteNames : Constants.CHROMATICS) {
            all.addAll(noteNames);
        }
        return all;
    }

    private String preferredSpelling(List<String> choices, boolean preferFlat) {
        for (String choice : choices) {
            if (preferFlat && choice.contains("b")) {
                return choice;
            }
            if (!preferFlat && choice.contains("#")) {
                return choice;
            }
        }
        return choices.get(0);
    }

    private int fifthOffset(String chordType) {
        if (chordType.equals("m") || chordType.equals("M")) {
            return 7;
        } else if (chordType.equals("dim")) {
            return 6;
        }
        return 8;
    }

    private int mod(int value, int divisor) {
        return ((value % divisor) + divisor) % divisor;
    }

    private String pickString(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private int pickInteger(List<Integer> values) {
        return values.get(random.nextInt(values.size()));
    }
}
