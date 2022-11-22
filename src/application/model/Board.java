package application.model;


public class Board {
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = -1;
    public static final int EMPTY = 0;
    int[][] board;
    public int currentPlayer;
    public int myPlayer;
    int turnCount;
    int winner;


    public Board(int[][] board) {
        this();
        this.board = board;
    }

    public Board(int myPlayer) {
        this();
        this.myPlayer = myPlayer;
    }

    public Board() {
        this.board = new int[3][3];
        currentPlayer = 1;
        turnCount = 0;
        winner = 0;
    }

    public boolean isEnd() {
        return turnCount == 9;
    }

    public int check() {
        for (int i = 0; i < 3; i++) {
            int row = 0;
            int column = 0;
            for (int j = 0; j < 3; j++) {
                row += board[i][j];
                column += board[j][i];
            }
            if (row == 3 || column == 3) {
                return 1;
            } else if (row == -3 || column == -3) {
                return -1;
            }
        }

        int diagonal1 = 0;
        int diagonal2 = 0;
        for (int i = 0; i < 3; i++) {
            diagonal1 += board[i][i];
            diagonal2 += board[i][2 - i];
        }
        if (diagonal1 == 3 || diagonal2 == 3) {
            return 1;
        } else if (diagonal1 == -3 || diagonal2 == -3) {
            return -1;
        }

        return 0;
    }

    public int takeStep(int x, int y, int player) {
        if (player != PLAYER1 && player != PLAYER2) return 1;
        if (player != currentPlayer) return 2;
        if (board[x][y] != 0) return 3;
        board[x][y] = player;
        turnCount++;
        currentPlayer = -currentPlayer;
        winner = check();
        return 0;
    }
}
