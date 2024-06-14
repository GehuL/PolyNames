package models;

public record Card(int cardId, int gameId, int wordId, boolean revealed, ECardColor color) {}
