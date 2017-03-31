package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gamelogic.*;
import message.*;

/**
 * A thread for a single game
 * Handles communication with the client for things pertaining to the game
 * @author Yash
 *
 */

public class GameThread implements Runnable {
	
    public Map<Player, ObjectInputStream> connected_playerInput = null;
    public Map<Player, ObjectOutputStream> connected_playerOutput = null;
    private Player hostp = null;
    private Socket hosts = null;
    private ObjectInputStream hostInput = null;
    private ObjectOutputStream hostOutput = null;
    int gid;
    
    private volatile boolean shutdown = false;
    
    public GameThread(Player p, Socket s, int id) throws IOException {
    	hostp = p;
    	hosts = s;
    	gid = id;
    	
        hostInput = new ObjectInputStream(hosts.getInputStream());
        hostOutput = new ObjectOutputStream(hosts.getOutputStream());
        connected_playerInput = Collections.synchronizedMap(new HashMap<Player,ObjectInputStream>());
		connected_playerOutput = Collections.synchronizedMap(new HashMap<Player,ObjectOutputStream>());
		connected_playerInput.put(p, hostInput);
		connected_playerOutput.put(p, hostOutput);
    }

	public void run() {
		while(!shutdown) {
			Object obj;
			try {
				obj = (Object) hostInput.readObject();
				if (obj instanceof StartGameMessage) {
					StartGameResponse sgr = new StartGameResponse(gid);
					for(ObjectOutputStream value : Server.connected_playerOutput.values()) {
	    				sgr.send(value);
	    			}
					
					Game game = new Game();
					ArrayList <Card> deck = game.createDeck();
					ArrayList <Card> table = new ArrayList <Card>();
					game.dealCards(deck, table, 12);
					//Send cards when Sahil asks
					int check = 0;
					while(!(check == this.connected_playerInput.size()-1)) {
						for (Map.Entry<Player, ObjectInputStream> entry: this.connected_playerInput.entrySet()) {
							obj = (Object) entry.getValue().readObject();
							if (obj instanceof InitialCardsMessage) {
								check++;
								InitialCardsResponse icr = new InitialCardsResponse(table);
								icr.send(this.connected_playerOutput.get(entry.getKey()));
							}
						}
					}
					TableResponse tr = new TableResponse(table);
					for(ObjectOutputStream value : this.connected_playerOutput.values()) {
	    				tr.send(value);
	    			}
					while (!shutdown) {
						while(!deck.isEmpty() || game.checkSetexists(table)) {
							if (!game.checkSetexists(table)) {
								game.dealCards(deck, table, 3);
								
								TableResponse tr1 = new TableResponse(table);
								for(Map.Entry<Player, ObjectOutputStream> entry: this.connected_playerOutput.entrySet()) {
									if (entry.getKey().setcount != -1) {
										tr1.send(entry.getValue());
									}
				    			}
							}
							for (Map.Entry<Player, ObjectInputStream> entry: this.connected_playerInput.entrySet()) {
								obj = (Object) entry.getValue().readObject();
								if (obj instanceof SetSelectMessage) {
									SetSelectMessage resp = (SetSelectMessage) obj;
									if(game.validateSet(resp.cards)) {
										game.updateSetcount(entry.getKey());
										
										SetSelectResponse ssr = new SetSelectResponse(entry.getKey(), true);
										for(Map.Entry<Player, ObjectOutputStream> entry1: this.connected_playerOutput.entrySet()) {
											if (entry1.getKey().setcount != -1) {
												ssr.send(entry1.getValue());
											}
						    			}
										
										game.removeCards(resp.cards, table);
										if (table.size() < 12 && !deck.isEmpty()) {
											game.dealCards(deck, table, 3);
											
											TableResponse tr2 = new TableResponse(table);
											for(Map.Entry<Player, ObjectOutputStream> entry1: this.connected_playerOutput.entrySet()) {
												if (entry1.getKey().setcount != -1) {
													tr2.send(entry1.getValue());
												}
							    			}
										}
									}
									else {
										SetSelectResponse ssr = new SetSelectResponse(entry.getKey(), false);
										for(Map.Entry<Player, ObjectOutputStream> entry1: this.connected_playerOutput.entrySet()) {
											if (entry1.getKey().setcount != -1) {
												ssr.send(entry1.getValue());
											}
						    			}
									}
								}
								
								if (obj instanceof LeaveGameMessage) {
									entry.getKey().setcount = -1;
									
									LeaveGameResponse lgr = new LeaveGameResponse(entry.getKey());
									for(Map.Entry<Player, ObjectOutputStream> entry1: this.connected_playerOutput.entrySet()) {
										if (entry1.getKey().setcount != -1) {
											lgr.send(entry1.getValue());
										}
					    			}
								}
							}
						}
						
						EndGameResponse eg = new EndGameResponse();
						for(Map.Entry<Player, ObjectOutputStream> entry: this.connected_playerOutput.entrySet()) {
							if (entry.getKey().setcount != -1) {
								eg.send(entry.getValue());
							}
		    			}
						Server.connected_games.remove(gid);
						//Push Stats to DB
						
						//Interrupt thread?
					}
				}
				for (Map.Entry<Player, ObjectInputStream> entry: this.connected_playerInput.entrySet()) {
					obj = (Object) entry.getValue().readObject();
					if (obj instanceof LeaveRoomMessage) {
						this.connected_playerInput.remove(entry.getKey());
						this.connected_playerOutput.remove(entry.getKey());
						
						LeaveRoomResponse lrr = new LeaveRoomResponse(entry.getKey());
						for(ObjectOutputStream value : this.connected_playerOutput.values()) {
		    				lrr.send(value);
		    			}
					}
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO Auto-generated method stub
	}

}
