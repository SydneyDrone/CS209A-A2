package application.controller;

import application.model.Board;
import application.model.Client;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

import static application.model.Board.*;

public class Controller implements Initializable {
    private static final int BOUND = 90;
    private static final int OFFSET = 15;
    @FXML
    public Text connectionText;
    @FXML
    public Text matchText;
    @FXML
    public Text gameplayText;
    @FXML
    public Button connectButton;
    @FXML
    private Pane base_square;
    @FXML
    private Rectangle game_panel;
    private Board board = new Board();
    private Client client;
    private Thread receiveThread;
    private boolean connect = false;

    EventHandler<MouseEvent> gameplay = event -> {
        int x = (int) (event.getX() / BOUND);
        int y = (int) (event.getY() / BOUND);
        if (x < 0 || 3 <= x || y < 0 || 3 <= y) return;
        takeStep(x, y, board.myPlayer);
        client.send("step " + x + " " + y + " " + board.myPlayer);
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game_panel.setOnMouseClicked(null);
        connectButton.setOnAction(event -> {
            if (!connect) {
                try {
                    client = new Client();
                    connect = true;
                    receiveThread = new Thread(() -> {
                        String msg;
                        while ((msg = client.receive()) != null) {
                            String tempMessage = msg;
                            Platform.runLater(() -> {
                                parseMessage(tempMessage);
                                connectionText.setText(tempMessage);
                            });
                        }
                        Platform.runLater(() -> connectionText.setText("Connection closed"));
                    });
                    receiveThread.start();
                } catch (Exception e) {
                    Platform.runLater(() -> connectionText.setText("Connection Failed: +" + e.getMessage()));
                }
            } else {
                Platform.runLater(() -> connectionText.setText("Already connected"));
            }
        });
    }

    private void parseMessage(String message) {
        String[] messages = message.split(" ");
        switch (messages[0]) {
            case "step":
                int x = Integer.parseInt(messages[1]);
                int y = Integer.parseInt(messages[2]);
                int player = Integer.parseInt(messages[3]);
                takeStep(x, y, player);
                break;
            case "match":
                changeMatchStatus(messages[1], messages[2]);
                break;
        }
    }

    private void changeMatchStatus(String status, String player) {
        switch (status) {
            case "success":
                Platform.runLater(() -> matchText.setText("Match success"));
                startGame(Integer.parseInt(player));
                break;
            case "waiting":
                Platform.runLater(() -> matchText.setText("Match waiting in room"));
                break;
            case "failure"://not used
                Platform.runLater(() -> matchText.setText("Match failure"));
                break;
        }
    }

    private void startGame(int player) {
        board = new Board(player);
        syncPanel();
    }

    private void syncPanel() {
        if (checkEnd()) return;
        if (board.myPlayer == board.currentPlayer) {
            Platform.runLater(() -> gameplayText.setText("Your turn"));
            game_panel.setOnMouseClicked(gameplay);
        } else {
            Platform.runLater(() -> gameplayText.setText("Opponent's turn"));
            game_panel.setOnMouseClicked(null);
        }
    }

    private boolean checkEnd() {
        int winner = board.check();
        if (winner != 0) {
            gameplayText.setText(winner == board.myPlayer ? "You win" : "You lose");
            game_panel.setOnMouseClicked(null);
            return true;
        } else if (board.isEnd()) {
            gameplayText.setText("Draw!");
            game_panel.setOnMouseClicked(null);
            return true;
        }
        return false;
    }

    private void takeStep(int x, int y, int player) {
        board.takeStep(x, y, player);
        drawChess(x, y, player);
        syncPanel();
    }

    private void drawChess(int i, int j, int player) {
        switch (player) {
            case PLAYER1:
                drawCircle(i, j);
                break;
            case PLAYER2:
                drawCross(i, j);
                break;
            case EMPTY:
                break;
            default:
                System.err.println("Invalid value!");
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
    }

    private void drawCross(int i, int j) {
        Line lineA = new Line();
        Line lineB = new Line();
        base_square.getChildren().add(lineA);
        base_square.getChildren().add(lineB);
        lineA.setStartX(i * BOUND + OFFSET * 1.5);
        lineA.setStartY(j * BOUND + OFFSET * 1.5);
        lineA.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        lineA.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        lineA.setStroke(Color.BLUE);

        lineB.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        lineB.setStartY(j * BOUND + OFFSET * 1.5);
        lineB.setEndX(i * BOUND + OFFSET * 1.5);
        lineB.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        lineB.setStroke(Color.BLUE);
    }
}
