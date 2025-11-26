package ca.bcit.comp2522.project;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class Mines extends Application
{
    private static final int  FONT_SIZE         = 18;
    private static final Font FONT              = Font.font("Arial", FontWeight.BOLD, FONT_SIZE);

    private static final int WINDOW_WIDTH       = 1920;
    private static final int WINDOW_HEIGHT      = 1080;
    private static final int BUTTON_WIDTH       = 60;
    private static final int BUTTON_HEIGHT      = 60;
    private static final int MENU_BUTTON_WIDTH  = 180;
    private static final int EASY_WIDTH         = 8;
    private static final int EASY_HEIGHT        = 8;
    private static final int HARD_WIDTH         = 36;
    private static final int HARD_HEIGHT        = 16;
    private static final int PADDING            = 20;
    private static final int GAME_PADDING       = 5;
    private static final int VERTICAL_MARGIN    = 15;

    private static final int EASY_MINES         = 10;
    private static final int HARD_MINES         = 99;
    private static final int MINE               = -1;
    private static final int NO_MINE            = 0;

    private static final int MIN_OFFSET         = -1;
    private static final int MAX_OFFSET         = 1;
    private static final int SELF_OFFSET        = 0;
    private static final int FIRST_ROW          = 0;
    private static final int FIRST_COL          = 0;

    private static final int NO_FLAG            = 0;
    private static final int FLAG               = 1;
    private static final int FLAG_QUESTION      = 2;

    private static final int DEFAULT_BUTTON     = -2;
    private static final Map<Integer, String> BUTTON_THEMES = new HashMap<>();

    static {
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

    private int[]     field;
    private boolean[] revealed;
    private int[]     flagged;
    private Button[]  buttons;
    private int       width;
    private int       height;
    private int       flags;

    private Label flagLabel;
    private Label timerLabel;
    private int   totalMines;

    private int seconds;
    private Timeline timer;
    private boolean timerRunning = false;
    private boolean randomMode = false;

    private void startTimer() {
        seconds = 0;
        timerLabel.setText("Time: 0");

        timer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    seconds++;
                    timerLabel.setText("Time: " + seconds);
                })
        );
        timer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timer.play();
        timerRunning = true;
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
        timerRunning = false;
    }

    @Override
    public void start(final Stage stage)
    {
        final Label  title;
        final Button smallBtn;
        final Button largeBtn;

        title = new Label("Select Game Size");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        smallBtn = new Button("8 × 8 (Beginner)");
        largeBtn = new Button("36 × 16 (Expert)");

        smallBtn.setPrefWidth(MENU_BUTTON_WIDTH);
        largeBtn.setPrefWidth(MENU_BUTTON_WIDTH);
        smallBtn.setOnAction(e -> startGame(EASY_WIDTH, EASY_HEIGHT, EASY_MINES));
        largeBtn.setOnAction(e -> startGame(HARD_WIDTH, HARD_HEIGHT, HARD_MINES));

        final VBox  root;
        final Scene scene;

        root = new VBox(VERTICAL_MARGIN);
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        root.setPadding(new Insets(PADDING));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(title, smallBtn, largeBtn);

        stage.setTitle("Random Mines - A Minesweeper Game");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void forEachNeighbor(
        final int index,
        final Consumer<Integer> action
    ) {
        final int row;
        final int col;

        row = index / width;
        col = index % width;

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

                if (nr < FIRST_ROW || nr >= height ||
                    nc < FIRST_COL || nc >= width)
                {
                    continue;
                }

                final int neighborIndex;
                neighborIndex = nr * width + nc;
                action.accept(neighborIndex);
            }
        }
    }

    private void generateField(final int mines)
    {
        this.revealed = new boolean[width * height];
        this.field    = new int[width * height];
        this.flagged  = new int[width * height];

        final Random rand;
        int placed;

        rand = new Random();
        placed = NO_MINE;

        while (placed < mines)
        {
            final int index;
            index = rand.nextInt(width * height);

            if (this.field[index] != MINE)
            {
                this.field[index] = MINE;
                placed++;
            }
        }

        for (int i = 0; i < this.field.length; i++)
        {
            if (this.field[i] == MINE)
            {
                continue;
            }

            final int[] count = { NO_MINE };

            forEachNeighbor(i, neighborIndex -> {
                if (this.field[neighborIndex] == MINE)
                {
                    count[SELF_OFFSET]++;
                }
            });

            this.field[i] = count[SELF_OFFSET];
        }
    }

    private void popFieldVoid(final int index)
    {
        forEachNeighbor(index, neighborIndex -> {

            if (!this.revealed[neighborIndex])
            {
                reveal(neighborIndex);

                if (this.field[neighborIndex] == NO_MINE)
                {
                    popFieldVoid(neighborIndex);
                }
            }
        });
    }

    private void startGame(
        final int width,
        final int height,
        final int mines
    ) {
        this.flags      = NO_FLAG;
        this.buttons    = new Button[width * height];
        this.width      = width;
        this.height     = height;
        this.totalMines = mines;

        generateField(mines);

        final Stage    gameStage;
        final VBox     box;
        final VBox     topBar;
        final GridPane grid;
        final Label    modeLabel;
        final Button   toggleModeBtn;

        gameStage     = new Stage();
        box           = new VBox();
        grid          = createGrid(width, height);
        modeLabel     = new Label("Random Mode: OFF");
        toggleModeBtn = new Button("Toggle Random Mode");

        this.flagLabel  = new Label("Flags: 0 / " + totalMines);
        this.timerLabel = new Label("Time: 0");
        this.flagLabel.setFont(FONT);
        this.timerLabel.setFont(FONT);

        toggleModeBtn.setOnAction(e -> {
            randomMode = !randomMode;
            modeLabel.setText("Random Mode: " + (randomMode ? "ON" : "OFF"));
        });


        topBar = new VBox(5, flagLabel, timerLabel);

        topBar.setAlignment(Pos.CENTER);
        box.getChildren().add(0, topBar);

        box.getChildren().add(grid);
        box.setAlignment(Pos.CENTER);
        grid.setAlignment(Pos.CENTER);

        final Scene scene;
        scene = new Scene(box, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.getChildren().addAll(title, smallBtn, largeBtn, modeLabel, toggleModeBtn);

        gameStage.setScene(scene);
        gameStage.setTitle("Random Mines " + width + "x" + height);
        gameStage.show();
        startTimer();
    }

    private void flag(final int index)
    {
        this.flagged[index] = (this.flagged[index] + FLAG) % (FLAG_QUESTION + FLAG);

        final String buttonText;

        buttonText = this.flagged[index] == FLAG ? "F" :
                     this.flagged[index] == FLAG_QUESTION ? "?" : "";

        this.buttons[index].setText(buttonText);

        this.flags = this.flagged[index] == FLAG ?
                     this.flags + FLAG :
                     this.flagged[index] == FLAG_QUESTION ?
                     this.flags - FLAG :
                     this.flags;

         this.flagLabel.setText("Flags: " + flags + " / " + totalMines);
    }

    private void checkWin()
    {
        int numRevealed;
        numRevealed = NO_MINE;

        for (int i = 0; i < this.field.length; i++)
        {
            if (this.revealed[i])
            {
                numRevealed++;
            }
            else if (this.field[i] != MINE)
            {
                // if a non revealed cell is not a mine, we know its not a win
                return;
            }
            else
            {
                // do nothing
                continue;
            }
        }

        if (numRevealed == (this.field.length - totalMines))
        {
            // WIN CONDITION
            stopTimer();

            final Alert win;
            win = new Alert(Alert.AlertType.INFORMATION);
            win.setHeaderText("You Win!");
            win.setContentText("You successfully cleared all safe squares!");
            win.showAndWait();

            ((Stage) buttons[0].getScene().getWindow()).close();
        }
    }

    private GridPane createGrid(
        final int width,
        final int height
    ) {
        final GridPane grid;
        grid = new GridPane();

        grid.setPadding(new Insets(GAME_PADDING));
        grid.setHgap(GAME_PADDING);
        grid.setVgap(GAME_PADDING);

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                final Button button;
                final int    index;

                index = (j * width) + i;
                button = new Button();

                button.setFont(FONT);
                button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
                button.setStyle(BUTTON_THEMES.get(DEFAULT_BUTTON));
                button.setOnMouseEntered(e -> button.setCursor(javafx.scene.Cursor.HAND));
                button.setOnMouseExited(e -> button.setCursor(javafx.scene.Cursor.DEFAULT));
                button.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY)
                    {
                        final boolean lossed;
                        lossed = reveal(index);

                        if (lossed)
                        {
                            final Alert loss;
                            loss = new Alert(Alert.AlertType.ERROR);
                            loss.setHeaderText("You lost...");
                            loss.setContentText("Unfortunately, you dug up a mine and lost your legs. I hear there is a deal on wheelchairs though.");
                            loss.showAndWait();

                            final Stage gameStage;
                            gameStage = (Stage) buttons[index].getScene().getWindow();
                            gameStage.close();
                            return;
                        }

                        checkWin();

                        randomizeRemaining();
                    }
                    else if (e.getButton() == MouseButton.SECONDARY)
                    {
                        flag(index);
                    }
                    else
                    {
                        // do nothing
                        return;
                    }
                });

                grid.add(button, i, j);
                this.buttons[index] = button;
            }
        }

        return grid;
    }

    private void randomizeRemaining()
    {
        int regenMine;

        regenMine = NO_MINE;

        for (int i = 0; i < this.field.length; i++)
        {
            // Ignore correctly flagged mines
            if (this.field[i] == MINE && this.flagged[i] == FLAG)
            {
                continue;
            }

            if (this.field[i] == MINE)
            {
                regenMine++;
            }

            this.field[i] = NO_MINE;
        }

        final Random rand;
        int placed;

        rand = new Random();
        placed = NO_MINE;

        while (placed < regenMine)
        {
            final int index;
            index = rand.nextInt(width * height);

            if (this.field[index] == NO_MINE && !this.revealed[index])
            {
                this.field[index] = MINE;
                placed++;
            }
        }

        for (int i = 0; i < this.field.length; i++)
        {
            if (this.field[i] == MINE)
            {
                continue;
            }

            final int[] count = { NO_MINE };

            forEachNeighbor(i, neighborIndex -> {
                if (this.field[neighborIndex] == MINE)
                {
                    count[SELF_OFFSET]++;
                }
            });

            this.field[i] = count[SELF_OFFSET];
        }

        for (int i = 0; i < this.field.length; i++)
        {
            if (this.revealed[i])
            {
                this.buttons[i].setText(this.field[i] == NO_MINE ? "" : "" + this.field[i]);
                this.buttons[i].setStyle(BUTTON_THEMES.get(this.field[i]));
            }

            if (this.revealed[i] && this.field[i] == NO_MINE)
            {
                popFieldVoid(i);
            }
        }
    }

    private boolean reveal(final int index)
    {
        if (this.revealed[index])
        {
            return false;
        }

        final String buttonText;
        final Button button;

        button = this.buttons[index];

        buttonText = "" + (this.field[index] == MINE ? "*" :
                           this.field[index] == NO_MINE ? " " :
                           this.field[index]);

        button.setText(buttonText);
        button.setStyle(BUTTON_THEMES.get(this.field[index]));
        button.setMouseTransparent(true);
        button.setFocusTraversable(false);

        this.revealed[index] = true;

        if (this.field[index] == NO_MINE)
        {
            popFieldVoid(index);
        }

        return this.field[index] == MINE;
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}
