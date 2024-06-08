package dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import database.PolynamesDatabase;
import models.DicoCard;
import models.ECardColor;

public class CardDAO 
{
    private PolynamesDatabase bdd;

    public CardDAO() throws SQLException
    {
        bdd = new PolynamesDatabase();
    }

    /**
     * Ajoute une carte dans la table Carte. La carte n'est pas révélée par défaut.
     * @param idPartie L'id de la partie
     * @param card La carte contenant l'id du mot
     * @param color La couleur de la carte
     * @return True en cas de succés de la requête
     */
    public boolean addCard(int idPartie, DicoCard card, ECardColor color) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("INSERT INTO carte idPartie, idMot, couleur, revelee VALUES (?,?,?,false);");
        statement.setInt(1, idPartie);
        statement.setInt(2, card.id());
        statement.setString(3, color.toString());
        return statement.executeUpdate() > 0;
    }
}
