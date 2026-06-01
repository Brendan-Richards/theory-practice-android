package com.example.theorypractice;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(32, 33, 36);
    private static final int SURFACE = Color.rgb(47, 49, 54);
    private static final int SURFACE_STROKE = Color.rgb(86, 89, 94);
    private static final int TEXT = Color.rgb(245, 245, 245);
    private static final int MUTED_TEXT = Color.rgb(190, 190, 190);
    private static final int ACCENT = Color.rgb(76, 175, 80);

    private TheoryTeacher teacher;
    private TheoryMode currentMode = TheoryMode.INTERVALS;
    private Question currentQuestion;
    private boolean suppressConfigCallback = false;

    private Spinner modeSpinner;
    private Spinner configSpinner;
    private TextView questionView;
    private LinearLayout answerArea;
    private Button submitButton;
    private TextView correctView;
    private TextView totalView;
    private TextView percentageView;
    private int selectedThirdPitchClass = -1;
    private int selectedFifthPitchClass = -1;
    private final List<Button> thirdButtons = new ArrayList<>();
    private final List<Button> fifthButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);

        teacher = new TheoryTeacher(new ConfigRepository(getAssets()));
        setContentView(createContentView());
        setupModeSpinner();
    }

    private View createContentView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    dp(20),
                    dp(32) + insets.getSystemWindowInsetTop(),
                    dp(20),
                    dp(40) + insets.getSystemWindowInsetBottom()
            );
            return insets;
        });
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(this);
        title.setText("Guitar Theory Trainer");
        title.setTextColor(TEXT);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        modeSpinner = addLabeledSpinner(root, "Theory Mode");
        configSpinner = addLabeledSpinner(root, "Config");

        questionView = new TextView(this);
        questionView.setTextColor(TEXT);
        questionView.setTextSize(22);
        questionView.setGravity(Gravity.CENTER);
        questionView.setMinHeight(dp(150));
        questionView.setPadding(dp(18), dp(18), dp(18), dp(18));
        questionView.setBackground(surfaceBackground(dp(8), SURFACE, SURFACE_STROKE));
        LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        questionParams.setMargins(0, dp(22), 0, dp(14));
        root.addView(questionView, questionParams);

        LinearLayout scoreRow = new LinearLayout(this);
        scoreRow.setOrientation(LinearLayout.HORIZONTAL);
        scoreRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams scoreParams = matchWrap();
        scoreParams.setMargins(0, 0, 0, dp(16));
        root.addView(scoreRow, scoreParams);

        correctView = scoreText("Correct: 0");
        totalView = scoreText("Total: 0");
        percentageView = scoreText("0% Correct");
        scoreRow.addView(correctView, weightedScoreParams());
        scoreRow.addView(totalView, weightedScoreParams());
        scoreRow.addView(percentageView, weightedScoreParams());

        answerArea = new LinearLayout(this);
        answerArea.setOrientation(LinearLayout.VERTICAL);
        root.addView(answerArea, matchWrap());

        submitButton = new Button(this);
        submitButton.setText("Submit");
        submitButton.setTextColor(Color.WHITE);
        submitButton.setTextSize(16);
        submitButton.setAllCaps(false);
        submitButton.setBackground(surfaceBackground(dp(6), ACCENT, ACCENT));
        submitButton.setOnClickListener(view -> submitAnswer());
        LinearLayout.LayoutParams buttonParams = fixedHeight(dp(52));
        buttonParams.setMargins(0, dp(12), 0, dp(18));
        root.addView(submitButton, buttonParams);

        return scrollView;
    }

    private Spinner addLabeledSpinner(LinearLayout root, String labelText) {
        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextColor(MUTED_TEXT);
        label.setTextSize(14);
        LinearLayout.LayoutParams labelParams = matchWrap();
        labelParams.setMargins(0, dp(18), 0, dp(6));
        root.addView(label, labelParams);

        Spinner spinner = new Spinner(this);
        spinner.setBackground(surfaceBackground(dp(6), Color.rgb(67, 70, 75), SURFACE_STROKE));
        spinner.setPadding(dp(8), 0, dp(8), 0);
        root.addView(spinner, fixedHeight(dp(52)));
        return spinner;
    }

    private void setupModeSpinner() {
        List<String> modeNames = Arrays.asList(
                TheoryMode.INTERVALS.displayName,
                TheoryMode.GUITAR_TRIADS.displayName,
                TheoryMode.CHORD_SPELLING.displayName
        );
        modeSpinner.setAdapter(createAdapter(modeNames));
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TheoryMode selectedMode = TheoryMode.fromDisplayName(modeNames.get(position));
                if (selectedMode != currentMode || currentQuestion == null) {
                    changeMode(selectedMode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        configSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!suppressConfigCallback && parent.getItemAtPosition(position) != null) {
                    loadConfigAndQuestion(parent.getItemAtPosition(position).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        changeMode(currentMode);
    }

    private void changeMode(TheoryMode mode) {
        currentMode = mode;
        teacher.setTheoryMode(mode);
        updateScore();

        try {
            List<String> configNames = teacher.getConfigNames(mode);
            suppressConfigCallback = true;
            configSpinner.setAdapter(createAdapter(configNames));
            suppressConfigCallback = false;

            if (!configNames.isEmpty()) {
                configSpinner.setSelection(0);
                loadConfigAndQuestion(configNames.get(0));
            } else {
                showBlockingError("No configs found for " + mode.displayName + ".");
            }
        } catch (IOException exception) {
            suppressConfigCallback = false;
            showBlockingError("Could not read configs: " + exception.getMessage());
        }
    }

    private void loadConfigAndQuestion(String configName) {
        try {
            teacher.loadConfig(configName);
            loadQuestion();
            updateScore();
        } catch (Exception exception) {
            showBlockingError("Could not load " + configName + ": " + exception.getMessage());
        }
    }

    private void loadQuestion() {
        submitButton.setEnabled(true);
        currentQuestion = teacher.generateQuestion();
        questionView.setText(currentQuestion.prompt);
        renderAnswerControls();
    }

    private void submitAnswer() {
        if (currentQuestion == null) {
            return;
        }

        GradeResult result;
        if (currentMode == TheoryMode.GUITAR_TRIADS) {
            result = teacher.gradeTouchAnswer(true, currentQuestion);
        } else {
            if (selectedThirdPitchClass < 0 || selectedFifthPitchClass < 0) {
                Toast.makeText(this, "Choose a third and a fifth.", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean isCorrect = selectedThirdPitchClass == currentQuestion.chordThirdPitchClass
                    && selectedFifthPitchClass == currentQuestion.chordFifthPitchClass;
            result = teacher.gradeTouchAnswer(isCorrect, currentQuestion);
        }

        updateScore(result);
        showFeedback(result.feedback);
    }

    private void renderAnswerControls() {
        answerArea.removeAllViews();
        selectedThirdPitchClass = -1;
        selectedFifthPitchClass = -1;
        thirdButtons.clear();
        fifthButtons.clear();

        if (currentMode == TheoryMode.INTERVALS) {
            answerArea.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
            answerArea.addView(createPitchGrid((pitchClass, button) -> {
                boolean isCorrect = pitchClass == currentQuestion.intervalAnswerPitchClass;
                GradeResult result = teacher.gradeTouchAnswer(isCorrect, currentQuestion);
                updateScore(result);
                showFeedback(result.feedback);
            }));
        } else if (currentMode == TheoryMode.CHORD_SPELLING) {
            answerArea.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
            submitButton.setText("Submit spelling");
            addPitchSelectionSection("Third", true);
            addPitchSelectionSection("Fifth", false);
        } else {
            answerArea.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
            submitButton.setText("Reveal answer");
        }
    }

    private void addPitchSelectionSection(String labelText, boolean isThird) {
        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextColor(MUTED_TEXT);
        label.setTextSize(14);
        LinearLayout.LayoutParams labelParams = matchWrap();
        labelParams.setMargins(0, isThird ? 0 : dp(14), 0, dp(6));
        answerArea.addView(label, labelParams);

        answerArea.addView(createPitchGrid((pitchClass, button) -> {
            if (isThird) {
                selectedThirdPitchClass = pitchClass;
                updateSelectionButtons(thirdButtons, pitchClass);
            } else {
                selectedFifthPitchClass = pitchClass;
                updateSelectionButtons(fifthButtons, pitchClass);
            }
        }, isThird ? thirdButtons : fifthButtons));
    }

    private GridLayout createPitchGrid(PitchClickListener listener) {
        return createPitchGrid(listener, null);
    }

    private GridLayout createPitchGrid(PitchClickListener listener, List<Button> trackedButtons) {
        GridLayout grid = new GridLayout(this);
        grid.setLayoutParams(matchWrap());
        grid.setColumnCount(4);
        grid.setUseDefaultMargins(false);

        for (int i = 0; i < Constants.CHROMATICS.size(); i++) {
            int pitchClass = i;
            Button button = createPitchButton(pitchLabel(i));
            button.setOnClickListener(view -> listener.onPitchClick(pitchClass, button));
            if (trackedButtons != null) {
                trackedButtons.add(button);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dp(52);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            grid.addView(button, params);
        }
        return grid;
    }

    private Button createPitchButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(TEXT);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setBackground(surfaceBackground(dp(6), Color.rgb(67, 70, 75), SURFACE_STROKE));
        return button;
    }

    private void updateSelectionButtons(List<Button> buttons, int selectedPitchClass) {
        for (int i = 0; i < buttons.size(); i++) {
            int fill = i == selectedPitchClass ? ACCENT : Color.rgb(67, 70, 75);
            int stroke = i == selectedPitchClass ? ACCENT : SURFACE_STROKE;
            buttons.get(i).setBackground(surfaceBackground(dp(6), fill, stroke));
        }
    }

    private String pitchLabel(int pitchClass) {
        List<String> labels = Constants.CHROMATICS.get(pitchClass);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                builder.append("/");
            }
            builder.append(labels.get(i));
        }
        return builder.toString();
    }

    private void showFeedback(String feedback) {
        TextView messageView = new TextView(this);
        messageView.setText(feedback);
        messageView.setTextColor(TEXT);
        messageView.setTextSize(18);
        messageView.setPadding(dp(22), dp(16), dp(22), dp(4));
        if (currentMode == TheoryMode.GUITAR_TRIADS) {
            messageView.setTypeface(Typeface.MONOSPACE);
        }

        new AlertDialog.Builder(this)
                .setView(messageView)
                .setPositiveButton("Next", (dialog, which) -> loadQuestion())
                .setOnCancelListener(dialog -> loadQuestion())
                .show();
    }

    private void showBlockingError(String message) {
        questionView.setText(message);
        submitButton.setEnabled(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateScore() {
        updateScore(new GradeResult(teacher.getCorrect(), teacher.getTotal(), ""));
    }

    private void updateScore(GradeResult result) {
        correctView.setText(String.format(Locale.US, "Correct: %d", result.correct));
        totalView.setText(String.format(Locale.US, "Total: %d", result.total));
        int percent = result.total == 0 ? 0 : Math.round((result.correct * 100f) / result.total);
        percentageView.setText(String.format(Locale.US, "%d%% Correct", percent));
    }

    private ArrayAdapter<String> createAdapter(List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                values
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private TextView scoreText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(MUTED_TEXT);
        view.setTextSize(14);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private LinearLayout.LayoutParams weightedScoreParams() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams fixedHeight(int height) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
    }

    private GradientDrawable surfaceBackground(int radius, int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface PitchClickListener {
        void onPitchClick(int pitchClass, Button button);
    }
}
