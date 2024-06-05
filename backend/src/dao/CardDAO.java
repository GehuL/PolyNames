package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.PolynamesDatabase;
import models.Card;
import models.ECardColor;

public class CardDAO 
{
    private PolynamesDatabase bdd;

    public CardDAO() throws SQLException
    {
        bdd = new PolynamesDatabase();
    }

    public void generateCards(int idPartie) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("SELECT id, mot FROM dictionnaire ORDER BY RAND() LIMIT 25");
        ResultSet result = statement.executeQuery();
        
        for(int i = 0; i < 8; i++)
        {
            Card card = new Card(result.getInt(1), result.getString(2), false, ECardColor.BLUE);
           // PreparedStatement insertCard = bdd.prepareStatement("INSERT INTO carte (idMot, idPartie, couleur)")
        }
        
        while(result.next())
        {
        }
    }
}
