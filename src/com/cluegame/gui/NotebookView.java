package com.cluegame.gui;

import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detective notebook panel for manual card tracking. Shows checkboxes
 * for all suspects, weapons and rooms so the player can mark off
 * cards they have seen during the game.
 * @author Thanh Shaw
 */
public class NotebookView extends ScrollPane {

    private static final String[] SUSPECTS = {
        "Miss Scarlett", "Colonel Mustard", "Mrs White",
        "Reverend Green", "Mrs Peacock", "Professor Plum"
    };

    private static final String[] WEAPONS = {
        "Candlestick", "Dagger", "Lead Piping",
        "Revolver", "Rope", "Spanner"
    };

    private static final String[] ROOMS = {
        "Kitchen", "Ballroom", "Conservatory", "Billiard Room",
        "Library", "Study", "Hall", "Lounge", "Dining Room"
    };

    private final Label ownerLabel;
    private final List<CheckBox> checkBoxes;
    private final Map<String, boolean[]> notebookStates;
    private String currentOwner;

    /**
     * Creates the detective notebook panel with checkboxes for all
     * suspects, weapons and rooms.
     */
    public NotebookView(List<Player> players) {
        this.checkBoxes = new ArrayList<>();
        this.notebookStates = new HashMap<>();
        VBox content = new VBox(4);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Detective Notebook");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: white;");
        ownerLabel = new Label("Shared notebook");
        ownerLabel.setFont(Font.font("System", 11));
        ownerLabel.setStyle("-fx-text-fill: #888888;");
        content.getChildren().addAll(title, ownerLabel, new Separator());

        addSection(content, "Suspects", SUSPECTS);
        addSection(content, "Weapons", WEAPONS);
        addSection(content, "Rooms", ROOMS);

        setContent(content);
        setFitToWidth(true);
        setPrefWidth(190);
        setMinWidth(170);
        setStyle("-fx-background: #2b2b2b; -fx-border-color: #444444; "
                + "-fx-border-width: 0 1 0 0;");

        for (Player player : players) {
            if (player instanceof HumanPlayer) {
                notebookStates.put(player.getName(), new boolean[checkBoxes.size()]);
            }
        }
    }

    /**
     * Adds a labelled section of checkboxes to the notebook.
     */
    private void addSection(VBox parent, String sectionName, String[] items) {
        Label heading = new Label(sectionName);
        heading.setFont(Font.font("System", FontWeight.BOLD, 11));
        heading.setStyle("-fx-text-fill: #aaaaaa;");
        heading.setPadding(new Insets(6, 0, 2, 0));
        parent.getChildren().add(heading);

        for (String item : items) {
            CheckBox cb = new CheckBox(item);
            cb.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11;");
            cb.setPadding(new Insets(1, 0, 1, 4));
            parent.getChildren().add(cb);
            checkBoxes.add(cb);
        }
    }

    /**
     * Switches the notebook view to the given player's personal notes.
     * The previous player's checkbox state is saved before the new state loads.
     * AI turns leave the last human notebook visible.
     * @param player the player whose notebook should be shown
     */
    public void setCurrentPlayer(Player player) {
        if (!(player instanceof HumanPlayer)) {
            return;
        }

        if (currentOwner != null) {
            saveCurrentState();
        }

        currentOwner = player.getName();
        ownerLabel.setText("Notebook for " + currentOwner);
        boolean[] state = notebookStates.computeIfAbsent(
                currentOwner, ignored -> new boolean[checkBoxes.size()]);
        loadState(state);
    }

    private void saveCurrentState() {
        boolean[] state = notebookStates.computeIfAbsent(
                currentOwner, ignored -> new boolean[checkBoxes.size()]);
        for (int i = 0; i < checkBoxes.size(); i++) {
            state[i] = checkBoxes.get(i).isSelected();
        }
    }

    private void loadState(boolean[] state) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setSelected(state[i]);
        }
    }
}
