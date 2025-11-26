package ca.bcit.comp2522.project;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class NumberGame
    extends Application
{
    private static final int  STARTING_NUMBERS_PLACED = 0;
    private static final int  INDEX_OFFSET   = 1;
    private static final int  FONT_SIZE      = 24;
    private static final int  WINDOW_WIDTH   = 700;
    private static final int  WINDOW_HEIGHT  = 600;
    private static final int  GRID_PADDING   = 10;
    private static final int  GRID_WIDTH     = 5;
    private static final int  GRID_HEIGHT    = 4;
    private static final int  BUTTON_WIDTH   = 200;
    private static final int  BUTTON_HEIGHT  = 160;
    private static final int  TOTAL_NUMBERS  = 20;
    private static final int  MIN_RAND_NUM   = 1;
    private static final int  MAX_RAND_NUM   = 1001;
    private static final Font FONT           = Font.font("Arial", FontWeight.BOLD, FONT_SIZE);

    private int numbersPlaced;
    private Label numberLabel;
    private int currentNumber;
    private int[] positions;
    private RandomNumberGenerator generator;

    @Override
    public void start(final Stage stage)
    {
        this.generator     = new RandomNumberGenerator(MIN_RAND_NUM, MAX_RAND_NUM);
        this.numbersPlaced = STARTING_NUMBERS_PLACED;
        this.currentNumber = this.generator.generate();
        this.positions     = new int[GRID_WIDTH * GRID_HEIGHT];

        this.numberLabel = new Label("Next number: " + this.currentNumber + " - Select a slot.");
        this.numberLabel.setFont(FONT);
        this.numberLabel.setMaxWidth(Double.MAX_VALUE);
        this.numberLabel.setAlignment(Pos.CENTER);

        final VBox root;
        final GridPane grid;
        final Scene scene;

        root = new VBox();
        grid = createGrid();
        root.getChildren().add(this.numberLabel);
        root.getChildren().add(grid);

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setScene(scene);
        stage.setTitle("20 Number Challenge");
        stage.setResizable(false);
        stage.show();
    }

    private GridPane createGrid()
    {
        final GridPane grid;
        grid = new GridPane();

        grid.setPadding(new Insets(GRID_PADDING));
        grid.setHgap(GRID_PADDING);
        grid.setVgap(GRID_PADDING);

        for (int i = 0; i < GRID_HEIGHT; i++)
        {
            for (int j = 0; j < GRID_WIDTH; j++)
            {
                final Button button;
                final int    index;

                button = new Button("[]");
                index = (j * GRID_HEIGHT) + i;

                button.setFont(FONT);
                button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
                button.setOnAction(e -> handlePress(button, index));

                grid.add(button, i, j);
            }
        }

        return grid;
    }

    private void triggerFailed()
    {
        this.numberLabel.setText("Next number: " + this.currentNumber + " - Impossible to place next number");
    }

    private boolean canBePlaced()
    {
        for (int i = 0; i < positions.length; i++) {

            if (this.positions[i] != STARTING_NUMBERS_PLACED)
            {
                continue;
            }

            int left;
            left = Integer.MIN_VALUE;

            for (int j = i - INDEX_OFFSET; j >= STARTING_NUMBERS_PLACED; j--)
            {
                if (this.positions[j] != STARTING_NUMBERS_PLACED)
                {
                    left = this.positions[j];
                    break;
                }
            }

            int right;
            right = Integer.MAX_VALUE;
            for (int j = i + INDEX_OFFSET; j < this.positions.length; j++)
            {
                if (this.positions[j] != STARTING_NUMBERS_PLACED) {
                    right = this.positions[j];
                    break;
                }
            }

            if (left < currentNumber && currentNumber < right) {
                return true;
            }
        }

        return false;
    }

    private void handlePress(final Button button, final int index)
    {
        button.setText("" + this.currentNumber);
        button.setDisable(true);
        this.numbersPlaced++;

        this.positions[index] = this.currentNumber;

        // Detect bad placement
        for (int i = 0; i < GRID_WIDTH * GRID_HEIGHT; i++)
        {
            final boolean largerBelow;
            final boolean smallerAbove;

            largerBelow  = i < index &&
                           this.positions[i] != STARTING_NUMBERS_PLACED &&
                           this.positions[i] > this.positions[index];

            smallerAbove = i > index &&
                           this.positions[i] != STARTING_NUMBERS_PLACED &&
                           this.positions[i] < this.positions[index];

            if (largerBelow || smallerAbove)
            {
                triggerFailed();
                return;
            }
        }

        if (this.numbersPlaced >= TOTAL_NUMBERS)
        {
            this.numberLabel.setText("All numbers placed!");
            return;
        }

        this.currentNumber = this.generator.generate();

        final boolean canPlaceNext;
        canPlaceNext = canBePlaced();

        if (!canPlaceNext)
        {
            triggerFailed();
            return;
        }

        this.numberLabel.setText("Next number: " + this.currentNumber + " - Select a slot.");
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}
