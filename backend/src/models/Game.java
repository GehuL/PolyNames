package models;

public record Game(int id, String code, int score, String indiceCourant, int doitDeviner, int dejaTrouvee, EEtatPartie etat) {}
