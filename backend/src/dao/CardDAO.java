package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import database.PolynamesDatabase;
import models.Card;
import models.ClientCard;
import models.ECardColor;

public class CardDAO 
{
    private PolynamesDatabase bdd;

    public CardDAO() throws SQLException
    {
        bdd = new PolynamesDatabase();
    }

    /**
     * Ajoute une carte dans la table Carte.
     * @return True en cas de succés de la requête
     */
    public boolean addCard(Card card) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("INSERT INTO carte (idPartie, idMot, couleur, revelee) VALUES (?,?,?,?);");
        statement.setInt(1, card.gameId());
        statement.setInt(2, card.wordId());
        statement.setString(3, card.color().toString());
        statement.setBoolean(4, card.revealed());
        return statement.executeUpdate() > 0;
    }

    /**
     * Compte le nombre de carte qui sont révélées pra le maitre des intuitions.
     * @param idPartie
     * @return
     * @throws SQLException 
     */
    public int countCardRevelead(int idPartie) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("SELECT COUNT(revelee) FROM carte WHERE idPartie=? AND revelee=TRUE;");
        statement.setInt(1, idPartie);
        ResultSet result = statement.executeQuery();
        result.next();
        return result.getInt(1);
    }

    /**
     * Renvoie les cartes associés à une partie.
     * @param idPartie
     * @return La liste des cartes. Peut être vide si la partie n'existe pas ou qu'il n'y a pas encore de carte.
     * @throws SQLException
     */
    public ArrayList<ClientCard> getCards(int idPartie) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("SELECT mot,couleur,carte.id FROM carte INNER JOIN dictionnaire ON dictionnaire.id=carte.idMot WHERE idPartie=?;");
        statement.setInt(1, idPartie);
        ResultSet result = statement.executeQuery();
        ArrayList<ClientCard> cards = new ArrayList<>();
        while(result.next())
        {
            cards.add(new ClientCard(result.getString("dictionnaire.mot"), 
                                     result.getInt("id"),
                                     ECardColor.valueOf(result.getString("couleur"))));
        }
        return cards;
    }

    /**
     * Obtient une carte d'une partie depuis la BDD 
     * @param idPartie 
     * @param idCarte
     * @return Card ou null si la partie n'existe pas ou la carte
     * @throws SQLException
     */
    public Card getCard(int idPartie, int idCarte) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("SELECT * FROM carte WHERE idPartie=? AND id=?;");
        statement.setInt(1, idPartie);
        statement.setInt(2, idCarte);

        ResultSet result = statement.executeQuery();
        if(!result.next())
            return null;

        return new Card(result.getInt("id"), 
                    idPartie, 
                    idCarte, 
                    result.getBoolean("revelee"), 
                    ECardColor.valueOf(result.getString("couleur")));
    }

    public boolean revealCard(int idPartie, int idCard) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("UPDATE carte SET revelee=? WHERE idPartie=? AND id=?;");
        statement.setBoolean(1, true);
        statement.setInt(2, idPartie);
        statement.setInt(3, idCard);
        return statement.executeUpdate() > 0;
    }
}
