package ca.bcit.comp2522.project;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MinesUI handles all Minesweeper UI elements,
 * main menu, grid generation of the minefield.
 * Updating button displays, disabling buttons,
 * showing flags, warnings, etc.
 *
 * @author Braeden Sowinski
 * @version 1.0.0
 */
public class MinesUI
{
    private static final int  MENU_FONT_SIZE          = 14;
    private static final int  FONT_SIZE               = 18;
    private static final Font FONT                    = Font.font("Arial", FontWeight.BOLD, FONT_SIZE);
    private static final Font MENU_FONT               = Font.font("Arial", FontWeight.NORMAL, MENU_FONT_SIZE);

    private static final int MENU_PADDING             = 5;
    private static final int POPUP_WIDTH              = 500;
    private static final int POPUP_HEIGHT             = 250;

    private static final int WINDOW_WIDTH             = 900;
    private static final int WINDOW_HEIGHT            = 700;
    private static final int BUTTON_WIDTH             = 40;
    private static final int BUTTON_HEIGHT            = 40;
    private static final int MENU_BUTTON_WIDTH        = 200;
    private static final int MENU_BUTTON_HEIGHT       = 60;

    private static final int PADDING                  = 20;
    private static final int GAME_PADDING             = 5;
    private static final int VERTICAL_MARGIN          = 15;
    private static final int TOPBAR_SPACING           = 5;

    private static final int EASY_WIDTH               = 8;
    private static final int EASY_HEIGHT              = 8;
    private static final int EASY_MINES               = 10;
    private static final int MEDIUM_WIDTH             = 16;
    private static final int MEDIUM_HEIGHT            = 16;
    private static final int MEDIUM_MINES             = 40;
    private static final int HARD_WIDTH               = 36;
    private static final int HARD_HEIGHT              = 16;
    private static final int HARD_MINES               = 99;

    private static final int EASY_WINDOW_WIDTH        = 600;
    private static final int EASY_WINDOW_HEIGHT       = 600;
    private static final int MEDIUM_WINDOW_WIDTH      = 1000;
    private static final int MEDIUM_WINDOW_HEIGHT     = 1000;
    private static final int HARD_WINDOW_WIDTH        = 1800;
    private static final int HARD_WINDOW_HEIGHT       = 1000;

    private static final int STARTING_FLAGS           = 0;
    private static final int STARTING_SECONDS         = 0;
    private static final int TIMER_TICK_SECONDS       = 1;

    private static final int DEFAULT_BUTTON_KEY       = -2;
    private static final int MINE_KEY                 = -1;
    private static final int ZERO_KEY                 = 0;

    private static final String TITLE_TEXT            = "Random Mines - A Minesweeper Game";
    private static final String SCORE_FILE            = "./data/minesweeper-score.txt";

    private static final Map<Integer, String> BUTTON_THEMES;

    static
    {
        BUTTON_THEMES = new java.util.HashMap<>();
        BUTTON_THEMES.put(-2, "-fx-background-color: #b3b3b3; -fx-text-fill: black;");
        BUTTON_THEMES.put(-1, "-fx-background-color: #d9d9d9; -fx-text-fill: black;");
        BUTTON_THEMES.put(0, "-fx-background-color: #d9d9d9; -fx-text-fill: black;");
        BUTTON_THEMES.put(1, "-fx-background-color: #c8ffbf; -fx-text-fill: black;");
        BUTTON_THEMES.put(2, "-fx-background-color: #edfc9f; -fx-text-fill: black;");
        BUTTON_THEMES.put(3, "-fx-background-color: #fad08c; -fx-text-fill: black;");
        BUTTON_THEMES.put(4, "-fx-background-color: #f59338; -fx-text-fill: black;");
        BUTTON_THEMES.put(5, "-fx-background-color: #ff644d; -fx-text-fill: black;");
        BUTTON_THEMES.put(6, "-fx-background-color: #ff644d; -fx-text-fill: black;");
        BUTTON_THEMES.put(7, "-fx-background-color: #ff644d; -fx-text-fill: black;");
        BUTTON_THEMES.put(8, "-fx-background-color: #ff644d; -fx-text-fill: black;");
    }

    private final List<Button> buttons;

    private Mines game;

    private Stage gameStage;
    private Label flagLabel;
    private Label timerLabel;

    private boolean randomMode;
    private int     flagsPlaced;
    private int     seconds;

    private Timeline timer;
    private boolean  timerRunning;

    /**
     * MinesUI constructor creates an ArrayList
     * to store buttons and track which mode
     * the game is in, normal or random
     */
    public MinesUI()
    {
        this.buttons = new ArrayList<>();
        this.randomMode   = false;
        this.timerRunning = false;
    }

    private void showInfo(final Window window)
    {
        final Stage  popup;
        final VBox   layout;
        final Label  msgLabel;
        final Button okButton;
        final Scene  popupScene;

        popup = new Stage();
        popup.setTitle("Random Mines - A Minesweeper Game");

        msgLabel = new Label("Random Mode regenerates the field every move. " +
                             "Any mine that is not correctly flagged will get randomly moved " +
                             "after each reveal! This may make it easier to start, but riskier " +
                             "to end. Beware of losing track of mines you haven't flagged.");
        msgLabel.setFont(MENU_FONT);
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setWrapText(true);
        msgLabel.setPadding(new Insets(PADDING));

        okButton = new Button("OK");
        okButton.setFont(MENU_FONT);
        okButton.setOnAction(e -> {
            popup.close();
        });

        layout = new VBox(PADDING, msgLabel, okButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(MENU_PADDING));

        popupScene = new Scene(layout, POPUP_WIDTH, POPUP_HEIGHT);
        popup.setScene(popupScene);
        popup.setResizable(false);
        popup.initOwner(window);
        popup.show();
    }

    /**
     * showMainMenu displays main menu to choose
     * game mode, and field size.
     * @param primaryStage to display main menu to
     */
    public void showMainMenu(final Stage primaryStage)
    {
        final Label  titleLabel;
        final Button easyButton;
        final Button mediumButton;
        final Button hardButton;
        final Label  modeLabel;
        final Button toggleModeButton;
        final Button modeInfoButton;
        final HBox   modeButtons;
        final VBox   root;
        final Scene  scene;

        titleLabel = new Label("Select Game Size");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        easyButton   = new Button("8 x 8 (Beginner)");
        mediumButton = new Button("16 x 16 (Intermediate)");
        hardButton   = new Button("36 x 16 (Expert)");

        easyButton.setPrefWidth(MENU_BUTTON_WIDTH);
        easyButton.setPrefHeight(MENU_BUTTON_HEIGHT);
        mediumButton.setPrefWidth(MENU_BUTTON_WIDTH);
        mediumButton.setPrefHeight(MENU_BUTTON_HEIGHT);
        hardButton.setPrefWidth(MENU_BUTTON_WIDTH);
        hardButton.setPrefHeight(MENU_BUTTON_HEIGHT);

        modeLabel        = new Label("Random Mode: OFF");
        toggleModeButton = new Button("Toggle Random Mode");
        modeInfoButton   = new Button("?");
        modeButtons      = new HBox(MENU_PADDING, toggleModeButton, modeInfoButton);

        toggleModeButton.setOnAction(e -> {
            this.randomMode = !this.randomMode;
            modeLabel.setText("Random Mode: " + (this.randomMode ? "ON" : "OFF"));
        });

        modeInfoButton.setOnAction(e -> showInfo(primaryStage.getOwner()));

        easyButton.setOnAction(e -> startGame(
                EASY_WIDTH,
                EASY_HEIGHT,
                EASY_MINES,
                primaryStage,
                EASY_WINDOW_WIDTH,
                EASY_WINDOW_HEIGHT
            )
        );
        mediumButton.setOnAction(e -> startGame(
                MEDIUM_WIDTH,
                MEDIUM_HEIGHT,
                MEDIUM_MINES,
                primaryStage,
                MEDIUM_WINDOW_WIDTH,
                MEDIUM_WINDOW_HEIGHT
            )
        );
        hardButton.setOnAction(e -> startGame(
                HARD_WIDTH,
                HARD_HEIGHT,
                HARD_MINES,
                primaryStage,
                HARD_WINDOW_WIDTH,
                HARD_WINDOW_HEIGHT
            )
        );

        root = new VBox(VERTICAL_MARGIN);
        root.setPadding(new Insets(PADDING));
        root.setAlignment(Pos.CENTER);
        modeButtons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
            titleLabel,
            easyButton,
            mediumButton,
            hardButton,
            modeLabel,
            modeButtons
        );

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle(TITLE_TEXT);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * startGame creates new Mine game, to
     * generate field, place mines, handle
     * randomization if enabled. Creates
     * grid of buttons for minefield, displays
     * new window of game instance of given size.
     * @param width of field to generate
     * @param height of field to generates
     * @param mines to generate in field
     * @param ownerStage to display minefield grid to
     */
    private void startGame(
        final int   width,
        final int   height,
        final int   mines,
        final Stage ownerStage,
        final int   windowWidth,
        final int   windowHeight
    ) {
        final VBox       root;
        final VBox       topBar;
        final GridPane   grid;
        final Scene      scene;
        final Label      bestLabel;

        this.flagsPlaced = STARTING_FLAGS;
        this.seconds     = STARTING_SECONDS;
        this.timerRunning = false;

        this.game = new Mines(width, height, mines, this.randomMode);

        this.buttons.clear();

        this.flagLabel  = new Label("Flags: " + this.flagsPlaced + " / " + this.game.getTotalMines());
        this.timerLabel = new Label("Time: " + this.seconds);

        final MinesScore bestScore;
        final String     difficulty;

        difficulty = mines == EASY_MINES   ? MinesScore.DIFFICULTY_EASY :
                     mines == MEDIUM_MINES ? MinesScore.DIFFICULTY_MEDIUM :
                                             MinesScore.DIFFICULTY_HARD;

        bestScore = MinesScore.getHighScore(
            MinesScore.readScoresFromFile(SCORE_FILE),
            difficulty,
            this.randomMode
        );

        bestLabel = getBestLabel(bestScore);

        this.flagLabel.setFont(FONT);
        this.timerLabel.setFont(FONT);
        bestLabel.setFont(FONT);

        topBar = new VBox(
            TOPBAR_SPACING,
            this.flagLabel,
            this.timerLabel,
                bestLabel
        );
        topBar.setAlignment(Pos.CENTER);

        grid = createGrid(width, height);

        root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(GAME_PADDING));
        root.getChildren().addAll(topBar, grid);

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        this.gameStage = new Stage();
        this.gameStage.setResizable(true);
        this.gameStage.setMinWidth(windowWidth);
        this.gameStage.setMinHeight(windowHeight);
        this.gameStage.initOwner(ownerStage);
        this.gameStage.setTitle("Random Mines " + width + "x" + height + " - A Minesweeper Game");
        this.gameStage.setScene(scene);
        this.gameStage.show();
    }

    /**
     * getBestLabel generates the label with
     * best score from score file
     * @param bestScore to create label from
     * @return Label of best score
     */
    private static Label getBestLabel(final MinesScore bestScore)
    {
        final Label bestLabel;
        if (bestScore == null)
        {
            bestLabel = new Label("Best: -");
        }
        else
        {
            final StringBuilder best;

            best = new StringBuilder();

            best.append("Best: ");
            best.append(bestScore.getSeconds());
            best.append("s Difficulty: ");
            best.append(bestScore.getDifficulty());
            best.append(" Random Mode: ");
            best.append(bestScore.getRandomMode());
            best.append(" Date: ");
            best.append(bestScore.getDateTimePlayed());

            bestLabel = new Label(best.toString());
        }

        return bestLabel;
    }

    /**
     * createGrid generates the button grid
     * that represents the minefield of the
     * given dimensions
     * @param width of minefield grid
     * @param height of minefield grid
     * @return GridPane containing buttons of minefield
     */
    private GridPane createGrid(final int width, final int height)
    {
        final GridPane grid;

        grid = new GridPane();
        grid.setPadding(new Insets(GAME_PADDING));
        grid.setHgap(GAME_PADDING);
        grid.setVgap(GAME_PADDING);
        grid.setAlignment(Pos.CENTER);

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                final int index;
                final Button button;

                index  = (j * width) + i;
                button = new Button();

                button.setFont(FONT);
                button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
                button.setStyle(BUTTON_THEMES.get(DEFAULT_BUTTON_KEY));

                button.setOnMouseEntered(e -> button.setCursor(javafx.scene.Cursor.HAND));
                button.setOnMouseExited(e -> button.setCursor(javafx.scene.Cursor.DEFAULT));

                button.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY)
                    {
                        handleReveal(index);
                    }
                    else if (e.getButton() == MouseButton.SECONDARY)
                    {
                        handleFlag(index);
                    }
                });

                grid.add(button, i, j);
                this.buttons.add(button);
            }
        }

        return grid;
    }

    /**
     * handleReveal reveals the given cell in the
     * minefield ensures the timer is running if first
     * reveal, call randomize on board if random mode
     * enabled, and refresh all button UI elements.
     * @param index to reveal
     */
    private void handleReveal(final int index)
    {
        if (!timerRunning)
        {
            startTimer();
        }

        try
        {
            final boolean hitMine;
            hitMine = this.game.reveal(index);

            if (hitMine)
            {
                handleLoss();
                return;
            }

            if (this.game.isRandomMode())
            {
                this.game.randomizeRemaining();
            }

            refreshAllButtons();

            if (this.game.hasWon())
            {
                handleWin();
                return;
            }

        }
        catch (final InvalidMoveException e)
        {
            final Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setHeaderText("Invalid move");
            warn.setContentText(e.getMessage());
            warn.showAndWait();
        }
    }

    /**
     * handleFlag updates the given cell to be
     * flagged, questions, or back to no flag.
     * @param index to flag
     */
    private void handleFlag(final int index)
    {
        final int newState;

        newState = this.game.toggleFlag(index);

        if (newState == Mines.FLAG)
        {
            this.flagsPlaced++;
        }
        else if (newState == Mines.FLAG_QUESTION)
        {
            this.flagsPlaced--;
        }

        this.flagLabel.setText("Flags: " +
                               this.flagsPlaced +
                               " / " +
                               this.game.getTotalMines());

        updateButtonDisplay(index);
    }

    /**
     * updateButtonDisplay updates a given button
     * by index depending on how the cell is configured.
     * Flagged, questioned, or revealed.
     * @param index of cell to update button
     */
    private void updateButtonDisplay(final int index)
    {
        final Button button;

        button = this.buttons.get(index);

        if (this.game.isFlagged(index))
        {
            button.setText("F");
            button.setStyle(BUTTON_THEMES.get(DEFAULT_BUTTON_KEY));
            return;
        }

        if (this.game.isQuestionMarked(index))
        {
            button.setText("?");
            button.setStyle(BUTTON_THEMES.get(DEFAULT_BUTTON_KEY));
            return;
        }

        if (!this.game.isRevealed(index))
        {
            button.setText("");
            button.setStyle(BUTTON_THEMES.get(DEFAULT_BUTTON_KEY));
            return;
        }

        final int value;

        value = this.game.getFieldValue(index);

        if (value == MINE_KEY)
        {
            button.setText("*");
            button.setStyle(BUTTON_THEMES.get(MINE_KEY));
        }
        else if (value == ZERO_KEY)
        {
            button.setText(" ");
            button.setStyle(BUTTON_THEMES.get(ZERO_KEY));
        }
        else
        {
            button.setText(Integer.toString(value));
            button.setStyle(BUTTON_THEMES.get(value));
        }
    }

    /**
     * refreshAllButtons iterates over all buttons
     * and calls updateButtonDisplay for that button,
     * as well as disables button if revealed, preventing
     * further clicking on the button.
     */
    private void refreshAllButtons()
    {
        for (int i = 0; i < this.buttons.size(); i++)
        {
            updateButtonDisplay(i);
            if (this.game.isRevealed(i))
            {
                disableButton(this.buttons.get(i));
            }
        }
    }

    /**
     * startTimer when initial cell is revealed to
     * track the game time.
     */
    private void startTimer()
    {
        final KeyFrame tick;

        seconds = STARTING_SECONDS;
        this.timerLabel.setText("Time: " + seconds);

        tick = new KeyFrame(Duration.seconds(TIMER_TICK_SECONDS), e -> {
            this.seconds++;
            this.timerLabel.setText("Time: " + this.seconds + "s");
        });

        this.timer = new Timeline(tick);
        this.timer.setCycleCount(Animation.INDEFINITE);
        this.timer.play();

        this.timerRunning = true;
    }

    /**
     * stopTime once game is over, either lost or won.
     */
    private void stopTimer()
    {
        if (this.timer != null)
        {
            this.timer.stop();
        }
        this.timerRunning = false;
    }

    /**
     * handleWin displays a win message window, with the time
     * taken to win the game, show all mines, and disable all
     * buttons.
     */
    private void handleWin()
    {
        final Alert      winAlert;
        final MinesScore score;
        final String     difficulty;
        final int        mines;

        stopTimer();

        mines = this.game.getTotalMines();

        difficulty = mines == EASY_MINES   ? MinesScore.DIFFICULTY_EASY :
                     mines == MEDIUM_MINES ? MinesScore.DIFFICULTY_MEDIUM :
                                             MinesScore.DIFFICULTY_HARD;

        score = new MinesScore(
            LocalDateTime.now(),
            this.seconds,
            difficulty,
            this.randomMode
        );

        System.out.println(this.seconds + difficulty + this.randomMode);

        showAllMines();
        disableAllButtons();

        final StringBuilder winMessage;

        winMessage = new StringBuilder();

        winMessage.append("You successfully cleared all safe squares!");

        if (MinesScore.isHighScore(score, MinesScore.readScoresFromFile(SCORE_FILE)))
        {
            winMessage.append("\nNew High Score! ");
            winMessage.append("Time: ");
            winMessage.append(score.getSeconds());
            winMessage.append("s\nDifficulty: ");
            winMessage.append(score.getDifficulty());
            winMessage.append("\nRandom Mode: ");
            winMessage.append(score.getRandomMode());
        }

        MinesScore.appendScoreToFile(score, SCORE_FILE);

        winAlert = new Alert(Alert.AlertType.INFORMATION);
        winAlert.setHeaderText("You Win!");
        winAlert.setContentText(winMessage.toString());
        winAlert.showAndWait();
        this.gameStage.close();
    }

    /**
     * handleLoss displays a loss message, reveals all
     * mines and stops timer. Forces user to restart game.
     */
    private void handleLoss()
    {
        final Alert lossAlert;

        stopTimer();
        disableAllButtons();
        showAllMines();

        lossAlert = new Alert(Alert.AlertType.ERROR);
        lossAlert.setHeaderText("You lost...");
        lossAlert.setContentText("You dug up a mine and lost your legs.");
        lossAlert.showAndWait();
        this.gameStage.close();
    }

    /**
     * showAllMines shows all mine locations for when
     * a user wins or loses.
     */
    private void showAllMines()
    {
        for (int i = 0; i < this.buttons.size(); i++)
        {
            if (this.game.isMine(i))
            {
                final Button button;
                button = this.buttons.get(i);

                button.setText("*");
                button.setStyle(BUTTON_THEMES.get(MINE_KEY));
            }
        }
    }

    /**
     * disableAllButtons iterates over all buttons
     * and calls the disableButton method on it.
     */
    private void disableAllButtons()
    {
        final Iterator<Button> iterator;

        iterator = this.buttons.iterator();

        while (iterator.hasNext())
        {
            final Button button;

            button = iterator.next();
            disableButton(button);
        }
    }

    /**
     * disableButton prevents user from
     * interacting with it by clicking.
     * @param button to disable
     */
    private void disableButton(final Button button)
    {
        button.setMouseTransparent(true);
        button.setFocusTraversable(false);
    }
}
