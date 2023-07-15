package com.stackroute.oops.league.service;

import com.stackroute.oops.league.dao.PlayerDao;
import com.stackroute.oops.league.dao.PlayerDaoImpl;
import com.stackroute.oops.league.dao.PlayerTeamDao;
import com.stackroute.oops.league.dao.PlayerTeamDaoImpl;
import com.stackroute.oops.league.exception.PlayerAlreadyAllottedException;
import com.stackroute.oops.league.exception.PlayerAlreadyExistsException;
import com.stackroute.oops.league.exception.PlayerNotFoundException;
import com.stackroute.oops.league.exception.TeamAlreadyFormedException;
import com.stackroute.oops.league.model.Player;
import com.stackroute.oops.league.model.PlayerTeam;
import com.sun.source.doctree.SeeTree;
import jdk.jfr.Registered;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class implements leagueTeamService
 * This has four fields: playerDao, playerTeamDao and registeredPlayerList and playerTeamSet
 */
public class LeagueTeamServiceImpl implements LeagueTeamService {
    PlayerDao playerDao;
    PlayerTeamDao playerTeamDao;
    List<Player> registeredPlayerList;
    Set<PlayerTeam> playerTeamSet;
    /**
     * Constructor to initialize playerDao, playerTeamDao
     * empty ArrayList for registeredPlayerList and empty TreeSet for playerTeamSet
     */
    public LeagueTeamServiceImpl() {
        playerDao=null;
        playerTeamDao=null;
        registeredPlayerList=new ArrayList<>();
        playerTeamSet=new TreeSet<>();
    }

    //Add player data to file using PlayerDao object
    @Override
    public boolean addPlayer(Player player) throws PlayerAlreadyExistsException {
        boolean flag=false;
        if(player!=null) {
            playerDao.addPlayer(player);
            flag=true;
        }
        return flag;
    }

    /**
     * Register the player for the given teamTitle
     * Throws PlayerNotFoundException if the player does not exists
     * Throws PlayerAlreadyAllottedException if the player is already allotted to team
     * Throws TeamAlreadyFormedException if the maximum number of players has reached for the given teamTitle
     * Returns null if there no players available in the file "player.csv"
     * Returns "Registered" for successful registration
     * Returns "Invalid credentials" when player credentials are wrong
     */
    //@Override
    public synchronized String registerPlayerToLeague(String playerId, String password, LeagueTeamTitles teamTitle)
            throws PlayerNotFoundException, TeamAlreadyFormedException, PlayerAlreadyAllottedException {
        Player findPlayer=null;
        try{
            findPlayer=playerDao.findPlayer(playerId);
        }
        catch (PlayerNotFoundException e){
            throw new PlayerNotFoundException();
        }
        if(findPlayer!=null){
            if(findPlayer.getPlayerId().equals(playerId)&&findPlayer.getPassword().equals(password)){
                playerTeamSet=playerTeamDao.getAllPlayerTeams();
                PlayerTeam players = new PlayerTeam(playerId,teamTitle.getTitle());
                if(playerTeamSet.contains(players)){
                    throw new PlayerAlreadyAllottedException();
                }
                else {
                    findPlayer.setTeamTitle(teamTitle.getTitle());
                    long happyfeetCount =registeredPlayerList.stream().filter(player->player.getTeamTitle().equals(LeagueTeamTitles.HAPPYFEET)).count();
                    long win2winCount =registeredPlayerList.stream().filter(player->player.getTeamTitle().equals(LeagueTeamTitles.WIN2WIN)).count();
                    long hiphopCount =registeredPlayerList.stream().filter(player->player.getTeamTitle().equals(LeagueTeamTitles.HIPHOP)).count();
                    long luckystrikeCount =registeredPlayerList.stream().filter(player->player.getTeamTitle().equals(LeagueTeamTitles.LUCKYSTRIKE)).count();

                    if(happyfeetCount>12||win2winCount>12||hiphopCount>12||luckystrikeCount>12) {
                        throw new TeamAlreadyFormedException();
                    }
                    else{

                        registeredPlayerList.add(findPlayer);
                       
                    }
                    return "Registered";
                }
            }
            else{
                return "Invalid credentials";
            }
        }
        return null;
    }

    /**
     * Return the list of all registered players
     */
    @Override
    public List<Player> getAllRegisteredPlayers() {
        List<Player> playerList=new ArrayList<>();
        if(registeredPlayerList!=null){
            for(Player e:registeredPlayerList)
            {
                playerList.add(e);
            }
                return playerList;
        }
        return null;
    }


    /**
     * Return the existing number of players for the given title
     */
    @Override
    public int getExistingNumberOfPlayersInTeam(LeagueTeamTitles teamTitle) {
        int count=0;
        if(registeredPlayerList!=null){
            for(int i=0;i<registeredPlayerList.size();i++){
                if(registeredPlayerList.get(i).getTeamTitle().equals(teamTitle.getTitle())){
                    count=count+1;
                }
            }
        }
        return count;
    }

    /**
     * Admin credentials are authenticated and registered players are allotted to requested teams if available
     * If the requested teams are already formed,admin randomly allocates to other available teams
     * PlayerTeam object is added to "finalteam.csv" file allotted by the admin using PlayerTeamDao
     * Return "No player is registered" when registeredPlayerList is empty
     * Throw TeamAlreadyFormedException when maximum number is reached for all teams
     * Return "Players allotted to teams" when registered players are successfully allotted
     * Return "Invalid credentials for admin" when admin credentials are wrong
     */
    @Override
    public String allotPlayersToTeam(String adminName, String password, LeagueTeamTitles teamTitle)
            throws TeamAlreadyFormedException, PlayerNotFoundException {
        int countForTeam=0;
        if(!adminName.equals(AdminCredentials.admin)||!password.equals(AdminCredentials.password)) {
            return "Invalid credentials for admin";
        }
        if(registeredPlayerList.size()==0) {
            return "No player is registered";
        }
        List<Player> playerTeams=playerDao.getAllPlayers();
        for(int i=0;i<playerTeams.size();i++)
        {
            if(playerTeams.get(i).getTeamTitle()==null)
            {
                continue;
            }
            if(playerTeams.get(i).getTeamTitle().equals(teamTitle.getTitle()))
            {
                countForTeam++;
            }
        }
        if(countForTeam==11) {
            throw new TeamAlreadyFormedException();
        }
        int value=getExistingNumberOfPlayersInTeam(teamTitle);
        if (value < 12) {
            for (int j = 0; j < registeredPlayerList.size(); j++) {

                playerTeamDao.addPlayerToTeam(registeredPlayerList.get(j));
                return "Players allotted to teams";
            }
        } else {
            int pick = new Random().nextInt(LeagueTeamTitles.values().length);
            LeagueTeamTitles pickTitle = LeagueTeamTitles.values()[pick];
            allotPlayersToTeam("admin", "pass", pickTitle);
        }
        return null;
    }


    /**
     * static nested class to initialize admin credentials
     * admin name='admin' and password='pass'
     */
    static class AdminCredentials {
        private static String admin = "admin";
        private static String password = "pass";
    }
}

