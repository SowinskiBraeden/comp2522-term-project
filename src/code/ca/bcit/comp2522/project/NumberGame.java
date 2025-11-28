package ca.bcit.comp2522.project;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * NumberGame is a GUI game with JavaFX
 * where a user has to place randomly generated
 * numbers into a 5x4 grid of buttons. Numbers
 * must be played in order to win.
 *
 * Extends JavaFX Application
 *
 * @author Braeden Sowinski
 * @version 1.0.0
 */
public class NumberGame
    extends Application
{
    private static final int  DECIMAL_PLACES = 2;
    private static final int  NO_GAMES_WON   = 0;
    private static final int  STARTING_NUMBERS_PLACED = 0;
    private static final int  MENU_FONT_SIZE = 16;
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
    private static final int  MENU_PADDING   = 20;
    private static final int  POPUP_WIDTH    = 400;
    private static final int  POPUP_HEIGHT   = 200;
    private static final Font FONT           = Font.font("Arial", FontWeight.BOLD, FONT_SIZE);
    private static final Font MENU_FONT      = Font.font("Arial", FontWeight.NORMAL, MENU_FONT_SIZE);

    private int gamesPlayed;
    private int gamesWon;
    private int allTimePlaced;
    private int numbersPlaced;
    private Label numberLabel;
    private int currentNumber;
    private int[] positions;
    private List<Button> buttons;
    private RandomNumberGenerator generator;
    private AscendingPlacement placementValidator;

    /**
     * start NumberGame GUI
     * @param stage to show
     */
    @Override
    public void start(final Stage stage)
    {
        this.gamesPlayed++;
        this.placementValidator = new AscendingPlacement();
        this.generator          = new RandomNumberGenerator(MIN_RAND_NUM, MAX_RAND_NUM);
        this.numbersPlaced      = STARTING_NUMBERS_PLACED;
        this.currentNumber      = this.generator.generate();
        this.positions          = new int[GRID_WIDTH * GRID_HEIGHT];
        this.buttons            = new ArrayList<>();

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

    /*
     * createGrid is used to generate the
     * button grid for users to play in.
     * @return GridPane with Buttons
     */
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
                button.setStyle("-fx-background-color: #c2c2c2; -fx-text-fill: black;");
                button.setOnAction(e -> handlePress(button, index));

                grid.add(button, i, j);
                buttons.add(button);
            }
        }

        return grid;
    }

    /*
     * showPopup displays a simple popup window with a message
     * and a Restart button to begin a new game.
     * @param title of the popup
     * @param message to show the user
     */
    private void showPopup(final String title, final String message)
    {
        final Stage  popup;
        final VBox   layout;
        final HBox   buttonLayout;
        final Label  msgLabel;
        final Button restartButton;
        final Button quitButton;
        final Scene  popupScene;

        popup = new Stage();
        popup.setTitle(title);

        msgLabel = new Label(message);
        msgLabel.setFont(MENU_FONT);
        msgLabel.setAlignment(Pos.CENTER);

        restartButton = new Button("Restart");
        restartButton.setFont(MENU_FONT);
        restartButton.setOnAction(e -> {
            popup.close();
            restartGame();
        });

        quitButton = new Button("Quit");
        quitButton.setFont(MENU_FONT);
        quitButton.setOnAction(e -> {
            popup.close();

            showResults();
        });

        buttonLayout = new HBox(MENU_PADDING, restartButton, quitButton);
        layout       = new VBox(MENU_PADDING, msgLabel, buttonLayout);
        buttonLayout.setAlignment(Pos.CENTER);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(MENU_PADDING));

        popupScene = new Scene(layout, POPUP_WIDTH, POPUP_HEIGHT);
        popup.setScene(popupScene);
        popup.setResizable(false);
        popup.initOwner(numberLabel.getScene().getWindow());
        popup.show();
    }

    private void showResults()
    {
        final Stage         popup;
        final VBox          layout;
        final Label         msgLabel;
        final Button        okButton;
        final Scene         popupScene;
        final StringBuilder results;

        results = new StringBuilder();
        popup   = new Stage();
        popup.setTitle("Results");

        if (this.gamesWon > NO_GAMES_WON)
        {
            results.append("You won ");
            results.append(this.gamesWon);
            results.append(" out of ");
            results.append(this.gamesPlayed);
            results.append(" games");

            if (this.gamesWon != this.gamesPlayed)
            {
                results.append(" and ");
            }
        }

        if (this.gamesPlayed - this.gamesWon > NO_GAMES_WON)
        {
            results.append("You lost ");
            results.append(this.gamesPlayed - this.gamesWon);
            results.append(" out of ");
            results.append(this.gamesPlayed);
            results.append(" games");
        }

        final float  average;
        final String averageFormatted;

        average          = (float) this.allTimePlaced / (float) this.gamesPlayed;
        averageFormatted = String.format(String.format("%%.%df", DECIMAL_PLACES), average);

        results.append(", with ");
        results.append(this.allTimePlaced);
        results.append(" successful\nplacements, ");
        results.append("an average of ");
        results.append(averageFormatted);
        results.append(" per game.");

        msgLabel = new Label(results.toString());
        msgLabel.setFont(MENU_FONT);
        msgLabel.setAlignment(Pos.CENTER);

        okButton = new Button("OK");
        okButton.setFont(MENU_FONT);
        okButton.setOnAction(e -> {
            popup.close();

            final Stage gameStage;

            gameStage = (Stage) this.numberLabel.getScene().getWindow();
            gameStage.close();
        });
        layout       = new VBox(MENU_PADDING, msgLabel, okButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(MENU_PADDING));

        popupScene = new Scene(layout, POPUP_WIDTH, POPUP_HEIGHT);
        popup.setScene(popupScene);
        popup.setResizable(false);
        popup.initOwner(numberLabel.getScene().getWindow());
        popup.show();
    }

    /*
     * restartGame resets the entire game state by
     * reloading the JavaFX scene.
     */
    private void restartGame()
    {
        final Stage stage;
        stage = (Stage) this.numberLabel.getScene().getWindow();
        start(stage);
    }

    /*
     * disableAllButtons disables every button in the grid
     * after the user loses.
     */
    private void disableAllButtons()
    {
        this.buttons.forEach(button -> {
            button.setMouseTransparent(true);
            button.setFocusTraversable(false);
            button.setStyle("-fx-background-color: #f78f8f; -fx-text-fill: black;");
        });
    }

    /*
     * triggerFailed handles stopping the game
     * when lost, i.e. impossible to place next
     * number
     */
    private void triggerFailed(final String message)
    {
        this.numberLabel.setText("Next number: " + this.currentNumber + " - " + message);
        disableAllButtons();
        showPopup("You Lost!", message);
    }

    /*
     * triggerWin handles stopping the game
     * when won, all numbers placed
     */
    private void triggerWin()
    {
        this.gamesWon++;
        this.numberLabel.setText("All numbers placed!");
        disableAllButtons();
        showPopup("You Won!", "Congratulations! You placed all numbers correctly.");
    }


    /*
     * handlePress of button to place number
     * in that cell
     * @param button pressed
     * @param index of button to place value
     */
    private void handlePress(final Button button, final int index)
    {
        button.setText("" + this.currentNumber);
        button.setMouseTransparent(true);
        button.setFocusTraversable(false);
        button.setStyle("-fx-background-color: #95f595; -fx-text-fill: black;");

        this.numbersPlaced++;
        this.allTimePlaced++;

        this.positions[index] = this.currentNumber;

        // Detect bad placement
        final boolean isValidPlacement;
        isValidPlacement = this.placementValidator.isValidPlacement(
                this.positions,
                index,
                this.currentNumber
        );

        if (!isValidPlacement)
        {
            triggerFailed("Placed number incorrectly.");
            return;
        }

        if (this.numbersPlaced >= TOTAL_NUMBERS)
        {
            triggerWin();
            return;
        }

        this.currentNumber = this.generator.generate();

        final boolean canPlaceNext;
        canPlaceNext = this.placementValidator.canPlaceNext(this.positions, this.currentNumber);

        if (!canPlaceNext)
        {
            triggerFailed("Impossible to place next number.");
            return;
        }

        this.numberLabel.setText("Next number: " + this.currentNumber + " - Select a slot.");
    }

    /**
     * main program entry for quick testing
     * @param args from command line
     */
    public static void main(final String[] args)
    {
        launch(args);
    }
}
