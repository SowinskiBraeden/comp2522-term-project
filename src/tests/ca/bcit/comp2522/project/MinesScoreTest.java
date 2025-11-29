package ca.bcit.comp2522.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MinesScore test to test score file appending,
 * reading, high-score comparisons
 *
 * @author Braeden Sowinski
 * @version 1.0.0
 */
public class MinesScoreTest
{
    @Test
    public void testAppendScoreThenRead(@TempDir Path tempDir)
    {
        final File file;
        file = tempDir.resolve("scores.txt").toFile();

        final LocalDateTime now;
        now = LocalDateTime.now();

        final MinesScore score;
        score = new MinesScore(now, 45, MinesScore.DIFFICULTY_EASY, true);

        MinesScore.appendScoreToFile(score, file.getAbsolutePath());

        final List<MinesScore> scores;
        scores = MinesScore.readScoresFromFile(file.getAbsolutePath());

        assertEquals(1, scores.size());
        assertEquals(45, scores.get(0).getSeconds());
        assertEquals(MinesScore.DIFFICULTY_EASY, scores.get(0).getDifficulty());
        assertTrue(scores.get(0).getRandomMode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "easy", "medium", "hard" })
    public void testDifficultyValidationAcceptsValidValues(final String difficulty)
    {
        final LocalDateTime now;
        now = LocalDateTime.now();

        final MinesScore score;
        score = new MinesScore(now, 10, difficulty, false);

        assertEquals(difficulty, score.getDifficulty());
    }

    @Test
    public void testHighScoreComparisonCorrect()
    {
        final LocalDateTime now;
        now = LocalDateTime.now();

        final MinesScore best;
        best = new MinesScore(now, 20, MinesScore.DIFFICULTY_HARD, false);

        final MinesScore challenger;
        challenger = new MinesScore(now, 10, MinesScore.DIFFICULTY_HARD, false);

        final List<MinesScore> scores;
        scores = List.of(best);

        assertTrue(MinesScore.isHighScore(challenger, scores));
        assertFalse(MinesScore.isHighScore(best, scores));
    }

    @Test
    public void testReadScoresHandlesBlankFile(@TempDir Path tempDir)
    {
        final File file;
        file = tempDir.resolve("empty.txt").toFile();

        final List<MinesScore> result;
        result = MinesScore.readScoresFromFile(file.getAbsolutePath());

        assertTrue(result.isEmpty());
    }
}
