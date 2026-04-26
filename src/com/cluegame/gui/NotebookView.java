package com.cluegame.gui;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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

    /**
     * Creates the detective notebook panel with checkboxes for all
     * suspects, weapons and rooms.
     */
    public NotebookView() {
        VBox content = new VBox(4);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Detective Notebook");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: white;");
        content.getChildren().addAll(title, new Separator());

        addSection(content, "Suspects", SUSPECTS);
        addSection(content, "Weapons", WEAPONS);
        addSection(content, "Rooms", ROOMS);

        setContent(content);
        setFitToWidth(true);
        setPrefWidth(190);
        setMinWidth(170);
        setStyle("-fx-background: #2b2b2b; -fx-border-color: #444444; "
                + "-fx-border-width: 0 1 0 0;");
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
        }
    }
}
