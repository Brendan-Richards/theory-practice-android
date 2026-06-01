package com.example.theorypractice;

import java.util.List;

final class Question {
    final String prompt;
    final List<String> acceptedAnswers;
    final String revealAnswer;
    final int intervalAnswerPitchClass;
    final int chordThirdPitchClass;
    final int chordFifthPitchClass;

    Question(
            String prompt,
            List<String> acceptedAnswers,
            String revealAnswer,
            int intervalAnswerPitchClass,
            int chordThirdPitchClass,
            int chordFifthPitchClass
    ) {
        this.prompt = prompt;
        this.acceptedAnswers = acceptedAnswers;
        this.revealAnswer = revealAnswer;
        this.intervalAnswerPitchClass = intervalAnswerPitchClass;
        this.chordThirdPitchClass = chordThirdPitchClass;
        this.chordFifthPitchClass = chordFifthPitchClass;
    }
}
