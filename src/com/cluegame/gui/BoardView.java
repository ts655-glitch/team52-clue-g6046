package com.cluegame.gui;

import com.cluegame.model.Board;
import com.cluegame.model.Square;
import com.cluegame.players.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Displays the Clue! board image with player tokens drawn on a Canvas overlay.
 * The board scales responsively to fit the allocated layout space while
 * preserving its aspect ratio. Token positions are recalculated on each resize.
 * Supports highlighting valid move squares and click-to-move interaction.
 * Original board dimensions: 1296x1226 pixels, 24 columns x 25 rows.
 * @author Thanh Shaw
 */
public class BoardView extends Pane {

    private static final int BOARD_COLS = 24;
    private static final int BOARD_ROWS = 25;
    private static final double ORIGINAL_WIDTH = 1296.0;
    private static final double ORIGINAL_HEIGHT = 1226.0;
    private static final double BASE_TOKEN_RADIUS = 12.0;

    // grid offset within the board image (calibrated from board.png)
    // the playable grid does not start at pixel (0,0) due to the decorative border
    // 24 cols wide, 25 rows tall
    private static final double GRID_LEFT = 43.0;
    private static final double GRID_TOP = 14.0;
    private static final double CELL_W = 50.4;
    private static final double CELL_H = 48.0;

    /** Set to true to draw the debug grid overlay with cell types. */
    private static final boolean DEBUG_OVERLAY = false;

    /**
     * Returns the display colour for a player based on their character token.
     * @param token the short token name (e.g. "Scarlett")
     * @return the colour for that character
     */
    private static Color getTokenColour(String token) {
        switch (token) {
            case "Scarlett": return Color.RED;
            case "Mustard":  return Color.GOLDENROD;
            case "White":    return Color.GHOSTWHITE;
            case "Green":    return Color.GREEN;
            case "Peacock":  return Color.BLUE;
            case "Plum":     return Color.PURPLE;
            default:         return Color.GRAY;
        }
    }

    private ImageView boardImageView;
    private Canvas tokenCanvas;
    private List<Player> players;
    private List<int[]> highlightedSquares;
    private BiConsumer<Integer, Integer> clickListener;
    private Board board;
    private com.cluegame.model.Game game;

    // cached layout values for coordinate conversion
    private double displayWidth;
    private double displayHeight;
    private double offsetX;
    private double offsetY;

    /**
     * Creates a BoardView that loads the board image and sets up the
     * token overlay canvas. The board automatically scales to fit
     * whatever space it is given by its parent layout.
     * @param game the Game instance containing players and board data
     */
    public BoardView(com.cluegame.model.Game game) {
        this.game = game;
        this.players = game.getPlayers();
        this.board = game.getBoard();
        this.highlightedSquares = new ArrayList<>();

        // load the board image from the classpath
        InputStream imageStream = getClass().getResourceAsStream("/board.png");
        Image boardImage = new Image(imageStream);
        boardImageView = new ImageView(boardImage);
        boardImageView.setPreserveRatio(true);

        tokenCanvas = new Canvas(0, 0);
        tokenCanvas.setOnMouseClicked(this::onCanvasClicked);

        getChildren().addAll(boardImageView, tokenCanvas);
    }

    /**
     * Sets a listener that is called when the user clicks a board cell.
     * The listener receives the grid row and column that was clicked.
     * @param listener callback accepting (row, col)
     */
    public void setClickListener(BiConsumer<Integer, Integer> listener) {
        this.clickListener = listener;
    }

    /**
     * Sets the list of squares to highlight as valid move destinations.
     * Triggers a redraw of the overlay.
     * @param squares list of {row, col} arrays to highlight
     */
    public void setHighlights(List<int[]> squares) {
        this.highlightedSquares = squares;
        drawOverlay();
    }

    /**
     * Clears all highlighted squares and redraws.
     */
    public void clearHighlights() {
        this.highlightedSquares.clear();
        drawOverlay();
    }

    /**
     * Handles a mouse click on the canvas. Converts pixel coordinates
     * to grid row/col using the scaled grid offset, and notifies the
     * click listener. Rejects clicks outside the grid area.
     */
    private void onCanvasClicked(MouseEvent event) {
        if (clickListener == null || displayWidth <= 0 || displayHeight <= 0) return;

        double scale = displayWidth / ORIGINAL_WIDTH;
        double scaledGridLeft = GRID_LEFT * scale;
        double scaledGridTop = GRID_TOP * scale;
        double scaledCellW = CELL_W * scale;
        double scaledCellH = CELL_H * scale;

        // subtract grid offset from click position
        double gridX = event.getX() - scaledGridLeft;
        double gridY = event.getY() - scaledGridTop;

        if (gridX < 0 || gridY < 0) return;

        int col = (int) (gridX / scaledCellW);
        int row = (int) (gridY / scaledCellH);

        if (row >= 0 && row < BOARD_ROWS && col >= 0 && col < BOARD_COLS) {
            clickListener.accept(row, col);
        }
    }

    /**
     * Called by JavaFX during layout. Scales the board image and canvas
     * to fit within the actual allocated width and height, centered.
     */
    @Override
    protected void layoutChildren() {
        double availableWidth = getWidth();
        double availableHeight = getHeight();
        if (availableWidth <= 0 || availableHeight <= 0) return;

        // scale to fit whichever dimension is tighter
        double scaleX = availableWidth / ORIGINAL_WIDTH;
        double scaleY = availableHeight / ORIGINAL_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        displayWidth = ORIGINAL_WIDTH * scale;
        displayHeight = ORIGINAL_HEIGHT * scale;

        // center the board within the available space
        offsetX = (availableWidth - displayWidth) / 2;
        offsetY = (availableHeight - displayHeight) / 2;

        boardImageView.setFitWidth(displayWidth);
        boardImageView.setFitHeight(displayHeight);
        boardImageView.relocate(offsetX, offsetY);

        tokenCanvas.setWidth(displayWidth);
        tokenCanvas.setHeight(displayHeight);
        tokenCanvas.relocate(offsetX, offsetY);

        drawOverlay();
    }

    /**
     * Draws the full canvas overlay: highlighted squares then player tokens.
     * All positions use the scaled grid offset so elements align with
     * the actual board cells in the image.
     */
    public void drawOverlay() {
        if (displayWidth <= 0 || displayHeight <= 0) return;

        double scale = displayWidth / ORIGINAL_WIDTH;
        double scaledGridLeft = GRID_LEFT * scale;
        double scaledGridTop = GRID_TOP * scale;
        double scaledCellW = CELL_W * scale;
        double scaledCellH = CELL_H * scale;
        double tokenRadius = BASE_TOKEN_RADIUS * scale;
        double inset = 5 * scale;

        GraphicsContext gc = tokenCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, displayWidth, displayHeight);

        // debug overlay: grid lines, cell types, labels
        if (DEBUG_OVERLAY) {
            drawDebugOverlay(gc, scale, scaledGridLeft, scaledGridTop,
                    scaledCellW, scaledCellH);
        }

        // draw highlighted valid move squares — smaller inset keeps them
        // visually inside the corridor cells even where room decorations
        // overlap the logical grid boundary
        gc.setFill(Color.rgb(0, 200, 0, 0.3));
        gc.setStroke(Color.LIMEGREEN);
        gc.setLineWidth(2.5 * scale);
        for (int[] sq : highlightedSquares) {
            double sx = scaledGridLeft + sq[1] * scaledCellW + inset;
            double sy = scaledGridTop + sq[0] * scaledCellH + inset;
            double w = scaledCellW - 2 * inset;
            double h = scaledCellH - 2 * inset;
            gc.fillRect(sx, sy, w, h);
            gc.strokeRect(sx, sy, w, h);
        }

        // draw player tokens — players in rooms get stable in-room positions
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Color colour = getTokenColour(player.getToken());

            double x, y;
            if (player.getCurrentRoom() != null) {
                // place token inside the room at a stable slot position
                double[] pos = getRoomTokenPosition(player, i, scaledGridLeft,
                        scaledGridTop, scaledCellW, scaledCellH);
                x = pos[0];
                y = pos[1];
            } else {
                x = scaledGridLeft + (player.getCol() + 0.5) * scaledCellW;
                y = scaledGridTop + (player.getRow() + 0.5) * scaledCellH;
            }

            gc.setFill(colour);
            gc.fillOval(x - tokenRadius, y - tokenRadius,
                    tokenRadius * 2, tokenRadius * 2);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2 * scale);
            gc.strokeOval(x - tokenRadius, y - tokenRadius,
                    tokenRadius * 2, tokenRadius * 2);
        }

        // draw non-player suspect pieces (smaller, semi-transparent)
        if (game != null) {
            double npcRadius = tokenRadius * 0.7;
            for (java.util.Map.Entry<String, int[]> entry
                    : game.getNonPlayerSuspects().entrySet()) {
                String token = entry.getKey();
                int[] pos = entry.getValue();
                Color colour = getTokenColour(token);

                double x = scaledGridLeft + (pos[1] + 0.5) * scaledCellW;
                double y = scaledGridTop + (pos[0] + 0.5) * scaledCellH;

                gc.setGlobalAlpha(0.5);
                gc.setFill(colour);
                gc.fillOval(x - npcRadius, y - npcRadius,
                        npcRadius * 2, npcRadius * 2);
                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(1.5 * scale);
                gc.strokeOval(x - npcRadius, y - npcRadius,
                        npcRadius * 2, npcRadius * 2);
                gc.setGlobalAlpha(1.0);
            }

            // draw weapon tokens as small text labels in rooms
            double fontSize = Math.max(7, 9 * scale);
            gc.setFont(Font.font("System", fontSize));
            gc.setFill(Color.rgb(255, 200, 50, 0.9));
            int weaponIndex = 0;
            for (java.util.Map.Entry<String, String> entry
                    : game.getWeaponPositions().entrySet()) {
                String weapon = entry.getKey();
                String roomName = entry.getValue();

                // find room center for placement
                for (int ri = 0; ri < ROOM_NAMES_ORDERED.length; ri++) {
                    if (ROOM_NAMES_ORDERED[ri].equals(roomName)) {
                        double cx = scaledGridLeft
                                + (ROOM_CENTER_COORDS[ri][1] + 0.5) * scaledCellW;
                        double cy = scaledGridTop
                                + (ROOM_CENTER_COORDS[ri][0] + 1.5) * scaledCellH;
                        // offset each weapon vertically
                        int count = 0;
                        for (String wr : game.getWeaponPositions().values()) {
                            if (wr.equals(roomName)) count++;
                        }
                        gc.fillText(weapon, cx - 20 * scale, cy + weaponIndex * fontSize * 0.3);
                        break;
                    }
                }
                weaponIndex++;
            }
        }
    }

    /**
     * Draws the debug grid overlay showing cell types from Board.java.
     * Grid lines in grey, doors outlined in cyan, blocked in red,
     * rooms in a subtle tint, and row/col labels on corridor cells.
     */
    private void drawDebugOverlay(GraphicsContext gc, double scale,
            double scaledGridLeft, double scaledGridTop,
            double scaledCellW, double scaledCellH) {
        if (board == null) return;

        // draw cell type overlays
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                double cx = scaledGridLeft + col * scaledCellW;
                double cy = scaledGridTop + row * scaledCellH;

                Square sq = board.getSquare(row, col);
                boolean isDoor = board.isDoor(row, col);

                if (isDoor) {
                    // door: cyan fill + border
                    gc.setFill(Color.rgb(0, 255, 255, 0.25));
                    gc.fillRect(cx, cy, scaledCellW, scaledCellH);
                    gc.setStroke(Color.CYAN);
                    gc.setLineWidth(2 * scale);
                    gc.strokeRect(cx + 1, cy + 1, scaledCellW - 2, scaledCellH - 2);
                } else if (sq.getType() == Square.Type.BLOCKED) {
                    // blocked: red fill + border
                    gc.setFill(Color.rgb(255, 0, 0, 0.2));
                    gc.fillRect(cx, cy, scaledCellW, scaledCellH);
                    gc.setStroke(Color.RED);
                    gc.setLineWidth(1.5 * scale);
                    gc.strokeRect(cx + 1, cy + 1, scaledCellW - 2, scaledCellH - 2);
                } else if (sq.getType() == Square.Type.ROOM) {
                    // room interior: visible blue tint with border
                    gc.setFill(Color.rgb(0, 80, 255, 0.2));
                    gc.fillRect(cx, cy, scaledCellW, scaledCellH);
                    gc.setStroke(Color.rgb(100, 150, 255, 0.4));
                    gc.setLineWidth(0.5);
                    gc.strokeRect(cx, cy, scaledCellW, scaledCellH);
                } else if (sq.getType() == Square.Type.START) {
                    // start: yellow border
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(1.5 * scale);
                    gc.strokeRect(cx + 1, cy + 1, scaledCellW - 2, scaledCellH - 2);
                }
            }
        }

        // draw grid lines
        gc.setStroke(Color.rgb(255, 255, 255, 0.3));
        gc.setLineWidth(0.5);
        for (int col = 0; col <= BOARD_COLS; col++) {
            double x = scaledGridLeft + col * scaledCellW;
            gc.strokeLine(x, scaledGridTop, x, scaledGridTop + BOARD_ROWS * scaledCellH);
        }
        for (int row = 0; row <= BOARD_ROWS; row++) {
            double y = scaledGridTop + row * scaledCellH;
            gc.strokeLine(scaledGridLeft, y, scaledGridLeft + BOARD_COLS * scaledCellW, y);
        }

        // draw row/col labels on corridor and start cells
        double fontSize = Math.max(7, 8 * scale);
        gc.setFont(Font.font("Monospaced", fontSize));
        gc.setFill(Color.rgb(255, 255, 255, 0.7));
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                Square sq = board.getSquare(row, col);
                if (sq.getType() == Square.Type.CORRIDOR
                        || sq.getType() == Square.Type.START
                        || board.isDoor(row, col)) {
                    double tx = scaledGridLeft + col * scaledCellW + 2 * scale;
                    double ty = scaledGridTop + row * scaledCellH + fontSize + 1 * scale;
                    gc.fillText(row + "," + col, tx, ty);
                }
            }
        }
    }

    // Room center positions (row, col) for placing tokens inside rooms.
    // Each room has a center point; tokens are offset from this center
    // in a grid pattern so they don't overlap.
    private static final double[][] ROOM_CENTERS = {
        // {centerRow, centerCol} — approximate visual center of each room
        // Study (0,0)-(3,6)
        // Hall (0,9)-(6,14)
        // Lounge (0,17)-(5,23)
        // Library (6,0)-(10,6)
        // Billiard Room (12,0)-(16,5)
        // Dining Room (9,16)-(14,23)
        // Conservatory (19,0)-(23,5)
        // Ballroom (17,8)-(22,15)
        // Kitchen (18,18)-(23,23)
    };

    private static final String[] ROOM_NAMES_ORDERED = {
        "Study", "Hall", "Lounge", "Library", "Billiard Room",
        "Dining Room", "Conservatory", "Ballroom", "Kitchen"
    };

    private static final double[][] ROOM_CENTER_COORDS = {
        {1.5, 3.0},   // Study
        {3.0, 11.5},  // Hall
        {2.5, 20.0},  // Lounge
        {8.0, 3.0},   // Library
        {14.0, 2.5},  // Billiard Room
        {11.5, 19.5}, // Dining Room
        {21.0, 2.5},  // Conservatory
        {19.5, 11.5}, // Ballroom
        {20.5, 20.5}  // Kitchen
    };

    // Offsets for up to 6 tokens within a room so they don't stack
    private static final double[][] TOKEN_OFFSETS = {
        {-0.8, -0.8}, {0.8, -0.8}, {-0.8, 0.8},
        {0.8, 0.8}, {0.0, -0.8}, {0.0, 0.8}
    };

    /**
     * Returns the pixel position for a player's token inside their room.
     * Each player gets a distinct offset from the room center so tokens
     * don't overlap.
     * @param player the player in a room
     * @param playerIndex the player's index in the players list
     * @param sgl scaled grid left
     * @param sgt scaled grid top
     * @param scw scaled cell width
     * @param sch scaled cell height
     * @return {x, y} pixel position
     */
    private double[] getRoomTokenPosition(Player player, int playerIndex,
            double sgl, double sgt, double scw, double sch) {
        String roomName = player.getCurrentRoom().getName();

        // find the room center
        double centerRow = player.getRow();
        double centerCol = player.getCol();
        for (int i = 0; i < ROOM_NAMES_ORDERED.length; i++) {
            if (ROOM_NAMES_ORDERED[i].equals(roomName)) {
                centerRow = ROOM_CENTER_COORDS[i][0];
                centerCol = ROOM_CENTER_COORDS[i][1];
                break;
            }
        }

        // apply offset based on player index
        int slot = playerIndex % TOKEN_OFFSETS.length;
        double offsetRow = TOKEN_OFFSETS[slot][0];
        double offsetCol = TOKEN_OFFSETS[slot][1];

        double x = sgl + (centerCol + offsetCol + 0.5) * scw;
        double y = sgt + (centerRow + offsetRow + 0.5) * sch;
        return new double[]{x, y};
    }

    /**
     * Redraws tokens and highlights. Convenience method for external callers.
     */
    public void drawTokens() {
        drawOverlay();
    }
}
