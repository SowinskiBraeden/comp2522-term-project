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

import java.util.Random;

public class NumberGame extends Application
{
    private static final int  FONT_SIZE      = 24;
    private static final int  WINDOW_WIDTH   = 700;
    private static final int  WINDOW_HEIGHT  = 600;
    private static final int  GRID_PADDING   = 10;
    private static final int  GRID_WIDTH     = 5;
    private static final int  GRID_HEIGHT    = 4;
    private static final int  BUTTON_WIDTH   = 200;
    private static final int  BUTTON_HEIGHT  = 160;
    private static final int  RANDOM_NUMBERS = 20;
    private static final int  MIN_RAND_NUM   = 1;
    private static final int  MAX_RAND_NUM   = 1001;
    private static final Font FONT           = Font.font("Arial", FontWeight.BOLD, FONT_SIZE);
    private static final int  STARTING_NUMBER_INDEX = 0;

    private int currentIndex = STARTING_NUMBER_INDEX;

    private int[] numbers;
    private Label numberLabel;

    @Override
    public void start(final Stage stage)
    {
        final Random rand = new Random();

        numbers = new int[RANDOM_NUMBERS];
        for (int i = 0; i < RANDOM_NUMBERS; i++) {
            numbers[i] = rand.nextInt(MAX_RAND_NUM - MIN_RAND_NUM) + MIN_RAND_NUM;
        }

        numberLabel = new Label("Next number: " + numbers[currentIndex] + " - Select a slot.");
        numberLabel.setFont(FONT);
        numberLabel.setMaxWidth(Double.MAX_VALUE);
        numberLabel.setAlignment(Pos.CENTER);

        final VBox root = new VBox(10);
        root.getChildren().add(numberLabel);

        final GridPane grid = createGrid();
        root.getChildren().add(grid);

        final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("20 Number Challenge");
        stage.setResizable(false);
        stage.show();
    }

    private GridPane createGrid()
    {
        final GridPane grid = new GridPane();
        grid.setPadding(new Insets(GRID_PADDING));
        grid.setHgap(GRID_PADDING);
        grid.setVgap(GRID_PADDING);

        final Button[] buttons;

        buttons = new Button[GRID_WIDTH * GRID_HEIGHT];

        for (int i = 0; i < GRID_HEIGHT; i++)
        {
            for (int j = 0; j < GRID_WIDTH; j++)
            {
                final Button button = new Button("[]");
                button.setFont(FONT);
                button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

                final int index;
                index = i * GRID_WIDTH + j;
                buttons[index] = button;

                button.setOnAction(e -> handlePress(button));

                grid.add(button, i, j);
            }
        }

        return grid;
    }

    private void handlePress(final Button button)
    {
        button.setText("" + numbers[currentIndex]);
        button.setDisable(true);

        currentIndex++;

        if (currentIndex < RANDOM_NUMBERS) {
            numberLabel.setText("Next number: " + numbers[currentIndex] + " - Select a slot.");
        }
        else
        {
            numberLabel.setText("All numbers placed!");
        }
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}
