package ca.bcit.comp2522.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Mines class holds all relevant game logic
 * for minesweeper, generating field, tracking
 * game board (field), tracks cells revealed,
 * cells flagged and flagged status, randomizing
 * board if needed and provide getters for cells.
 *
 * @author Braeden Sowinski
 * @version 1.0.0
 */
public class Mines extends GameBoard
{
    private static final int MINE               = -1;
    private static final int NO_MINE            = 0;

    private static final int MIN_OFFSET         = -1;
    private static final int MAX_OFFSET         = 1;
    private static final int SELF_OFFSET        = 0;
    private static final int FIRST_ROW          = 0;
    private static final int FIRST_COL          = 0;

    public static final int NO_FLAG            = 0;
    public static final int FLAG               = 1;
    public static final int FLAG_QUESTION      = 2;

    private static final int INITIAL_BEST_SCORE = Integer.MAX_VALUE;

    private static final String SCORE_FILE_NAME = "minesweeper-score.txt";

    private static final Random RANDOM_GENERATOR;

    static
    {
        RANDOM_GENERATOR = new Random();
    }

    private final int     totalMines;
    private final boolean randomMode;

    private int[]     field;
    private boolean[] revealed;
    private int[]     flagged;

    /**
     * Mines constructor generates minefield board
     * with a given width, height and number of mines.
     * @param width of minefield
     * @param height of minefield
     * @param mines to place in minefield
     * @param randomMode to enable randomizing the field
     */
    public Mines(
        final int width,
        final int height,
        final int mines,
        final boolean randomMode
    ) {
        super(width, height);
        this.totalMines = mines;
        this.randomMode = randomMode;
        reset();
    }

    /**
     * isRandomMode checks if the mode is random or not
     * @return random mode value
     */
    public boolean isRandomMode()
    {
        return this.randomMode;
    }

    /**
     * reset game board, refreshes field,
     * revealed, and flagged arrays to
     * default values of total number of cells
     * then generates field values
     */
    @Override
    public void reset()
    {
        final int totalCells;

        totalCells = this.width * this.height;

        this.field    = new int[totalCells];
        this.revealed = new boolean[totalCells];
        this.flagged  = new int[totalCells];

        generateField();
    }

    /**
     * generateField places number of mines randomly
     * within the minefield, then for each cell count
     * number of neighboring mines and set values in
     * the field
     */
    private void generateField()
    {
        int placedMines;
        final int totalCells;

        placedMines = NO_MINE;
        totalCells  = this.width * this.height;

        while (placedMines < this.totalMines)
        {
            final int index;

            index = RANDOM_GENERATOR.nextInt(totalCells);

            if (this.field[index] != MINE)
            {
                this.field[index] = MINE;
                placedMines++;
            }
        }

        countNeighboringMines();
    }

    /**
     * countNeighboringMines iterates over the field
     * and for each cell of the field, checks all 8
     * neighbors, top, top right, right, bottom right,
     * and so on to count the total number of mines
     * surround that cell, then updates the mine count
     * for that cell in the field.
     */
    private void countNeighboringMines()
    {
        for (int i = 0; i < this.field.length; i++)
        {
            if (this.field[i] == MINE)
            {
                continue;
            }

            final int[] count;

            count = new int[] { NO_MINE };

            forEachNeighbor(i, neighborIndex -> {
                if (this.field[neighborIndex] == MINE)
                {
                    count[SELF_OFFSET]++;
                }
            });

            this.field[i] = count[SELF_OFFSET];
        }
    }

    /**
     * forEachNeighbor is a helper method that takes in
     * an index of the field and a Consumer to perform
     * an action for each neighbor of the given index.
     *
     * Performs bounds checking to ensure there is no
     * out of bounds errors.
     * @param index to get each neighbor of
     * @param action to perform on each neighbor of the given cell index
     */
    private void forEachNeighbor(
        final int               index,
        final Consumer<Integer> action
    ) {
        final int row;
        final int col;

        row = index / this.width;
        col = index % this.width;

        for (int dr = MIN_OFFSET; dr <= MAX_OFFSET; dr++)
        {
            for (int dc = MIN_OFFSET; dc <= MAX_OFFSET; dc++)
            {
                if (dr == SELF_OFFSET && dc == SELF_OFFSET)
                {
                    continue;
                }

                final int nr;
                final int nc;

                nr = row + dr;
                nc = col + dc;

                if (nr < FIRST_ROW || nr >= this.height ||
                    nc < FIRST_COL || nc >= this.width)
                {
                    continue;
                }

                final int neighborIndex;

                neighborIndex = nr * this.width + nc;
                action.accept(neighborIndex);
            }
        }
    }

    /**
     * popFieldVoid reveals all neighboring cells of a
     * 0 value cell till it reaches a cell that has a
     * value greater than 0. i.e. a cell that has a
     * neighboring mine. Recursively calls itself to
     * accomplish this and "pop" a "void" within the field.
     * @param index to pop
     */
    private void popFieldVoid(final int index)
    {
        forEachNeighbor(index, neighborIndex -> {

            if (!this.revealed[neighborIndex])
            {
                try
                {
                    reveal(neighborIndex);
                }
                catch (final InvalidMoveException e)
                {
                    // revealing neighbors should never be invalid
                }

                if (this.field[neighborIndex] == NO_MINE)
                {
                    popFieldVoid(neighborIndex);
                }
            }
        });
    }

    /**
     * reveal a given cell and returns if it was
     * a mine or not or if invalid reveal
     * @param index of the cell to reveal
     * @return if revealed cell was a mine
     * @throws InvalidMoveException when attempting to reveal a flagged cell
     */
    public boolean reveal(final int index)
        throws InvalidMoveException
    {
        if (this.flagged[index] == FLAG)
        {
            throw new InvalidMoveException("Cannot reveal a flagged cell.");
        }

        this.revealed[index] = true;

        if (this.field[index] == NO_MINE)
        {
            popFieldVoid(index);
        }

        return this.field[index] == MINE;
    }

    /**
     * toggleFlag of a given cell to either flag,
     * question, or no flag.
     * @param index of cell to flag
     * @return the updated state of the flag
     */
    public int toggleFlag(final int index)
    {
        final int nextState;

        nextState = (this.flagged[index] + FLAG) % (FLAG_QUESTION + FLAG);
        this.flagged[index] = nextState;
        return nextState;
    }

    /**
     * getFieldValue returns the value of a given cell
     * used to show values in the button of the UI
     * @param index of cell to get value
     * @return value of given cell
     */
    public int getFieldValue(final int index)
    {
        return this.field[index];
    }

    /**
     * isRevealed returns if a given cell has been revealed
     * @param index of cell to check if revealed
     * @return if given cell is revealed
     */
    public boolean isRevealed(final int index)
    {
        return this.revealed[index];
    }

    /**
     * isMine checks if a given cell is a mine
     * @param index of cell to check if mine
     * @return if given cell is mine
     */
    public boolean isMine(final int index)
    {
        return this.field[index] == MINE;
    }

    /**
     * isFlagged checks if a given cell has been flagged
     * @param index of cell to check if flagged
     * @return if given cell is flagged
     */
    public boolean isFlagged(final int index)
    {
        return this.flagged[index] == FLAG;
    }

    /**
     * isQuestionMarked checks if a given cell has been questioned
     * @param index of cell to check if questioned
     * @return if given cell is questioned
     */
    public boolean isQuestionMarked(final int index)
    {
        return this.flagged[index] == FLAG_QUESTION;
    }

    /**
     * getTotalMines returns the total mines in the field
     * @return total mines in the field
     */
    public int getTotalMines()
    {
        return this.totalMines;
    }

    /**
     * hasWon checks if all cells that are not mines have
     * been revealed, which is considered a win
     * @return if the game is over and has won
     */
    public boolean hasWon()
    {
        int revealedCount;

        revealedCount = NO_MINE;

        for (int i = 0; i < this.field.length; i++)
        {
            if (this.revealed[i])
            {
                revealedCount++;
            }
            else if (this.field[i] != MINE)
            {
                return false;
            }
        }

        return revealedCount == (this.field.length - this.totalMines);
    }

    /**
     * randomizeRemaining removes any mines from the field
     * that have not been flagged, then for each mine removed
     * it is randomly placed back into the field in an unrevealed
     * cell. Effectively "randomizing" undiscovered cells within
     * the field. Field values are then recalculated to reflect new
     * mine positions, and newly discovered voids are popped.
     */
    public void randomizeRemaining()
    {
        if (!this.randomMode)
        {
            return;
        }

        int minesToReplant;

        minesToReplant = NO_MINE;

        for (int i = 0; i < this.field.length; i++)
        {
            if (this.field[i] == MINE && this.flagged[i] == FLAG)
            {
                continue;
            }

            if (this.field[i] == MINE)
            {
                minesToReplant++;
            }

            this.field[i] = NO_MINE;
        }

        while (minesToReplant > NO_MINE)
        {
            final int index;

            index = RANDOM_GENERATOR.nextInt(this.width * this.height);

            if (this.field[index] == NO_MINE && !this.revealed[index])
            {
                this.field[index] = MINE;
                minesToReplant--;
            }
        }

        countNeighboringMines();

        for (int i = 0; i < this.field.length; i++)
        {
            if (this.revealed[i] && this.field[i] == NO_MINE)
            {
                popFieldVoid(i);
            }
        }

    }
}
