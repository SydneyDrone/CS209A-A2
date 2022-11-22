package application.model;

public class Player {
    public String account;
    //    public String password;
    public int id;
    public int gamePlayed;
    public int gameWon;

    public Player(String account, int id) {
        this();
        this.account = account;
        this.id = id;
    }

    public Player() {
        gamePlayed = 0;
        gameWon = 0;
    }
}
