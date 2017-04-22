package server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

import gamelogic.*;
import message.*;

/**
 * A thread for a single game
 * Handles communication with the client for things pertaining to the game
 * @author Yash
 */

public class GameThread implements Runnable {
	
    public CopyOnWriteArrayList<PlayerCom> connected_players = null;
    public PlayerCom host = null;
    long gid;
    
    /**
     * Initializes the GameThread. Keeps track of the host and gid
     * @param	p	host
     * @param	i	hostinputstream
     * @param	o	hostoutputstream
     * @param	id	gid
     * @author Yash
     */
    public GameThread(long id, PlayerCom playercom) throws IOException {
    	gid = id;
        connected_players = new CopyOnWriteArrayList<PlayerCom>();
        addNewPlayer(playercom);
        host = playercom;
    }

	public void run() {
		//Handles Room Actions
		while(true) {
			Object obj;
			try {				
				//In Room, check with all the players and see what they want to do
				for (PlayerCom playercom: this.connected_players) {
					
					if (playercom.gameToPlayerPipe.peek() == null){
						playercom.gameToPlayerPipe.put("readObject");
					}
					obj = playercom.playerToGamePipe.poll();
					
					if (playercom == host && obj instanceof StartGameMessage) {
						gameStart();
						return;
					}
					if (obj instanceof LeaveRoomMessage) {
						System.out.println("Received LeaveRoomMessage");
						
						//Tell all players client leaving
						LeaveRoomResponse lrr = new LeaveRoomResponse(playercom.player);
						for(PlayerCom playercom1: this.connected_players) {
		    				lrr.send(playercom1.output);
		    			}
						
						System.out.println("Sent LeaveRoomResponse");
						
						//System.out.println(this.connected_players.size());
						//If only 1 player in room, if player leaves, close game
						if (this.connected_players.size() == 1) {
							this.connected_players.remove(playercom);
							this.terminate();
							playercom.gameToPlayerPipe.put("leave");
							return;
						}
						playercom.gameToPlayerPipe.put("leave");
						this.connected_players.remove(playercom);
						
						//If host leaves room, find another player and set them to be the host
						if (playercom == host) {
							for (PlayerCom playercom1: this.connected_players) {
								host = playercom1;
								break;
							}
							
							
							//Send ChangedHostResponse, telling all clients who the new host is
							ChangedHostResponse chr = new ChangedHostResponse(host.player);
							for(PlayerCom playercom1: this.connected_players) {
								chr.send(playercom1.output);
			    			}
						}
					}
					// receive PlayerCom when a player has disconnected
					// similar to leavegameMessage
					else if (obj instanceof PlayerCom){
						PlayerCom surrendered_player = (PlayerCom) obj;
						
						//Tell all players client leaving
						LeaveRoomResponse lrr = new LeaveRoomResponse(surrendered_player.player);
						for(PlayerCom playercom1: this.connected_players) {
							if (!surrendered_player.player.username.equals(playercom1.player.username))
								lrr.send(playercom1.output);
		    			}
						
						//remove player since the socket is already closed
						connected_players.remove(surrendered_player);
						//If only 1 player in room, if player leaves, close game
						if (this.connected_players.size() == 1) {
							this.connected_players.remove(playercom);
							this.terminate();
							return;
						}
						this.connected_players.remove(playercom);
						
						//If host leaves room, find another player and set them to be the host
						if (surrendered_player == host) {
							for (PlayerCom playercom1: this.connected_players) {
								host = playercom1;
								break;
							}
							
							
							//Send ChangedHostResponse, telling all clients who the new host is
							ChangedHostResponse chr = new ChangedHostResponse(host.player);
							for(PlayerCom playercom1: this.connected_players) {
								chr.send(playercom1.output);
			    			}
						}
					}
				}
				//System.out.println("Out of for loop");
			}
			  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	// TODO Auto-generated method stub
	}
	
	private void gameStart() throws InterruptedException, IOException{
		Object obj;
		System.out.println("Received Start Game Message");
		Server.connected_rooms.remove(gid);
		StartGameResponse sgr = new StartGameResponse(gid);
		//Sent StartGameResponse to all players
		//POSSIBLE DEBUG: Unnecessarily sending response to players already in game might overflow buffer
		for(ObjectOutputStream value : Server.connected_playerOutput.values()) {
			sgr.send(value);
		}
		System.out.println("Sent Start Game Response");	
		Server.connected_games.put(gid, this);
		//Game Logic
		Game game = new Game();
		ArrayList <Card> deck = game.createDeck();
		ArrayList <Card> table = new ArrayList <Card>();
		game.initTable(deck, table, 12);
		System.out.println("Setup Table");
		//Game only starts when server receives initialcardsmessages from each of the players in the game
		//On InitialCardsMessage, send InitialCardsResponse, simply initial state of the table
		int check = 0;
		System.out.println(this.connected_players.size());
		while(!(check == this.connected_players.size())) {
			for (PlayerCom playercom: this.connected_players) {
				if (playercom.gameToPlayerPipe.peek() == null){
					playercom.gameToPlayerPipe.put("readObject");
				}
				obj = playercom.playerToGamePipe.poll();
				
				if (obj instanceof InitialCardsMessage) {
					System.out.println("Received Initial Cards Message");
					check++;
					InitialCardsResponse icr = new InitialCardsResponse(table);
					icr.send(playercom.output);
					System.out.println("Sent Initial Cards Response");
				}
			}
		}
		//Handles Game Actions
		System.out.println("Starting Game");
		//Game only ends when deck is empty and no set exists on the table
		while(!deck.isEmpty() || (game.checkSetexists(table).size() > 0)) {
			// end game if no players left
			if (connected_players.size() <= 0){
				System.out.println("Terminating");
				this.terminate();
				return;
			}	
			//No set on table, if there's no set on table, must be at least 3 cards in deck
			if (game.checkSetexists(table).size() == 0) {
				System.out.println("No Set on Table");
				game.dealCards(deck, table, 3);

				//Send Updated table to all players currently in game
				TableResponse tr1 = new TableResponse(table);
				for(PlayerCom playercom: this.connected_players) {
					playercom.output.reset();
					tr1.send(playercom.output);
    			}
				//Check again if the game needs to continue, and if so, if 3 more cards need to be dealt to the table
				//If the game is live, there should always be a set on the board
				System.out.println("continue");
				continue;
			}
			ArrayList <Card> temp = new ArrayList <Card>();	
			//checkSetexists returns set, returns 0 if no set, hence can be used as a check as well
			//optimizes testing out game, finding a set is hard
			temp = game.checkSetexists(table);
			String answer = "";
			for (Card card: temp) {
				answer += card.getDescription() + "|";
			}
			System.out.print(answer + "\r");
			//Check for messages from each player
			for (PlayerCom playercom: this.connected_players) {
				Player player = playercom.player;
				if (playercom.gameToPlayerPipe.peek() == null){
					playercom.gameToPlayerPipe.put("readObject");
				}
				obj = playercom.playerToGamePipe.poll();
				
				if (obj instanceof SetSelectMessage) {
					System.out.println("Received a set");
					SetSelectMessage resp = (SetSelectMessage) obj;
					//If set's valid, update set count, send to all players in SetSelectResponse
					if(game.validateSet(resp.cards)) {
						System.out.println("Set's valid!");
						game.updateSetcount(player);
						
						SetSelectResponse ssr = new SetSelectResponse(player, true);
						for(PlayerCom playercom1: this.connected_players) {
							ssr.send(playercom1.output);
		    			}

						//If set's valid, remove cards from table
						//In order to make board configuration intuitive, we had to figure out how to make it so that if the set gets
						//replaced it's done realistically, like the 3 cards are literally replaced on the board, the cards aren't shifted
						//around unrealistically. 
						
						//To put things short, we put a hole attribute in card, and changed removecard to set hole attribute to true
						game.removeCards(resp.cards, table);

						for(Card card: table) {
							System.out.println(card.getDescription());
						}
						//If less than 12 cards on table and there are cards in the deck, REPLACE the holes. Function takes next 3 cards
						//in deck and places it in place of the holes
						System.out.println("Table Size");
						//Overloaded size function needed as holes need to be manually not accounted for when calculating number of cards
						//on table
						System.out.println(game.getsize(table));
						if (game.getsize(table) < 12 && !deck.isEmpty()) {
							game.replaceCards(resp.cards, deck, table);
						}
						//System.out.println(game.getsize(table));

						//Send updated table to all players in game
						TableResponse tr2 = new TableResponse(table);
						for(PlayerCom playercom1: this.connected_players) {
							playercom1.output.reset();
							tr2.send(playercom1.output);
		    			}
		    			
					}
					else {
						//If set invalid, only tell client
						System.out.println("Set invalid");
						SetSelectResponse ssr = new SetSelectResponse(player, false);
						/*for(Map.Entry<Player, ObjectOutputStream> entry1: this.connected_playerOutput.entrySet()) {
							ssr.send(entry1.getValue());
		    			}*/
						ssr.send(playercom.output);
					}
				}
				//Users also have option to leave game midway, surrender
				else if (obj instanceof LeaveGameMessage) {
					//Set setcount to -1, punishment for raging
					player.setcount = -1;
					
					//If only 1 player in game, if player leaves, close game
					if (this.connected_players.size() == 1) {
						this.connected_players.remove(playercom);
						this.terminate();
						playercom.gameToPlayerPipe.put("leave");
						return;
					}
					this.connected_players.remove(playercom);
					playercom.gameToPlayerPipe.put("leave");
					
					//Tell all players client has left game
					LeaveGameResponse lgr = new LeaveGameResponse(player);
					for(PlayerCom playercom1: this.connected_players) {
						lgr.send(playercom1.output);
	    			}
				}
				// receive PlayerCom when a player has disconnected
				// similar to leavegameMessage
				else if (obj instanceof PlayerCom){
					PlayerCom surrendered_player = (PlayerCom) obj;
					
					//remove player since the socket is already closed
					connected_players.remove(surrendered_player);
					
					//If only 1 player in game, if player leaves, close game
					if (this.connected_players.size() == 1) {
						this.connected_players.remove(playercom);
						this.terminate();
						return;
					}
					this.connected_players.remove(playercom);

					//Set setcount to -1, punishment for raging
					surrendered_player.player.setcount = -1;
					
					//Tell all players client has left game
					LeaveGameResponse lgr = new LeaveGameResponse(surrendered_player.player);
					for(PlayerCom playercom1: this.connected_players) {
						lgr.send(playercom1.output);
	    			}
				}
			}
		}
		
		//Game's over
		System.out.println("Game's over");
		// make sure every player is in readObject state
		for (PlayerCom playercom: this.connected_players) {
			if (playercom.gameToPlayerPipe.peek() == null){
				playercom.gameToPlayerPipe.put("readObject");
			}
		}
		System.out.println("Everyone's in readObject state");

		//Send final table response
		TableResponse tr2 = new TableResponse(table);
		for(PlayerCom playercom: this.connected_players) {
			playercom.output.reset();
			tr2.send(playercom.output);
		}
		
		//Send EndGameResponse to all players in game
		EndGameResponse eg = new EndGameResponse();
		for(PlayerCom playercom: this.connected_players) {
			System.out.println("Sent EndGameResponse");
			eg.send(playercom.output);
		}
		// make sure we hear a response from ALL players before proceeding
		HashSet<PlayerCom> responded_players = new HashSet<PlayerCom>();
		while( responded_players.size() < connected_players.size()){
			for(PlayerCom playercom: this.connected_players){
				if (playercom.playerToGamePipe.peek() != null){
					responded_players.add(playercom);
				}
			}
		}
		System.out.println("Heard Response from all players");
		
		//Terminate game - early so that players show up in lobby
		this.terminate();
		
		//tell all players to leave
		for (PlayerCom playercom: this.connected_players) {
			playercom.gameToPlayerPipe.put("leave");
			playercom.player.setcount=0; // reset setcount so it's 0 for any new game
		}
		System.out.println("All players told to leave");
		System.out.println("Terminated");
		//Push Stats to DB
		return;

	}
	
	private void terminate() throws IOException {
		Server.connected_games.remove(gid);
        Server.connected_rooms.remove(gid);
    }
	
	public void addNewPlayer(PlayerCom playercom){
        connected_players.add(playercom);
	}
	
}
