package ca.bcit.comp2522.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Score manager for the word game.
 *
 * @author Braeden Sowinski
 * @version 1.0.0
 */
public class Score
{
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final int    FIRST_GUESS_POINTS = 2;
    private static final int    SECOND_GUESS_POINTS = 1;
    private static final int    SPLIT_VALUE = 1;

    private final String dateTimePlayed;
    private final int    numGamesPlayed;
    private final int    numCorrectFirstAttempt;
    private final int    numCorrectSecondAttempt;
    private final int    numIncorrectTwoAttempts;
    private final int    score;

    public static void appendScoreToFile(
        final Score score,
        final String scoreFilePath
    ) {
        try
        {
            final BufferedWriter writer;

            writer = new BufferedWriter(new FileWriter(scoreFilePath, true));

            writer.write(score.toString());
            writer.newLine();

            writer.close();
        }
        catch (final IOException e)
        {
            System.err.println("Failed to write score.txt");
        }
    }

    public static List<Score> readScoresFromFile(final String scoreFilePath)
    {
        final List<Score> scores;

        scores = new ArrayList<>();

        try
        {
            final BufferedReader reader;

            reader = new BufferedReader(new FileReader(scoreFilePath));

            String line;
            while ((line = reader.readLine()) != null)
            {

                if (!line.isBlank() && line.contains("Date and Time: "))
                {
                    final DateTimeFormatter formatter;
                    final String dateTime;
                    final Score score;
                    final String games;
                    final String correctFirst;
                    final String correctSecond;
                    final String incorrect;

                    formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

                    dateTime = line.split(": ")[SPLIT_VALUE];
                    System.out.println("debug " + dateTime);
                    games = reader.readLine().split(": ")[SPLIT_VALUE];
                    correctFirst = reader.readLine().split(": ")[SPLIT_VALUE];
                    correctSecond = reader.readLine().split(": ")[SPLIT_VALUE];
                    incorrect = reader.readLine().split(": ")[SPLIT_VALUE];
                    reader.readLine(); // Skip over score line

                    score = new Score(
                            LocalDateTime.parse(dateTime, formatter),
                            Integer.parseInt(games),
                            Integer.parseInt(correctFirst),
                            Integer.parseInt(correctSecond),
                            Integer.parseInt(incorrect)
                    );

                    scores.add(score);
                }
            }

            reader.close();
        } catch (final IOException e)
        {
            System.err.println("Failed to open score log file.");
        }

        return scores;
    }

    public static Score getHighScore(final List<Score> scores)
    {
        return scores.stream()
                .min(Comparator.comparingInt(Score::getScore))
                .orElse(null);
    }

    public static boolean isHighScore(
            final Score score,
            final List<Score> scores
    ) {
        final Score highScore;

        highScore = Score.getHighScore(scores);

        return (highScore == null ||
                highScore.getScore() < score.getScore());
    }

    public Score(
        final LocalDateTime dateTime,
        final int numGamesPlayed,
        final int numCorrectFirstAttempt,
        final int numCorrectSecondAttempt,
        final int numIncorrectTwoAttempts
    ) {
        final DateTimeFormatter formatter;

        formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        this.dateTimePlayed = dateTime.format(formatter);

        this.numGamesPlayed          = numGamesPlayed;
        this.numCorrectFirstAttempt  = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;

        this.score = this.numCorrectFirstAttempt * FIRST_GUESS_POINTS +
                     this.numCorrectSecondAttempt * SECOND_GUESS_POINTS;
    }

    public String getDateTimePlayed()
    {
        return this.dateTimePlayed;
    }

    @Override
    public String toString()
    {
        final StringBuilder log;

        log = new StringBuilder();

        log.append("Date and Time: ");
        log.append(this.dateTimePlayed);
        log.append("\n");
        log.append("Games Played: ");
        log.append(this.numGamesPlayed);
        log.append("\n");
        log.append("Correct First Attempts: ");
        log.append(this.numCorrectFirstAttempt);
        log.append("\n");
        log.append("Correct Second Attempts: ");
        log.append(this.numCorrectSecondAttempt);
        log.append("\n");
        log.append("Incorrect Attempts: ");
        log.append(this.numIncorrectTwoAttempts);
        log.append("\n");
        log.append("Score: ");
        log.append(this.score);
        log.append(" points\n");

        return log.toString();
    }

    public int getScore()
    {
        return this.score;
    }

    public float calculateAverage()
    {
        return (float) this.score / (float) this.numGamesPlayed;
    }
}
