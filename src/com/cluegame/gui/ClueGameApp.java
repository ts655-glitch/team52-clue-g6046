package com.cluegame.gui;

import com.cluegame.model.Game;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JavaFX launcher for the Clue! GUI.
 * Shows a setup screen first, then starts the game with the
 * board, sidebar and detective notebook.
 * Run alongside the existing console Main — this does not replace it.
 * @author Thanh Shaw
 */
public class ClueGameApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Clue!");

        showSetupScreen();
    }

    /**
     * Shows the character selection / setup screen.
     */
    private void showSetupScreen() {
        SetupView setup = new SetupView(this::startGame);

        // wrap in a centered container so the form panel doesn't stretch
        javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(setup);
        wrapper.setStyle("-fx-background-color: #1a1a2e;");

        Scene scene = new Scene(wrapper, 620, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Starts the game with the configured Game instance.
     * Builds the main game layout: board (center), sidebar (right),
     * detective notebook (left).
     * @param game the configured game from the setup screen
     */
    private void startGame(Game game) {
        BoardView boardView = new BoardView(game);

        GameController controller = new GameController(game);

        SidebarView sidebar = new SidebarView(controller, boardView);
        NotebookView notebook = new NotebookView();

        BorderPane root = new BorderPane();
        root.setLeft(notebook);
        root.setCenter(boardView);
        root.setRight(sidebar);

        Scene scene = new Scene(root, 1400, 850);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
