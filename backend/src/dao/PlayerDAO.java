package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import database.PolynamesDatabase;
import models.Player;

public class PlayerDAO
{
    private PolynamesDatabase bdd;

    public PlayerDAO() throws SQLException
    {
        bdd = new PolynamesDatabase();
    }

    /**
     * Cherche un joueur dans la BDD
     * @param nom
     * @return Renvoie les joueurs avec pseudo correspondant
     * @throws SQLException 
     */    
    public ArrayList<Player> getPlayers(String nom) throws SQLException
    {
        // Cherche le pseudo dans la BDD
        PreparedStatement select = bdd.prepareStatement("SELECT id, nom FROM joueur WHERE nom=?;");
        select.setString(1, nom);
        ResultSet result = select.executeQuery();
        ArrayList<Player> players = new ArrayList<>();
        
        while(result.next())
        players.add(new Player(result.getInt(1), result.getString(2)));

        return players;
    }

    /**
     * Cherche un joueur dans la BDD
     * @param nom
     * @return Renvoie null si le joueur n'existe pas
     * @throws SQLException 
     */  
    public Player getPlayer(int id) throws SQLException
    {
        // Cherche le pseudo dans la BDD
        PreparedStatement select = bdd.prepareStatement("SELECT id,nom FROM joueur WHERE id=?;");
        select.setInt(1, id);
        ResultSet result = select.executeQuery();
        
        if(!result.next())
            return null; 

        return new Player(result.getInt(1), result.getString(2));
    }

    /**
     * Cherche un joueur dans la BDD
     * @param nom
     * @return Renvoie true si le joueur existe
     * @throws SQLException 
     */  
    public boolean exist(String nom) throws SQLException
    {
        PreparedStatement select = bdd.prepareStatement("SELECT COUNT(nom) FROM joueur WHERE nom=?;");
        select.setString(1, nom);
        ResultSet result = select.executeQuery();
        result.next();
        return result.getInt(1) > 0;
    }
    
    /**
     * 
     * @param nickname
     * @return Player ou null si existe déjà
     * @throws SQLException
     */
    public Player createPlayer(String nickname) throws SQLException
    {
        // Cherche le pseudo dans la BDD
        if(exist(nickname))
            return null;

        // Ajoute le pseudo dans la BDD
        PreparedStatement request = bdd.prepareStatement("INSERT INTO joueur (nom) VALUES (?);");
        request.setString(1, nickname);
        request.execute();

        // Renvoie le joueur
        return getPlayers(nickname).get(0);
    }
}
