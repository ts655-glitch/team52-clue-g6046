package com.cluegame.gui;

import com.cluegame.model.Game;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Setup screen shown before the game starts. Lets players configure
 * the number of human and AI players, enter names, and choose characters.
 * @author Thanh Shaw
 */
public class SetupView extends VBox {

    private static final String[] CHARACTERS = Game.getCharacterNames();
    private static final String[] TOKENS = Game.getTokenNames();

    private ComboBox<Integer> humanCountBox;
    private ComboBox<Integer> aiCountBox;
    private VBox playerConfigArea;
    private Label statusLabel;
    private List<TextField> nameFields;
    private List<ComboBox<String>> charBoxes;
    private Consumer<Game> onStartGame;

    /**
     * Creates the setup screen.
     * @param onStartGame callback invoked with the configured Game when ready
     */
    public SetupView(Consumer<Game> onStartGame) {
        this.onStartGame = onStartGame;
        this.nameFields = new ArrayList<>();
        this.charBoxes = new ArrayList<>();

        setAlignment(Pos.TOP_CENTER);
        setSpacing(12);
        setPadding(new Insets(30, 60, 30, 60));
        setMaxWidth(550);
        setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("CLUE!");
        title.setFont(Font.font("System", FontWeight.BOLD, 42));
        title.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label("Murder Mystery Game");
        subtitle.setFont(Font.font("System", 16));
        subtitle.setStyle("-fx-text-fill: #aaaaaa;");

        // player count row
        GridPane countGrid = new GridPane();
        countGrid.setHgap(10);
        countGrid.setVgap(8);
        countGrid.setAlignment(Pos.CENTER);

        Label humanLabel = new Label("Human players:");
        humanLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
        humanCountBox = new ComboBox<>();
        humanCountBox.getItems().addAll(1, 2, 3, 4, 5);
        humanCountBox.setValue(1);
        humanCountBox.setPrefWidth(80);
        humanCountBox.setOnAction(e -> rebuildPlayerConfig());

        Label aiLabel = new Label("AI players:");
        aiLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
        aiCountBox = new ComboBox<>();
        aiCountBox.getItems().addAll(0, 1, 2, 3, 4, 5);
        aiCountBox.setValue(2);
        aiCountBox.setPrefWidth(80);
        aiCountBox.setOnAction(e -> rebuildPlayerConfig());

        countGrid.add(humanLabel, 0, 0);
        countGrid.add(humanCountBox, 1, 0);
        countGrid.add(aiLabel, 2, 0);
        countGrid.add(aiCountBox, 3, 0);

        // player config
        playerConfigArea = new VBox(6);
        playerConfigArea.setAlignment(Pos.CENTER);
        playerConfigArea.setPadding(new Insets(5, 0, 5, 0));

        // status label
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12;");
        statusLabel.setWrapText(true);

        // start button
        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("System", FontWeight.BOLD, 15));
        startButton.setPrefWidth(180);
        startButton.setPrefHeight(36);
        startButton.setOnAction(e -> onStart());

        getChildren().addAll(title, subtitle, new Separator(),
                countGrid, new Separator(), playerConfigArea,
                statusLabel, startButton);

        rebuildPlayerConfig();
    }

    /**
     * Rebuilds the player name/character fields based on the current counts.
     */
    private void rebuildPlayerConfig() {
        playerConfigArea.getChildren().clear();
        nameFields.clear();
        charBoxes.clear();

        int numHumans = humanCountBox.getValue();
        int numAI = aiCountBox.getValue();
        int total = numHumans + numAI;

        if (total < 2 || total > 6) {
            statusLabel.setText(total < 2
                    ? "Need at least 2 players total."
                    : "Maximum 6 players total.");
            statusLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 12;");
            return;
        }

        statusLabel.setText("AI players will take the remaining characters.");
        statusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12;");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < numHumans; i++) {
            Label numLabel = new Label((i + 1) + ".");
            numLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13;");

            TextField nameField = new TextField("Player " + (i + 1));
            nameField.setPrefWidth(130);

            Label asLabel = new Label("as");
            asLabel.setStyle("-fx-text-fill: #888888;");

            ComboBox<String> charBox = new ComboBox<>();
            charBox.getItems().addAll(Arrays.asList(CHARACTERS));
            charBox.setValue(CHARACTERS[i]);
            charBox.setPrefWidth(160);

            grid.add(numLabel, 0, i);
            grid.add(nameField, 1, i);
            grid.add(asLabel, 2, i);
            grid.add(charBox, 3, i);

            nameFields.add(nameField);
            charBoxes.add(charBox);
        }

        playerConfigArea.getChildren().add(grid);
    }

    /**
     * Validates setup and creates the game.
     */
    private void onStart() {
        int numHumans = humanCountBox.getValue();
        int numAI = aiCountBox.getValue();
        int total = numHumans + numAI;
        if (total < 2 || total > 6) return;

        // validate no duplicate character selections
        List<String> selectedChars = new ArrayList<>();
        for (int i = 0; i < numHumans; i++) {
            String character = charBoxes.get(i).getValue();
            if (selectedChars.contains(character)) {
                statusLabel.setText("Each player must choose a different character.");
                statusLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 12;");
                return;
            }
            selectedChars.add(character);
        }

        List<String> usedTokens = new ArrayList<>();
        List<Player> playerList = new ArrayList<>();

        for (int i = 0; i < numHumans; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) name = "Player " + (i + 1);

            String character = charBoxes.get(i).getValue();
            String token = getTokenForCharacter(character);
            usedTokens.add(token);
            playerList.add(new HumanPlayer(name, token, 0, 0,
                    new Scanner(System.in)));
        }

        // collect remaining characters and shuffle for random AI assignment
        List<Integer> remaining = new ArrayList<>();
        for (int j = 0; j < CHARACTERS.length; j++) {
            if (!usedTokens.contains(TOKENS[j])) remaining.add(j);
        }
        Collections.shuffle(remaining);

        for (int i = 0; i < numAI && i < remaining.size(); i++) {
            int idx = remaining.get(i);
            playerList.add(new AIPlayer("AI " + CHARACTERS[idx],
                    TOKENS[idx], 0, 0));
            usedTokens.add(TOKENS[idx]);
        }

        Game game = new Game(playerList);
        game.startGame();
        onStartGame.accept(game);
    }

    private String getTokenForCharacter(String character) {
        for (int i = 0; i < CHARACTERS.length; i++) {
            if (CHARACTERS[i].equals(character)) return TOKENS[i];
        }
        return TOKENS[0];
    }
}
