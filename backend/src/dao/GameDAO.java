package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.PolynamesDatabase;
import models.Clue;
import models.EEtatPartie;
import models.Game;

public class GameDAO 
{
    private PolynamesDatabase bdd;

    public GameDAO() throws SQLException
    {
        bdd = new PolynamesDatabase();
    }

    /**
     * Créer une partie 
     * @param code Le code de la partie /!\ Doit être unique
     * @return
     * @throws SQLException
     */
    public boolean createGame(String code) throws SQLException
    {
        PreparedStatement request = bdd.prepareStatement("INSERT INTO partie (code, score) VALUES (?, 0);");
        request.setString(1, code);
        request.execute();
        return true;
    }
    
    /**
     * Cherche une partie
     * @param code le code de la partie
     * @return La partie ou null si elle n'existe pas
     * @throws SQLException Si erreur SQL
     */
    public Game getGame(String code) throws SQLException
    {
        PreparedStatement request = bdd.prepareStatement("SELECT * FROM partie WHERE code=?;");
        request.setString(1, code);
        ResultSet result = request.executeQuery();

        if(!result.next())
            return null;
        
        result.findColumn("id");
        return new Game(result.getInt("id"), 
                        result.getString("code"), 
                        result.getInt("score"), 
                        result.getString("indiceCourant"), 
                        result.getInt("doitDeviner"),
                        result.getInt("dejaTrouvee"),
                        EEtatPartie.valueOf(result.getString("etat")));
    }

    /**
     * Cherche une partie
     * @param id L'id de la partie
     * @return La partie ou null si elle n'existe pas
     * @throws SQLException Si erreur SQL
     */
    public Game getGame(int id) throws SQLException
    {
        PreparedStatement request = bdd.prepareStatement("SELECT * FROM partie WHERE id=?;");
        request.setInt(1, id);
        ResultSet result = request.executeQuery();
       
        if(!result.next())
            return null;

        result.findColumn("id");
        return new Game(result.getInt("id"), 
                        result.getString("code"), 
                        result.getInt("score"), 
                        result.getString("indiceCourant"), 
                        result.getInt("doitDeviner"),
                        result.getInt("dejaTrouvee"),
                        EEtatPartie.valueOf(result.getString("etat")));
    }

    /**
     * @return Le nombre de partie en cours dans la BDD
     * @throws SQLException
     */
    public int getGameCount() throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("SELECT COUNT(*) FROM partie;");
        ResultSet result = statement.executeQuery();
        result.next();
        return result.getInt(1);
    }
 
    /**
     * Définie l'état de la partie courante
     * @param idPartie
     * @return Si la partie à été modifié
     * @throws SQLException
     */
    public boolean setState(int idPartie, EEtatPartie etatPartie) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("UPDATE partie SET etat=? WHERE id=?;");
        statement.setString(1, etatPartie.toString());
        statement.setInt(2, idPartie);
        return statement.executeUpdate() > 0;
    }

    /**
     * Définine l'indice courant et le nombre de mots à trouver.
     * @param idPartie L'ID de la partie
     * @param clue  L'indice
     * @return Si la partie à été modifié
     * @throws SQLException 
     */
    public boolean setClue(int idPartie, Clue clue) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("UPDATE partie SET indiceCourant=?,doitDeviner=? WHERE id=?;");
        statement.setString(1, clue.clue());
        statement.setInt(2, clue.toFind());
        statement.setInt(3, idPartie);
        return statement.executeUpdate() > 0;
    }

    public boolean setDejaTrouvee(int idPartie, int dejaTrouvee) throws SQLException
    {
        PreparedStatement statement = bdd.prepareStatement("UPDATE partie SET dejaTrouvee=? WHERE id=?;");
        statement.setInt(1, dejaTrouvee);
        statement.setInt(2, idPartie);
        return statement.executeUpdate() > 0;
    }

    /**
     * Définie le score de la partie
     * @param idPartie
     * @param score
     * @throws SQLException 
     */
    public boolean setScore(int idPartie, int score) throws SQLException 
    {
        PreparedStatement statement = bdd.prepareStatement("UPDATE partie SET score=? WHERE id=?;");
        statement.setInt(1, score);
        statement.setInt(2, idPartie);
        return statement.executeUpdate() > 0;
    }

    public class JoinException extends Exception
    {
        public enum Type
        {
            MAX_PLAYER,
            CODE_INVALID,
            PLAYER_INVALID
        }

        private Type type;

        public JoinException(String message, Type type) {
            super(message);
            this.type=type;
        }

        public Type getType()
        {
            return type;
        }
    }
}
