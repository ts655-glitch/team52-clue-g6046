package com.cluegame.gui;

import com.cluegame.cards.Card;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.model.Accusation;
import com.cluegame.model.Suggestion;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Sidebar panel for the Clue! GUI. Shows the current player's turn info,
 * their hand of cards, action buttons and a scrollable game log.
 * Handles full turn flow including AI turns and human handoff.
 * @author Thanh Shaw
 */
public class SidebarView extends VBox {

    private static final double SIDEBAR_WIDTH = 300;
    private static final double BTN_WIDTH = SIDEBAR_WIDTH - 30;

    private Label turnLabel;
    private Label phaseLabel;
    private Label movesLabel;
    private VBox handBox;
    private Button rollDiceButton;
    private Button endMoveButton;
    private Button suggestButton;
    private Button accuseButton;
    private Button endTurnButton;
    private TextArea gameLog;
    private GameController controller;
    private BoardView boardView;
    private NotebookView notebookView;
    private Player displayedPlayer;
    private boolean multipleHumans;
    private PauseTransition aiTurnPause;
    private boolean currentTurnEndLogged;

    /**
     * Creates the sidebar with all UI components, wired to the controller.
     * @param controller the game controller managing turn state
     * @param boardView the board view to refresh after actions
     */
    public SidebarView(GameController controller, BoardView boardView,
                       NotebookView notebookView) {
        this.controller = controller;
        this.boardView = boardView;
        this.notebookView = notebookView;

        int humanCount = 0;
        for (Player p : controller.getPlayers()) {
            if (p instanceof HumanPlayer) humanCount++;
        }
        this.multipleHumans = humanCount > 1;

        setPrefWidth(SIDEBAR_WIDTH);
        setMinWidth(SIDEBAR_WIDTH);
        setMaxWidth(SIDEBAR_WIDTH);
        setPadding(new Insets(12));
        setSpacing(6);
        setStyle("-fx-background-color: #2b2b2b;");

        // turn indicator
        turnLabel = new Label();
        turnLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        turnLabel.setStyle("-fx-text-fill: white;");
        turnLabel.setWrapText(true);

        // phase indicator
        phaseLabel = new Label();
        phaseLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        phaseLabel.setStyle("-fx-text-fill: #ffcc00;");

        // moves remaining
        movesLabel = new Label();
        movesLabel.setFont(Font.font("System", 12));
        movesLabel.setStyle("-fx-text-fill: #ffcc00;");

        // hand section
        Label handTitle = new Label("Your Hand:");
        handTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        handTitle.setStyle("-fx-text-fill: #aaaaaa;");

        handBox = new VBox(3);
        handBox.setPadding(new Insets(2, 0, 6, 8));

        // action buttons — consistent sizing
        rollDiceButton = createButton("Roll Dice");
        rollDiceButton.setOnAction(e -> onRollDice());

        endMoveButton = createButton("End Move");
        endMoveButton.setOnAction(e -> onEndMove());
        endMoveButton.setVisible(false);
        endMoveButton.setManaged(false);

        suggestButton = createButton("Make Suggestion");
        suggestButton.setOnAction(e -> onSuggest());
        suggestButton.setVisible(false);
        suggestButton.setManaged(false);

        accuseButton = createButton("Make Accusation");
        accuseButton.setStyle("-fx-text-fill: #cc3333;");
        accuseButton.setOnAction(e -> onAccuse());
        accuseButton.setVisible(false);
        accuseButton.setManaged(false);

        endTurnButton = createButton("End Turn");
        endTurnButton.setOnAction(e -> finishTurn());
        endTurnButton.setVisible(false);
        endTurnButton.setManaged(false);

        // game log
        Label logTitle = new Label("Game Log:");
        logTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        logTitle.setStyle("-fx-text-fill: #aaaaaa;");

        gameLog = new TextArea();
        gameLog.setEditable(false);
        gameLog.setWrapText(true);
        gameLog.setPrefHeight(200);
        javafx.scene.layout.VBox.setVgrow(gameLog, javafx.scene.layout.Priority.ALWAYS);
        gameLog.setStyle("-fx-control-inner-background: #1e1e1e; "
                + "-fx-text-fill: #dddddd; -fx-font-size: 11;");

        getChildren().addAll(
                turnLabel, phaseLabel, movesLabel,
                new Separator(),
                handTitle, handBox,
                new Separator(),
                rollDiceButton, endMoveButton, suggestButton, accuseButton,
                endTurnButton,
                new Separator(),
                logTitle, gameLog);

        boardView.setClickListener(this::onBoardClicked);
        controller.setSuggestionCardChooser(this::chooseSuggestionCardForGui);

        updateForCurrentPlayer();
        setPhase("Waiting to roll");
        currentTurnEndLogged = false;
        appendLog("Game started. Cards have been dealt.");
        appendHumanTurnBanner();
    }

    /**
     * Creates a consistently styled action button.
     */
    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(BTN_WIDTH);
        btn.setPrefHeight(30);
        btn.setFont(Font.font("System", FontWeight.BOLD, 13));
        return btn;
    }

    /**
     * Updates the phase indicator label.
     */
    private void setPhase(String text) {
        phaseLabel.setText(text);
    }

    // --- Turn flow ---

    private void onRollDice() {
        Player current = controller.getCurrentPlayer();
        if (current.getCurrentRoom() != null) {
            handleRoomExit();
            return;
        }
        startMovement();
    }

    private void handleRoomExit() {
        Player current = controller.getCurrentPlayer();
        List<String> options = controller.getRoomExitOptions();
        options.add("Stay in " + current.getCurrentRoom().getName());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                options.get(options.size() - 1), options);
        dialog.setTitle("Clue! - Room Exit");
        dialog.setHeaderText(current.getName() + " is in "
                + current.getCurrentRoom().getName());
        dialog.setContentText("Choose an action:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().startsWith("Stay")) {
            appendLog(current.getName() + " stays in "
                    + current.getCurrentRoom().getName() + ".");
            enterSuggestionPhase();
            return;
        }

        int chosenIndex = options.indexOf(result.get());
        String exitResult = controller.exitRoom(chosenIndex);
        appendLog(exitResult);
        boardView.drawTokens();

        if (result.get().startsWith("Secret passage")) {
            enterSuggestionPhase();
        } else {
            startMovement();
        }
    }

    private void startMovement() {
        Player current = controller.getCurrentPlayer();
        int roll = controller.rollDice();
        appendLog(current.getName() + " rolled " + roll + ".");

        setPhase("Moving (" + roll + " steps)");
        rollDiceButton.setDisable(true);
        endMoveButton.setVisible(true);
        endMoveButton.setManaged(true);
        updateMovesLabel();
        showValidMoves();
    }

    private void onEndMove() {
        appendLog(controller.getCurrentPlayer().getName() + " stops moving.");
        controller.endMove();
        endMovementPhase();
    }

    private void onBoardClicked(int row, int col) {
        if (!controller.isMovementActive()) return;

        Player current = controller.getCurrentPlayer();
        GameController.MoveResult result = controller.tryMove(row, col);

        switch (result) {
            case MOVED:
                updateMovesLabel();
                setPhase("Moving (" + controller.getRemainingSteps() + " left)");
                boardView.drawTokens();
                if (controller.isMovementActive()) {
                    showValidMoves();
                } else {
                    endMovementPhase();
                }
                break;
            case LANDED_ON_DOOR:
                boardView.drawTokens();
                handleDoorChoice(current, row, col);
                break;
            case ENTERED_ROOM:
                appendLog(current.getName() + " entered the "
                        + current.getCurrentRoom().getName() + ".");
                boardView.clearHighlights();
                boardView.drawTokens();
                updateMovesLabel();
                enterSuggestionPhase();
                break;
            case INVALID:
                break;
        }
    }

    /**
     * Asks the player whether to enter the room or keep moving past the door.
     */
    private void handleDoorChoice(Player current, int row, int col) {
        String roomName = controller.getBoard().getRoomAt(row, col).getName();

        Alert choice = new Alert(Alert.AlertType.CONFIRMATION,
                "Enter the " + roomName + "?",
                ButtonType.YES, ButtonType.NO);
        choice.setTitle("Clue! - Door");
        choice.setHeaderText(current.getName() + " is at the " + roomName + " door.");

        Optional<ButtonType> answer = choice.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            controller.enterCurrentDoor();
            appendLog(current.getName() + " entered the " + roomName + ".");
            boardView.clearHighlights();
            boardView.drawTokens();
            updateMovesLabel();
            enterSuggestionPhase();
        } else {
            // keep moving — update highlights for remaining steps
            appendLog(current.getName() + " walks past the " + roomName + " door.");
            updateMovesLabel();
            setPhase("Moving (" + controller.getRemainingSteps() + " left)");
            if (controller.isMovementActive()) {
                showValidMoves();
            } else {
                endMovementPhase();
            }
        }
    }

    private void endMovementPhase() {
        boardView.clearHighlights();
        endMoveButton.setVisible(false);
        endMoveButton.setManaged(false);
        movesLabel.setText("");

        if (controller.getCurrentPlayer().getCurrentRoom() != null) {
            enterSuggestionPhase();
        } else {
            Player current = controller.getCurrentPlayer();
            appendLog(current.getName() + " ends the turn at ("
                    + current.getRow() + ", " + current.getCol() + ").");
            currentTurnEndLogged = true;
            setPhase("");
            finishTurn();
        }
    }

    // --- Suggestion phase ---

    private void enterSuggestionPhase() {
        setPhase("In room — suggest or end turn");
        rollDiceButton.setDisable(true);
        endMoveButton.setVisible(false);
        endMoveButton.setManaged(false);
        movesLabel.setText("");

        suggestButton.setVisible(true);
        suggestButton.setManaged(true);
        accuseButton.setVisible(true);
        accuseButton.setManaged(true);
        endTurnButton.setVisible(true);
        endTurnButton.setManaged(true);
    }

    private void onSuggest() {
        Player current = controller.getCurrentPlayer();
        if (current.getCurrentRoom() == null) return;

        String roomName = current.getCurrentRoom().getName();

        ChoiceDialog<String> suspectDialog = new ChoiceDialog<>(
                GameController.getSuspects()[0],
                Arrays.asList(GameController.getSuspects()));
        suspectDialog.setTitle("Clue! - Suggestion");
        suspectDialog.setHeaderText("Suggesting in the " + roomName);
        suspectDialog.setContentText("Choose a suspect:");
        Optional<String> suspectResult = suspectDialog.showAndWait();
        if (suspectResult.isEmpty()) return;

        ChoiceDialog<String> weaponDialog = new ChoiceDialog<>(
                GameController.getWeapons()[0],
                Arrays.asList(GameController.getWeapons()));
        weaponDialog.setTitle("Clue! - Suggestion");
        weaponDialog.setHeaderText("Suggesting in the " + roomName);
        weaponDialog.setContentText("Choose a weapon:");
        Optional<String> weaponResult = weaponDialog.showAndWait();
        if (weaponResult.isEmpty()) return;

        Suggestion suggestion = new Suggestion(
                new SuspectCard(suspectResult.get()),
                new WeaponCard(weaponResult.get()),
                new RoomCard(roomName));

        appendLog(current.getName() + " suggests: " + suggestion);

        GameController.SuggestionResult result =
                controller.resolveSuggestion(suggestion);

        // log suspect token movement
        String suspectName = suspectResult.get();
        for (Player p : controller.getPlayers()) {
            if (suspectName.toLowerCase().contains(p.getToken().toLowerCase())
                    && p != current) {
                appendLog("  " + p.getName() + "'s token moved to "
                        + roomName + " (by suggestion).");
                break;
            }
        }

        boardView.drawTokens();

        if (result.isDisproved()) {
            appendLog(result.getDisproverName() + " disproves the suggestion.");
            Alert cardAlert = new Alert(Alert.AlertType.INFORMATION);
            cardAlert.setTitle("Clue! - Card Shown");
            cardAlert.setHeaderText(result.getDisproverName()
                    + " shows you a card:");
            cardAlert.setContentText(result.getShownCard().getName());
            cardAlert.showAndWait();
        } else {
            appendLog("No one could disprove the suggestion.");
        }

        suggestButton.setVisible(false);
        suggestButton.setManaged(false);
        setPhase("Suggestion made — accuse or end turn");
    }

    private void onAccuse() {
        Alert warning = new Alert(Alert.AlertType.WARNING,
                "A wrong accusation will eliminate you from the game!\n"
                + "Are you sure?",
                ButtonType.YES, ButtonType.NO);
        warning.setTitle("Clue! - Accusation");
        warning.setHeaderText("Make a formal accusation?");
        Optional<ButtonType> confirm = warning.showAndWait();
        if (confirm.isEmpty() || confirm.get() != ButtonType.YES) return;

        ChoiceDialog<String> suspectDialog = new ChoiceDialog<>(
                GameController.getSuspects()[0],
                Arrays.asList(GameController.getSuspects()));
        suspectDialog.setTitle("Clue! - Accusation");
        suspectDialog.setContentText("Who is the murderer?");
        Optional<String> suspectResult = suspectDialog.showAndWait();
        if (suspectResult.isEmpty()) return;

        ChoiceDialog<String> weaponDialog = new ChoiceDialog<>(
                GameController.getWeapons()[0],
                Arrays.asList(GameController.getWeapons()));
        weaponDialog.setTitle("Clue! - Accusation");
        weaponDialog.setContentText("What is the murder weapon?");
        Optional<String> weaponResult = weaponDialog.showAndWait();
        if (weaponResult.isEmpty()) return;

        String[] roomNames = {"Kitchen", "Ballroom", "Conservatory",
                "Billiard Room", "Library", "Study", "Hall", "Lounge",
                "Dining Room"};
        ChoiceDialog<String> roomDialog = new ChoiceDialog<>(
                roomNames[0], Arrays.asList(roomNames));
        roomDialog.setTitle("Clue! - Accusation");
        roomDialog.setContentText("Where did the murder happen?");
        Optional<String> roomResult = roomDialog.showAndWait();
        if (roomResult.isEmpty()) return;

        Accusation accusation = new Accusation(
                new SuspectCard(suspectResult.get()),
                new WeaponCard(weaponResult.get()),
                new RoomCard(roomResult.get()));

        appendLog(controller.getCurrentPlayer().getName()
                + " accuses: " + accusation);
        boolean correct = controller.checkAccusation(accusation);

        if (correct) {
            appendLog(controller.getCurrentPlayer().getName()
                    + " wins the game!");
            showGameOver();
            return;
        }

        String playerName = controller.getCurrentPlayer().getName();
        appendLog(playerName + "'s accusation was WRONG! Eliminated.");

        Alert eliminated = new Alert(Alert.AlertType.ERROR);
        eliminated.setTitle("Clue! - Wrong Accusation");
        eliminated.setHeaderText("Your accusation was incorrect.");
        eliminated.setContentText(playerName + " has been eliminated from the game.\n\n"
                + "You will remain to disprove other players' suggestions,\n"
                + "but you can no longer move, suggest, or accuse.\n\n"
                + "The game will continue with the remaining players.");
        eliminated.showAndWait();

        if (controller.isGameOver()) { showGameOver(); return; }
        finishTurn();
    }

    private void showGameOver() {
        hideTurnButtons();
        setPhase("Game Over");

        StringBuilder msg = new StringBuilder();
        msg.append("\n--- GAME OVER ---\n");
        if (controller.getWinner() != null) {
            msg.append(controller.getWinner().getName()).append(" wins!\n");
        } else {
            msg.append("No one solved the mystery.\n");
        }
        String envSuspect = controller.getEnvelope().getSuspect() != null
                ? controller.getEnvelope().getSuspect().getName() : "(unknown)";
        String envWeapon = controller.getEnvelope().getWeapon() != null
                ? controller.getEnvelope().getWeapon().getName() : "(unknown)";
        String envRoom = controller.getEnvelope().getRoom() != null
                ? controller.getEnvelope().getRoom().getName() : "(unknown)";

        msg.append("\nMurder envelope:\n");
        msg.append("  Suspect: ").append(envSuspect).append("\n");
        msg.append("  Weapon:  ").append(envWeapon).append("\n");
        msg.append("  Room:    ").append(envRoom).append("\n");
        appendLog(msg.toString());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Clue! - Game Over");
        alert.setHeaderText(controller.getWinner() != null
                ? controller.getWinner().getName() + " wins!"
                : "No one solved the mystery.");
        alert.setContentText(
                "Suspect: " + envSuspect
                + "\nWeapon: " + envWeapon
                + "\nRoom: " + envRoom);
        alert.getDialogPane().setMinWidth(350);
        alert.showAndWait();
    }

    // --- Turn management ---

    private void showValidMoves() {
        List<int[]> valid = controller.getValidMoves();
        boardView.setHighlights(valid);
        if (valid.isEmpty()) {
            appendLog(controller.getCurrentPlayer().getName()
                    + " has no valid moves.");
            endMovementPhase();
        }
    }

    private void finishTurn() {
        if (controller.isGameOver()) { showGameOver(); return; }
        logHumanTurnEndIfNeeded();
        hideTurnButtons();
        setPhase("Advancing...");
        rollDiceButton.setDisable(true);

        controller.advanceToNextPlayer();
        if (controller.isGameOver()) { showGameOver(); return; }
        processNextTurn();
    }

    private void processNextTurn() {
        if (controller.isGameOver()) { showGameOver(); return; }

        if (!hasActiveHuman()) {
            runAIOnlyFinish();
            return;
        }

        if (controller.isCurrentPlayerAI()) {
            hideTurnButtons();
            updateDisplayedPlayer(controller.getCurrentPlayer());
            setPhase("AI resolving...");
            rollDiceButton.setDisable(true);
            List<String> aiLog = controller.runAITurn();
            for (String msg : aiLog) {
                appendLog(msg);
            }
            boardView.drawTokens();

            if (controller.isGameOver()) { showGameOver(); return; }

            controller.advanceToNextPlayer();
            if (controller.isCurrentPlayerAI()) {
                setPhase("AI resolving...");
                scheduleAiContinuation(this::processNextTurn, 800);
            } else {
                beginHumanTurn();
            }
        } else {
            beginHumanTurn();
        }
    }

    private void beginHumanTurn() {
        hideTurnButtons();
        boardView.drawTokens();
        Player next = controller.getCurrentPlayer();

        appendLog("");
        if (multipleHumans) {
            showHandoffPlaceholder(next);
            Alert handoff = new Alert(Alert.AlertType.INFORMATION);
            handoff.setTitle("Clue! - Player Handoff");
            handoff.setHeaderText("Pass to " + next.getName()
                    + " (" + next.getToken() + ")");
            handoff.setContentText("Press OK when " + next.getName()
                    + " is ready to begin their turn.");
            handoff.showAndWait();
        }

        updateForCurrentPlayer();
        currentTurnEndLogged = false;
        appendHumanTurnBanner();
        setPhase(next.getCurrentRoom() == null
                ? "Waiting to roll"
                : "In room — roll to exit or stay");
        rollDiceButton.setDisable(false);
        rollDiceButton.setVisible(true);
        rollDiceButton.setManaged(true);
    }

    private boolean hasActiveHuman() {
        for (Player p : controller.getPlayers()) {
            if (p instanceof HumanPlayer && p.isActive()) return true;
        }
        return false;
    }

    private int aiOnlyTurnCount = 0;

    /**
     * Maximum number of full rounds (each active AI gets one turn per round)
     * before the AI-only endgame gives up. With 3 AI players and 50 rounds,
     * that is up to 150 individual turns — enough time to explore and deduce.
     */
    private static final int AI_ONLY_MAX_ROUNDS = 100;

    private void runAIOnlyFinish() {
        if (aiOnlyTurnCount == 0) {
            appendLog("\nAll human players eliminated. AI resolving...");
        }
        hideTurnButtons();
        setPhase("AI resolving...");
        rollDiceButton.setDisable(true);

        // count active AI players to compute rounds from individual turns
        int activeAI = 0;
        for (Player p : controller.getPlayers()) {
            if (p.isActive()) activeAI++;
        }
        int maxTurns = AI_ONLY_MAX_ROUNDS * Math.max(activeAI, 1);

        if (controller.isGameOver() || aiOnlyTurnCount >= maxTurns) {
            if (!controller.isGameOver()) {
                appendLog("Game ended — no one solved the mystery.");
            }
            showGameOver();
            return;
        }

        aiOnlyTurnCount++;
        updateDisplayedPlayer(controller.getCurrentPlayer());
        List<String> aiLog = controller.runAITurn();
        for (String msg : aiLog) { appendLog(msg); }
        boardView.drawTokens();

        if (controller.isGameOver()) { showGameOver(); return; }
        controller.advanceToNextPlayer();

        scheduleAiContinuation(this::runAIOnlyFinish, 600);
    }

    private void hideTurnButtons() {
        boardView.clearHighlights();
        endMoveButton.setVisible(false);
        endMoveButton.setManaged(false);
        suggestButton.setVisible(false);
        suggestButton.setManaged(false);
        accuseButton.setVisible(false);
        accuseButton.setManaged(false);
        endTurnButton.setVisible(false);
        endTurnButton.setManaged(false);
        movesLabel.setText("");
    }

    private void logHumanTurnEndIfNeeded() {
        Player current = controller.getCurrentPlayer();
        if (!(current instanceof HumanPlayer) || !current.isActive()
                || currentTurnEndLogged) return;

        if (current.getCurrentRoom() != null) {
            appendLog(current.getName() + " ends the turn in the "
                    + current.getCurrentRoom().getName() + ".");
        } else {
            appendLog(current.getName() + " ends the turn at ("
                    + current.getRow() + ", " + current.getCol() + ").");
        }
        currentTurnEndLogged = true;
    }

    private void showHandoffPlaceholder(Player next) {
        turnLabel.setText("Pass to " + next.getName()
                + " (" + next.getToken() + ")");
        setPhase("Player handoff");
        handBox.getChildren().clear();
        Label prompt = new Label("  Hand hidden until " + next.getName()
                + " is ready.");
        prompt.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");
        handBox.getChildren().add(prompt);
    }

    private void updateForCurrentPlayer() {
        updateDisplayedPlayer(controller.getCurrentPlayer());
    }

    private void updateDisplayedPlayer(Player player) {
        displayedPlayer = player;
        turnLabel.setText(player.getName() + " (" + player.getToken() + ")");

        handBox.getChildren().clear();
        if (!(player instanceof HumanPlayer)) {
            Label aiLabel = new Label("  (AI hand hidden)");
            aiLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");
            handBox.getChildren().add(aiLabel);
            return;
        }

        notebookView.setCurrentPlayer(player);
        for (Card card : player.getHand()) {
            Label cardLabel = new Label("  " + card.getName());
            cardLabel.setStyle("-fx-text-fill: #aaddaa; -fx-font-size: 11;");
            handBox.getChildren().add(cardLabel);
        }
        if (player.getHand().isEmpty()) {
            Label emptyLabel = new Label("  (no cards)");
            emptyLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");
            handBox.getChildren().add(emptyLabel);
        }
    }

    private void updateMovesLabel() {
        if (controller.isMovementActive()) {
            movesLabel.setText("Moves left: "
                    + controller.getRemainingSteps());
        } else {
            movesLabel.setText("");
        }
    }

    public void appendLog(String message) {
        gameLog.appendText(message + "\n");
    }

    private void appendHumanTurnBanner() {
        if (controller.isCurrentPlayerHuman()) {
            appendLog("--- " + controller.getCurrentPlayer().getName()
                    + "'s turn ---");
        }
    }

    private void scheduleAiContinuation(Runnable continuation, int millis) {
        if (aiTurnPause != null) {
            aiTurnPause.stop();
            aiTurnPause = null;
        }

        // Running on the next JavaFX pulse is more reliable than chaining
        // several timed pauses, which could leave the UI appearing stuck in
        // "AI resolving..." if a transition was interrupted.
        Platform.runLater(continuation);
    }

    private Card chooseSuggestionCardForGui(Player disprover,
                                            List<Card> matchingCards,
                                            String askerName) {
        if (matchingCards == null || matchingCards.isEmpty()) {
            return null;
        }

        if (!(disprover instanceof HumanPlayer) || !hasActiveHuman()
                || matchingCards.size() == 1) {
            return matchingCards.get(0);
        }

        Player originalPlayer = displayedPlayer != null
                ? displayedPlayer
                : controller.getCurrentPlayer();
        String previousPhase = phaseLabel.getText();

        try {
            if (multipleHumans) {
                Alert handoff = new Alert(Alert.AlertType.INFORMATION);
                handoff.setTitle("Clue! - Private Card Choice");
                handoff.setHeaderText("Pass to " + disprover.getName());
                handoff.setContentText(disprover.getName()
                        + " must choose which card to show to " + askerName + ".");
                handoff.showAndWait();
            }

            updateDisplayedPlayer(disprover);
            setPhase("Private card choice");
            applyCss();
            layout();

            List<String> options = matchingCards.stream()
                    .map(Card::getName)
                    .toList();
            ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
            dialog.setTitle("Clue! - Choose Card to Show");
            dialog.setHeaderText(disprover.getName()
                    + ", choose one card to show to " + askerName + ".");
            dialog.setContentText("Matching cards:");
            Optional<String> result = dialog.showAndWait();

            String chosenName = result.orElse(options.get(0));
            for (Card card : matchingCards) {
                if (card.getName().equals(chosenName)) {
                    return card;
                }
            }

            return matchingCards.get(0);
        } finally {
            updateDisplayedPlayer(originalPlayer);
            if (multipleHumans) {
                Alert handoffBack = new Alert(Alert.AlertType.INFORMATION);
                handoffBack.setTitle("Clue! - Return Turn");
                handoffBack.setHeaderText("Pass back to " + askerName);
                handoffBack.setContentText("Press OK when " + askerName + " is ready.");
                handoffBack.showAndWait();
            }
            setPhase(previousPhase);
        }
    }
}
