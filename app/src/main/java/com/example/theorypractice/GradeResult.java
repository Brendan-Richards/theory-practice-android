package com.example.theorypractice;

final class GradeResult {
    final int correct;
    final int total;
    final String feedback;

    GradeResult(int correct, int total, String feedback) {
        this.correct = correct;
        this.total = total;
        this.feedback = feedback;
    }
}
