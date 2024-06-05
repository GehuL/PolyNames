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
     * Renvoie les joueurs présents dans une partie.
     * @param code Le code d'une partie
     * @return La liste des joueurs dans la partie, peut être vide
     * @throws SQLException 
     */
    public ArrayList<Player> getPlayers(String code) throws SQLException
    {
        // récupère les joueurs dans la partie correspondante
        PreparedStatement request = bdd.prepareStatement("SELECT joueur.id, joueur.idPartie, nom, role FROM joueur INNER JOIN partie ON joueur.idPartie=partie.id WHERE partie.code=?;");
        request.setString(1, code);
        ResultSet result = request.executeQuery();
        ArrayList<Player> players = new ArrayList<>();
        while(result.next())
            players.add(new Player(result.getInt(1), result.getInt(2) , result.getString(3), result.getString(4)));
        return players;
    }

    /**
     * Définie le role du joeur ('maitre_mot' ou 'maitre_intuition')
     * @param idPlayer L'id du joueur
     * @param role Le role 
     * @return Renvoie true si le joueur à été modifié, sinon false (si le joueur n'existe pas)
     * @throws SQLException
     */
    public boolean setRole(int idPlayer, String role) throws SQLException
    {
        PreparedStatement request = bdd.prepareStatement("UPDATE joueur SET role=? WHERE id=?;");
        request.setString(1, role);
        request.setInt(2, idPlayer);
        return request.executeUpdate() > 0;
    }

    /**
     * Cherche un joueur dans la BDD avec l'id en paramètre.
     * @param nom
     * @return Renvoie null si le joueur n'existe pas
     * @throws SQLException 
     */  
    public Player getPlayer(int id) throws SQLException
    {
        // Cherche le pseudo dans la BDD
        PreparedStatement select = bdd.prepareStatement("SELECT joueur.id, joueur.idPartie, role, nom  FROM joueur WHERE id=?;");
        select.setInt(1, id);
        ResultSet result = select.executeQuery();
        
        if(!result.next())
            return null; 

        return new Player(result.getInt(1), result.getInt(2) , result.getString(3), result.getString(4));
    }

    /** 
     * Cherche un joueur dans la BDD
     * @param nom
     * @return Renvoie true si le joueur existe
     * @throws SQLException 
     */ 
    @Deprecated 
    public boolean exist(String nom) throws SQLException
    {
        PreparedStatement select = bdd.prepareStatement("SELECT COUNT(nom) FROM joueur WHERE nom=?;");
        select.setString(1, nom);
        ResultSet result = select.executeQuery();
        result.next();
        return result.getInt(1) > 0;
    }
    
    /**
     *  Créer un joueur temporaire. (Supprimé quand la partie est supprimé)
     * @param nickname
     * @throws SQLException
     */
    public void createPlayer(String nickname, int partieId, String role) throws SQLException
    {
        PreparedStatement request = bdd.prepareStatement("INSERT INTO joueur (idPartie, nom, role) VALUES (?, ?, 'maitre_intuition');");
        request.setInt(1, partieId);
        request.setString(2, nickname);
        request.setString(3, role);
        request.execute();
    }
}
