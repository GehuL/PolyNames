package models;

public record Card(int gameId, int wordId, boolean revealed, ECardColor color) {}
