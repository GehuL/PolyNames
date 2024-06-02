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

        PreparedStatement joueSelect = bdd.prepareStatement("SELECT COUNT(*) FROM joue INNER JOIN partie ON partie.code=?");
        joueSelect.setString(1, code);
        ResultSet nbrJoueur = joueSelect.executeQuery();
        nbrJoueur.next();
        
        if(nbrJoueur.getInt(1) >= 2)
        {
            return false;
        }

        // TODO: Vérifier si le joueur est déjà dans une partie

        // ajoute le joueur dans la partie
        PreparedStatement request = bdd.prepareStatement("INSERT INTO joue (idPartie, idJoueur, role) VALUES (?, ?, 'guesser');");
        request.setInt(1, idPartie);
        request.setInt(2, idPlayer);
        request.execute();
        return true;
    }
}
