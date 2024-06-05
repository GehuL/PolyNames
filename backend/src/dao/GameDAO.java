package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.PolynamesDatabase;
import models.Game;

public class GameDAO 
{
    private PolynamesDatabase bdd;

    public GameDAO() throws SQLException
    {
        bdd = new PolynamesDatabase();
    }

    public boolean createGame(String code) throws SQLException
    {
        PreparedStatement request = bdd.prepareStatement("INSERT INTO partie (code, score) VALUES (?, 0);");
        request.setString(1, code);
        request.execute();
        return true;
    }
    
    public Game getGame(String code)
    {
        // TODO: faire la fonction
        return null;
    }

    public void setRole(int idPlayer, String role) throws SQLException
    {
        PreparedStatement request = bdd.prepareStatement("UPDATE joueur SET role=? WHERE idJoueur=?;");
        request.setString(1, role);
        request.setInt(2, idPlayer);
        request.executeUpdate();
    }

    public boolean playerJoin(String code, String nickname) throws SQLException, JoinException
    {
        // Compte l'occurance de la partie dans la table JOUE et récupère l'ID de la partie si elle existe
        PreparedStatement select = bdd.prepareStatement("SELECT COUNT(joueur.idPartie),partie.id FROM joueur RIGHT JOIN partie ON partie.id=joueur.idPartie WHERE partie.code=?;");
        select.setString(1, code);
        ResultSet resultSet = select.executeQuery();
        resultSet.next();

        // Vérifie si la partie existe, et qu'il reste de la place.
        int occurencePartie = resultSet.getInt(1);
        int idPartie = resultSet.getInt(2);

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
