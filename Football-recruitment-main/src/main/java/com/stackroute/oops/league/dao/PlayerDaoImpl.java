package com.stackroute.oops.league.dao;

import com.stackroute.oops.league.exception.PlayerAlreadyExistsException;
import com.stackroute.oops.league.exception.PlayerNotFoundException;
import com.stackroute.oops.league.model.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is implementing the PlayerDao interface
 * This class has one field playerList and a String constant for storing file name
 */
public class PlayerDaoImpl implements PlayerDao {
    private static final String PLAYER_FILE_NAME = "src/main/resources/player.csv";
    private List<Player> playerList;

    /**
     * Constructor to initialize an empty ArrayList for playerList
     */
    public PlayerDaoImpl() {
        playerList=new ArrayList<Player>();
    }

    /**
     * Return true if  player object is stored in "player.csv" as comma separated fields successfully
     * when password length is greater than six and yearExpr is greater than zero
     */
    @Override
    public boolean addPlayer(Player player) throws PlayerAlreadyExistsException {
        boolean operation=false;
        Player findPlayer=null;
        try{
            findPlayer=findPlayer(player.getPlayerId());
        }
        catch(PlayerNotFoundException e)
        {
            System.out.println("");
        }
        if(findPlayer!=null)
            throw new PlayerAlreadyExistsException();
        if (player.getPassword().length() > 6 && player.getYearExpr() > 0) {
                try {
                        FileWriter w = new FileWriter(PLAYER_FILE_NAME,true);
                        w.append(player.getPlayerId() + ", " + player.getPlayerName() + ", " + player.getPassword() + ", " + player.getYearExpr() +", "+player.getTeamTitle()+ "\n");
                        w.close();
                    operation=true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        return operation;
    }

    //Return the list of player objects by reading data from the file "player.csv"
    @Override
    public List<Player> getAllPlayers() {
        playerList.clear();
        List<Player> players=new ArrayList<>();
        String[] str=null;
        String line=null;
        try (BufferedReader br = new BufferedReader(new FileReader(PLAYER_FILE_NAME))) {
            while ((line = br.readLine()) != null) {
                str=line.split(", ");
                    Player plr=new Player(str[0],str[1],str[2],Integer.parseInt(str[3]));
                    plr.setTeamTitle(str[4]);
                players.add(plr);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        return players;
    }

    /**
     * Return Player object given playerId to search
     */
    @Override
    public Player findPlayer(String playerId) throws PlayerNotFoundException {
        List<Player> lists=new ArrayList<>();
        lists=getAllPlayers();
        if(lists.isEmpty())
        {
            throw new PlayerNotFoundException();
        }
            for (int i = 0; i < lists.size(); i++) {
                if (playerId == null || !playerId.equalsIgnoreCase(lists.get(i).getPlayerId()) || playerId == "") {
                    throw new PlayerNotFoundException();
                } else {
                    return lists.get(i);
                }
            }
        return null;
    }
}
