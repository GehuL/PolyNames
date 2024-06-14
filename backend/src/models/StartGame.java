package models;

import java.util.ArrayList;

// Envoyer lorsque la partie démarre
public record StartGame(EEtatPartie etat, EPlayerRole role, ArrayList<ClientCard> cards) {}
