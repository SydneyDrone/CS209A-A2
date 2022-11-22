package application.model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    public static final int CAPACITY = 2;
    public List<Player> players;
    public int id;

    public Room(int id) {
        players = new ArrayList<>();
        this.id = id;
    }

    public Room() {
        this(0);
    }
    public boolean isFull() {
        return players.size() == CAPACITY;
    }

    public void addPlayer(Player player) {
        if (!isFull()) {
            players.add(player);
        }
    }
}
