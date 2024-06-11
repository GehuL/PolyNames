package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import database.PolynamesDatabase;
import models.Word;

public class DictionnaireDAO
{
    private PolynamesDatabase bdd;

    public DictionnaireDAO() throws SQLException
    {
        bdd = new PolynamesDatabase();
    }

    /**
     * Renvoie un certain nombre de mots depuis la table Dictionnaire.
     * Tous les mots sont uniques
     * @param ammount Le nombre de mots
     * @return La liste des mots sans doublons
     * @throws SQLException
     */
    public ArrayList<Word> getRandomWords(int ammount) throws SQLException
    {
        ArrayList<Word> cards = new ArrayList<>();

        if(ammount <= 0)
            return cards;

        PreparedStatement statement = bdd.prepareStatement("SELECT id, mot FROM dictionnaire ORDER BY RAND() LIMIT ?");
        statement.setInt(1, ammount);

        ResultSet results = statement.executeQuery();
        while(results.next())
        {
            Word dicoCard = new Word(results.getInt("id"), results.getString("mot"));
            cards.add(dicoCard);
        }
        return cards;
    }
}
