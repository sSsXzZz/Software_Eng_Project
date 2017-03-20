package server;
import java.io.*;
import java.net.*;
import gamelogic.*;
import ui.LoginMessage;
import ui.LoginResponse;
import java.sql.*;

/**
 * A thread for a single player/client.
 * Handles communication with the client for things such as
 * authentication, stat retrieval, etc
 * @author Shalin
 *
 */

public class PlayerThread implements Runnable {
    private Socket socket = null;
    public Player player = null;
    private ObjectInputStream clientInput = null;
    private ObjectOutputStream clientOutput = null;
    
    /**
     * Initializes the PlayerThread. Keeps track of the given socket and
     * creates the Object Stream's to communicate with the Client
     * @param	socket	The Socket object for the client
     * @author Shalin
     */
    public PlayerThread(Socket socket) throws IOException{
        this.socket = socket;
        clientInput = new ObjectInputStream(socket.getInputStream());
        clientOutput = new ObjectOutputStream(socket.getOutputStream());
    }

    /**
     * Obtained from the Runnable interface. Is called from Thread.start().
	 * It is essentially the main method for the thread, handling
	 * authentication and subsequently requests to check stats, join
	 * a game, logout, etc.
     * @author Shalin
     */
    public void run() {
    	
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
        	// try authenticating 3 times
        	for(int i=0; i<3; i++){
        		if (this.authenticate()){
        			LoginResponse lr = new LoginResponse(true);
        			lr.send(clientOutput);
        			// TODO add to connected_players
        			break;
        		}
        	}
            this.terminate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Authenticates the user by checking if the credentials are valid according to the database
     * @return Boolean value. true if the credentials are valid, false otherwise
     * @author Shalin
     */
    private Boolean authenticate() throws IOException{
    	Boolean authenticate_status = false;
    	String username, password;
    	LoginMessage inputObject;
    	
    	try {
	        inputObject = (LoginMessage)clientInput.readObject();
	        username = inputObject.username;
	        password = inputObject.password;
	    	System.out.println("Recieved from client:" + username + password);
    		DatabaseConnection conn = Database.getConnection();
    		authenticate_status = conn.authenticateUser(username, password);
    		conn.close();
    	} catch (ClassNotFoundException e) {
        	e.printStackTrace();
        } catch (SQLException e){
        	e.printStackTrace();
        }
    	return authenticate_status;
    }
    
    /**
     * Handles the cleanup when the thread closes.
     * This includes closing the socket and removing the player from the connect_players set
     * maintained by the main Server class.
     * @author Shalin
     */
    public void terminate() throws IOException{
		socket.close();
    }
}