package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.PolynamesDatabase;
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
     * Ajoute le joueur dans la partie
     * @param idPartie L'id de la partie
     * @param nickname Le pseudo du joueur le temps de la partie (temporaire)
     * @return Si le joueur est dans la partie
     * @throws SQLException
     * @throws JoinException
     */

    public boolean playerJoin(int idPartie, String nickname) throws SQLException, JoinException
    {
        // Compte l'occurance de la partie dans la table JOUE et récupère l'ID de la partie si elle existe
        PreparedStatement select = bdd.prepareStatement("SELECT COUNT(joueur.idPartie),partie.id FROM joueur RIGHT JOIN partie ON partie.id=joueur.idPartie WHERE partie.id=?;");
        select.setInt(1, idPartie);
        ResultSet resultSet = select.executeQuery();
        resultSet.next();

        // Vérifie si la partie existe, et qu'il reste de la place.
        int occurencePartie = resultSet.getInt(1);
        idPartie = resultSet.getInt(2);

        if(idPartie == 0)
            throw new JoinException("Code de partie invalide", JoinException.Type.CODE_INVALID);
        
        if(occurencePartie >= 2)
            throw new JoinException("Nombre de joueur max atteint", JoinException.Type.MAX_PLAYER);

        // ajoute le joueur dans la partie
        PreparedStatement request = bdd.prepareStatement("INSERT INTO joueur (idPartie, nom, role) VALUES (?, ?, 'maitre_intuition');");
        request.setInt(1, idPartie);
        request.setString(2, nickname);
        request.execute();
        return true;
    }

    public class JoinException extends Exception
    {
        enum Type
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
