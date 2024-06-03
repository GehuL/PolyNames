package models;

import java.util.ArrayList;

public record Game(int id, String code, int core, ArrayList<Player> players, ArrayList<Card> cards) {}
