import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class NumberGuessingGame extends JFrame {
    // Game state
    private final Random rng = new Random();
    private int target;
    private int min = 1;
    private int max = 100;
    private int attempts = 0;
    private int maxAttempts = 7;

    // Timer state
    private int timeLimitSeconds = 45;
    private int remainingSeconds = timeLimitSeconds;
    private Timer countdown;

    // UI components
    private JLabel titleLabel;
    private JTextField guessField;
    private JButton guessButton;
    private JButton newGameButton;
    private JButton giveUpButton;
    private JLabel feedbackLabel;
    private JLabel attemptsLabel;
    private JLabel timerLabel;
    private JLabel rangeLabel;
    private JComboBox<String> difficultyBox;
    private JLabel bestTimeLabel;

    // Best time (shortest win time). -1 means no record yet
    private int bestTimeSeconds = -1;

    public NumberGuessingGame() {
        super("Number Guessing Game — Swing");
        buildUI();
        setupTimer();
        startNewGame();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 360);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        // Fonts
        Font h1 = new Font("Segoe UI", Font.BOLD, 22);
        Font body = new Font("Segoe UI", Font.PLAIN, 14);

        // Panels
        setLayout(new BorderLayout(12, 12));
        JPanel top = new JPanel(new BorderLayout());
        JPanel center = new JPanel();
        JPanel bottom = new JPanel(new GridLayout(2, 1, 8, 8));

        // Title
        titleLabel = new JLabel("Guess the Number!", SwingConstants.CENTER);
        titleLabel.setFont(h1);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        top.add(titleLabel, BorderLayout.CENTER);

        // Difficulty selector
        String[] diffs = {"Easy (1-50, 60s, 10 tries)", "Medium (1-100, 45s, 7 tries)", "Hard (1-500, 60s, 9 tries)", "Insane (1-1000, 75s, 10 tries)"};
        difficultyBox = new JComboBox<>(diffs);
        difficultyBox.setSelectedIndex(1); // Medium by default
        difficultyBox.setToolTipText("Change difficulty then press New Game");
        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        diffPanel.add(new JLabel("Difficulty:"));
        diffPanel.add(difficultyBox);
        top.add(diffPanel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        // Center input area
        center.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        rangeLabel = new JLabel("I am thinking of a number between 1 and 100.");
        rangeLabel.setFont(body);
        center.add(rangeLabel, gc);

        gc.gridy = 1; gc.gridwidth = 1;
        guessField = new JTextField(10);
        guessField.setFont(body);
        center.add(guessField, gc);

        gc.gridx = 1;
        guessButton = new JButton("Guess");
        center.add(guessButton, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        feedbackLabel = new JLabel("Type your guess and press Enter.", SwingConstants.CENTER);
        feedbackLabel.setFont(body);
        center.add(feedbackLabel, gc);

        add(center, BorderLayout.CENTER);

        // Bottom status + controls
        JPanel statusPanel = new JPanel(new GridLayout(1, 3));
        attemptsLabel = new JLabel("Attempts: 0 / 7", SwingConstants.CENTER);
        timerLabel = new JLabel("Time: 45s", SwingConstants.CENTER);
        bestTimeLabel = new JLabel("Best: —", SwingConstants.CENTER);
        statusPanel.add(attemptsLabel);
        statusPanel.add(timerLabel);
        statusPanel.add(bestTimeLabel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        newGameButton = new JButton("New Game");
        giveUpButton = new JButton("Give Up");
        buttonsPanel.add(newGameButton);
        buttonsPanel.add(giveUpButton);

        bottom.add(statusPanel);
        bottom.add(buttonsPanel);
        add(bottom, BorderLayout.SOUTH);

        // Actions
        guessButton.addActionListener(e -> handleGuess());
        guessField.addActionListener(e -> handleGuess()); // Enter in field
        newGameButton.addActionListener(e -> {
            applyDifficulty();
            startNewGame();
        });
        giveUpButton.addActionListener(e -> endGame(false, true));

        // Improve UX: select all text when focused
        guessField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { SwingUtilities.invokeLater(guessField::selectAll); }
        });

        // Keyboard shortcut: Ctrl+N for new game
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newGame");
        getRootPane().getActionMap().put("newGame", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { newGameButton.doClick(); }
        });
    }

    private void setupTimer() {
        countdown = new Timer(1000, e -> {
            remainingSeconds--;
            timerLabel.setText("Time: " + remainingSeconds + "s");
            if (remainingSeconds <= 0) {
                endGame(false, false);
            }
        });
        countdown.setInitialDelay(0);
    }

    private void applyDifficulty() {
        switch (difficultyBox.getSelectedIndex()) {
            case 0: // Easy
                min = 1; max = 50; timeLimitSeconds = 60; maxAttempts = 10; break;
            case 1: // Medium
                min = 1; max = 100; timeLimitSeconds = 45; maxAttempts = 7; break;
            case 2: // Hard
                min = 1; max = 500; timeLimitSeconds = 60; maxAttempts = 9; break;
            case 3: // Insane
                min = 1; max = 1000; timeLimitSeconds = 75; maxAttempts = 10; break;
            default:
                min = 1; max = 100; timeLimitSeconds = 45; maxAttempts = 7;
        }
    }

    private void startNewGame() {
        target = rng.nextInt(max - min + 1) + min;
        attempts = 0;
        remainingSeconds = timeLimitSeconds;
        attemptsLabel.setText("Attempts: 0 / " + maxAttempts);
        timerLabel.setText("Time: " + remainingSeconds + "s");
        rangeLabel.setText("I am thinking of a number between " + min + " and " + max + ".");
        feedbackLabel.setText("Type your guess and press Enter.");
        guessField.setEnabled(true);
        guessButton.setEnabled(true);
        giveUpButton.setEnabled(true);
        guessField.setText("");
        guessField.requestFocusInWindow();
        if (countdown.isRunning()) countdown.stop();
        countdown.start();
    }

    private void handleGuess() {
        String text = guessField.getText().trim();
        int guess;
        try {
            guess = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            flashMessage("Please enter a valid whole number.");
            return;
        }
        if (guess < min || guess > max) {
            flashMessage("Out of range! Stay between " + min + " and " + max + ".");
            return;
        }

        attempts++;
        attemptsLabel.setText("Attempts: " + attempts + " / " + maxAttempts);

        if (guess == target) {
            endGame(true, false);
            return;
        }

        if (attempts >= maxAttempts) {
            endGame(false, false);
            return;
        }

        if (guess < target) {
            feedbackLabel.setText("Too low! Try a higher number.");
        } else {
            feedbackLabel.setText("Too high! Try a lower number.");
        }
        guessField.selectAll();
        guessField.requestFocusInWindow();
    }

    private void endGame(boolean win, boolean gaveUp) {
        if (countdown.isRunning()) countdown.stop();
        guessField.setEnabled(false);
        guessButton.setEnabled(false);
        giveUpButton.setEnabled(false);

        // Compute once so it's visible anywhere in this method
        int timeTaken = timeLimitSeconds - remainingSeconds;

        if (win) {
            feedbackLabel.setText("Correct! The number was " + target + ". You won in " + attempts +
                    (attempts == 1 ? " try" : " tries") + " and " + timeTaken + "s.");
            // Update best time (lower is better)
            if (bestTimeSeconds == -1 || timeTaken < bestTimeSeconds) {
                bestTimeSeconds = timeTaken;
                bestTimeLabel.setText("Best: " + bestTimeSeconds + "s");
                JOptionPane.showMessageDialog(this, "New best time: " + bestTimeSeconds + " seconds!", "Record", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            if (gaveUp) {
                feedbackLabel.setText("You gave up! The number was " + target + ".");
            } else if (remainingSeconds <= 0) {
                feedbackLabel.setText("Time's up! The number was " + target + ".");
            } else {
                feedbackLabel.setText("Out of tries! The number was " + target + ".");
            }
        }
    }

    private void flashMessage(String msg) {
        // Briefly show a dialog to get the user's attention
        JOptionPane.showMessageDialog(this, msg, "Invalid Input", JOptionPane.WARNING_MESSAGE);
        guessField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        // Optional: set system look & feel
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(NumberGuessingGame::new);
    }
}
