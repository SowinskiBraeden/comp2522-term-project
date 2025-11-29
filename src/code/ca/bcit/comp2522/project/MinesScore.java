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
 * Score manager for WordGame, writes and reads
 * scores from file, get and validate high-scores,
 * calculate score averages and provides useful
 * toString methods to summarize scores.
 *
 * @author Braeden Sowinski
 * @version 1.0.0
 */
public class MinesScore
{
    private static final String DATE_PATTERN      = "yyyy-MM-dd HH:mm:ss";
    private static final int    SPLIT_VALUE       = 1;

    public static final String DIFFICULTY_EASY    = "easy";
    public static final String DIFFICULTY_MEDIUM  = "medium";
    public static final String DIFFICULTY_HARD    = "hard";

    private final String  dateTimePlayed;
    private final String  difficulty;
    private final boolean randomMode;
    private final int     seconds;

    private static void validateDifficulty(final String difficulty)
    {
        if (difficulty == null)
        {
            throw new IllegalArgumentException("Difficulty cannot be null");
        }

        if (!difficulty.equalsIgnoreCase(DIFFICULTY_EASY) &&
            !difficulty.equalsIgnoreCase(DIFFICULTY_MEDIUM) &&
            !difficulty.equalsIgnoreCase(DIFFICULTY_HARD)
        ) {
            throw new IllegalArgumentException("Invalid difficulty");
        }

    }

    /**
     * appendScoreToFile takes in a Score object
     * and filepath, appends Score values to file
     * accordingly.
     * @param score to append to filepath
     * @param scoreFilePath filepath to append Score to
     */
    public static void appendScoreToFile(
            final MinesScore score,
            final String     scoreFilePath
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
            System.err.println("Failed to write " + scoreFilePath);
        }
    }

    /**
     * readScoresFromFile returns a history of all Scores
     * read from the given filepath.
     * @param scoreFilePath to read scores from
     * @return a list of all Scores read from given filepath
     */
    public static List<MinesScore> readScoresFromFile(final String scoreFilePath)
    {
        final List<MinesScore> scores;

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
                    final String            dateTime;
                    final MinesScore        score;
                    final String            seconds;
                    final String            difficulty;
                    final String            randomMode;

                    formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

                    dateTime   = line.split(": ")[SPLIT_VALUE];
                    seconds    = reader.readLine().split(": ")[SPLIT_VALUE];
                    difficulty = reader.readLine().split(": ")[SPLIT_VALUE];
                    randomMode = reader.readLine().split(": ")[SPLIT_VALUE];

                    reader.readLine(); // Skip over score line

                    score = new MinesScore(
                            LocalDateTime.parse(dateTime, formatter),
                            Integer.parseInt(seconds),
                            difficulty,
                            Boolean.parseBoolean(randomMode)
                    );

                    scores.add(score);
                }
            }

            reader.close();
        }
        catch (final IOException e)
        {
            System.err.println("Failed to open score log file.");
        }

        return scores;
    }

    /**
     * getHighScore from a given List of Scores
     * @param scores list to find high-score from
     * @return high Score from list of scores
     */
    public static MinesScore getHighScore(
        final List<MinesScore> scores,
        final String           difficulty,
        final boolean          randomMode
    ) {
        return scores.stream()
                .filter(s -> s.getDifficulty().equalsIgnoreCase(difficulty))
                .filter(s -> s.getRandomMode() == randomMode)
                .min(Comparator.comparingInt(MinesScore::getSeconds))
                .orElse(null);
    }

    /**
     * isHighScore takes a List of Scores and
     * new Score to check if the new Score is
     * a high-score in the given scores list.
     * @param score to check if is high score
     * @param scores list to compare Score to
     * @return if score is a new high-score in list
     */
    public static boolean isHighScore(
            final MinesScore        score,
            final List<MinesScore>  scores
    ) {
        final MinesScore highScore;

        highScore = MinesScore.getHighScore(
            scores,
            score.getDifficulty(),
            score.getRandomMode()
        );

        return (highScore == null ||
                highScore.getSeconds() > score.getSeconds());
    }

    /**
     * Score constructor saves score dateTime,
     * number of games played for this score,
     * and relevant scores based on number of
     * guesses.
     * @param dateTime the score was recorded
     * @param seconds is the number seconds for that round
     * @param difficulty the game was played in
     * @param randomMode was on or off
     */
    public MinesScore(
            final LocalDateTime dateTime,
            final int           seconds,
            final String        difficulty,
            final boolean       randomMode
    ) {
        final DateTimeFormatter formatter;

        formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        this.dateTimePlayed = dateTime.format(formatter);

        this.seconds    = seconds;
        this.difficulty = difficulty;
        this.randomMode = randomMode;
    }

    /**
     * getDateTimePlayed of Score
     * @return dateTimePlayed as a String
     */
    public String getDateTimePlayed()
    {
        return this.dateTimePlayed;
    }

    /**
     * toString neatly presents this Score
     * in a String format
     * @return formatted String of Score details
     */
    @Override
    public String toString()
    {
        final StringBuilder log;

        log = new StringBuilder();

        log.append("Date and Time: ");
        log.append(this.dateTimePlayed);
        log.append("\n");
        log.append("Seconds: ");
        log.append(this.seconds);
        log.append("\n");
        log.append("Difficulty: ");
        log.append(this.difficulty);
        log.append("\n");
        log.append("Random Mode: ");
        log.append(this.randomMode);
        log.append("\n");

        return log.toString();
    }

    /**
     * getScore returns total seconds of this Score
     * @return total seconds score
     */
    public int getSeconds()
    {
        return this.seconds;
    }

    /**
     * getDifficulty of this score
     * @return difficulty of this score
     */
    public String getDifficulty()
    {
        return this.difficulty;
    }

    /**
     * getRandomMode status of this score
     * @return if this score was in random mode
     */
    public boolean getRandomMode()
    {
        return this.randomMode;
    }
}
