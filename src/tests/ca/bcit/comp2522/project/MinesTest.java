package ca.bcit.comp2522.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MinesTest game logic, field array logic such as
 * popFieldVoid, validates correct cells are revealed,
 * flag cycling and tracking, invalid moves such as
 * revealing a flagged cell, revealing mine triggers
 * loss, etc.
 *
 * @author Braeden Sowinski
 * @version 1.0.0
 */
public class MinesTest
{
    private Mines mines;

    @BeforeEach
    public void setUp()
    {
        final int width;
        final int height;
        final int totalMines;
        final boolean randomMode;

        width = 3;
        height = 3;
        totalMines = 1;
        randomMode = false;

        mines = new Mines(width, height, totalMines, randomMode);
    }

    // --- Reflection helpers -------------------------------------------------

    private int[] getFieldArray(final Mines game) throws Exception
    {
        final Field fieldField;
        fieldField = Mines.class.getDeclaredField("field");
        fieldField.setAccessible(true);

        final Object value;
        value = fieldField.get(game);

        return (int[]) value;
    }

    private void setFieldArray(final Mines game, final int[] newField) throws Exception
    {
        final Field fieldField;
        fieldField = Mines.class.getDeclaredField("field");
        fieldField.setAccessible(true);
        fieldField.set(game, newField);
    }

    // --- Basic tests ---------------------------------------------------------

    @Test
    public void testFieldArraySizeMatchesWidthTimesHeight() throws Exception
    {
        final int[] fieldArray;
        fieldArray = getFieldArray(mines);

        final int expectedSize;
        expectedSize = mines.getWidth() * mines.getHeight();

        assertEquals(expectedSize, fieldArray.length);
    }

    @Test
    public void testToggleFlagCyclesCorrectly()
    {
        final int index;
        index = 0;

        final int result1;
        result1 = mines.toggleFlag(index);
        assertEquals(Mines.FLAG, result1);

        final int result2;
        result2 = mines.toggleFlag(index);
        assertEquals(Mines.FLAG_QUESTION, result2);

        final int result3;
        result3 = mines.toggleFlag(index);
        assertEquals(Mines.NO_FLAG, result3);
    }

    @Test
    public void testRevealThrowsOnFlaggedCell() throws Exception
    {
        final int index;
        index = 0;

        mines.toggleFlag(index);

        assertThrows(InvalidMoveException.class, () -> mines.reveal(index));
    }

    @Test
    public void testRevealMineReturnsTrue() throws Exception
    {
        final int size;
        size = mines.getWidth() * mines.getHeight();

        boolean foundMine;
        foundMine = false;

        for (int i = 0; i < size; i++)
        {
            if (mines.isMine(i))
            {
                final boolean hitMine;
                hitMine = mines.reveal(i);
                assertTrue(hitMine);
                foundMine = true;
                break;
            }
        }

        assertTrue(foundMine);
    }

    @Test
    public void testHasWonDetectsWinState() throws Exception
    {
        final int size;
        size = mines.getWidth() * mines.getHeight();

        for (int i = 0; i < size; i++)
        {
            if (!mines.isMine(i))
            {
                mines.reveal(i);
            }
        }

        assertTrue(mines.hasWon());
    }

    // --- Advanced tests (using reflection to control the board) -------------

    @Test
    public void testPopVoidExpansionRevealsConnectedRegion() throws Exception
    {
        /*
            Board layout (4x4), indices:
            0  1  2  3
            4  5  6  7
            8  9 10 11
           12 13 14 15

            Values:
            0 0 1 M
            0 0 1 1
            0 0 0 0
            M 1 0 0

            When revealing index 0, all connected 0s should be revealed.
         */

        final int width;
        final int height;
        final int totalMines;
        final boolean randomMode;

        width = 4;
        height = 4;
        totalMines = 0;
        randomMode = false;

        final Mines board;
        board = new Mines(width, height, totalMines, randomMode);

        final int[] field;
        field = new int[]{
                0, 0, 1, -1,
                0, 0, 1,  1,
                0, 0, 0,  0,
                -1, 1, 0,  0
        };

        setFieldArray(board, field);

        final int revealIndex;
        revealIndex = 0;

        board.reveal(revealIndex);

        // verify zero region auto-expands
        assertTrue(board.isRevealed(0));
        assertTrue(board.isRevealed(1));
        assertTrue(board.isRevealed(4));
        assertTrue(board.isRevealed(5));
        assertTrue(board.isRevealed(6));
        assertTrue(board.isRevealed(9));
        assertTrue(board.isRevealed(10));
        assertTrue(board.isRevealed(14));
    }

    @Test
    public void testRandomizeRemainingKeepsFlaggedMines() throws Exception
    {
        final int width;
        final int height;
        final int totalMines;
        final boolean randomMode;

        width = 4;
        height = 4;
        totalMines = 0;
        randomMode = true; // must be true for randomizeRemaining to do anything

        final Mines board;
        board = new Mines(width, height, totalMines, randomMode);

        final int[] field;
        field = new int[]{
                -1, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, -1, 0,
                0, 0, 0, 0
        };

        setFieldArray(board, field);

        final int flaggedMineIndex;
        flaggedMineIndex = 0;

        board.toggleFlag(flaggedMineIndex);

        board.randomizeRemaining();

        // flagged mine should still be a mine
        assertTrue(board.isMine(flaggedMineIndex));
    }

    @Test
    public void testHasWonTrueWhenAllNonMinesRevealedOnCustomBoard() throws Exception
    {
        final int width;
        final int height;
        final int totalMines;
        final boolean randomMode;

        width = 3;
        height = 3;
        totalMines = 1;
        randomMode = false;

        final Mines board;
        board = new Mines(width, height, totalMines, randomMode);

        final int[] field;
        field = new int[]{
                -1, 1, 0,
                1, 1, 0,
                0, 0, 0
        };

        setFieldArray(board, field);

        for (int i = 0; i < 9; i++)
        {
            if (!board.isMine(i))
            {
                board.reveal(i);
            }
        }

        assertTrue(board.hasWon());
    }

    @Test
    public void testRevealMineReturnsTrueOnCustomBoard() throws Exception
    {
        final int width;
        final int height;
        final int totalMines;
        final boolean randomMode;

        width = 3;
        height = 3;
        totalMines = 1;
        randomMode = false;

        final Mines board;
        board = new Mines(width, height, totalMines, randomMode);

        final int[] field;
        field = new int[]{
                0, -1, 0,
                0,  0, 0,
                0,  0, 0
        };

        setFieldArray(board, field);

        final int mineIndex;
        mineIndex = 1;

        final boolean hitMine;
        hitMine = board.reveal(mineIndex);

        assertTrue(hitMine);
    }
}
