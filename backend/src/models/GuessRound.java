package models;

/**
 * Décrit les infos après avoir cliqué sur une carte transmis par le serveur
 * color: La couleur de la carte cliquée
 * idCard: La carte qui à été cliquée
 */
public record GuessRound(EEtatPartie etatPartie, int score, int dejaTrouvee, ECardColor color, int idCard) {}
