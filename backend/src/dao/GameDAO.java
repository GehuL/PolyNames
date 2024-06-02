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

    public boolean playerJoin(String code, int idPlayer) throws SQLException
    {
        // récupère l'id de partie
        PreparedStatement select = bdd.prepareStatement("SELECT id FROM partie WHERE code=?;");
        select.setString(1, code);
        ResultSet resultSet = select.executeQuery();
        
        // Vérifie si la partie existe
        if(resultSet.next() == false)
            return false;

        int idPartie = resultSet.getInt(1);

        // ajoute le joueur dans la partie
        PreparedStatement request = bdd.prepareStatement("INSERT INTO joue (idPartie, idJoueur, role) VALUES (?, ?, 'guesser');");
        request.setInt(1, idPartie);
        request.setInt(2, idPlayer);
        request.execute();
        return true;
    }

    /**
     * 
     * @param nickname
     * @return Id du joueur ou -1 si existe déjà
     * @throws SQLException
     */
    public int createPlayer(String nickname) throws SQLException
    {
        PreparedStatement select = bdd.prepareStatement("SELECT COUNT(nom) FROM joueur WHERE nom=?;");
        select.setString(1, nickname);
        ResultSet result = select.executeQuery();
        result.next();
        if(result.getInt(1) > 0) // Il y a déjà un pseudo
            return -1;

        PreparedStatement request = bdd.prepareStatement("INSERT INTO joueur (nom) VALUES (?);");
        request.setString(1, nickname);
        request.execute();

        request = bdd.prepareStatement("SELECT id FROM joueur WHERE nom=?;");
        request.setString(1, nickname);
        result = request.executeQuery();
        result.next();
        return result.getInt(1);
    }
}
