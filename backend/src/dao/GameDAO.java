package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.PolynamesDatabase;

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

    public boolean playerJoin(String code, int idPlayer) throws SQLException, JoinException
    {
        // Compte l'occurance de la partie dans la table JOUE et récupère l'ID de la partie si elle existe et que le joueur existe aussi
        PreparedStatement select = bdd.prepareStatement("SELECT COUNT(idPartie), partie.id, joueur.id FROM partie LEFT JOIN joue ON `idPartie`=`partie`.`id` LEFT JOIN joueur ON joueur.id=? WHERE partie.code=?");
        select.setInt(1, idPlayer);        
        select.setString(2, code);
        ResultSet resultSet = select.executeQuery();
        resultSet.next();

        // Vérifie si la partie existe, et qu'il reste de la place.
        int occurencePartie = resultSet.getInt(1);
        int idPartie = resultSet.getInt(2);
        int idJoueur = resultSet.getInt(3);

        if(idPartie == 0)
            throw new JoinException("Code de partie invalide", JoinException.Type.CODE_INVALID);
        
        if(occurencePartie >= 2)
            throw new JoinException("Nombre de joueur max atteint", JoinException.Type.MAX_PLAYER);

        if(idJoueur == 0)
            throw new JoinException("Joueur inconnue", JoinException.Type.PLAYER_INVALID);

        // TODO: Vérifier si le joueur est déjà dans une partie

        // ajoute le joueur dans la partie
        PreparedStatement request = bdd.prepareStatement("INSERT INTO joue (idPartie, idJoueur, role) VALUES (?, ?, 'guesser');");
        request.setInt(1, idPartie);
        request.setInt(2, idPlayer);
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
