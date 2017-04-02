package message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/*
 * LoginMessage
 * A Client sends a login message to the server
 * that contains the username and password that the user
 * Is trying to login with
 */

public class CreateRoomMessage implements Sendable, Serializable {
  
  public String username;
  public String roomname;
  
  public CreateRoomMessage(String username, String roomname)
  {
    this.username = username;
    this.roomname = roomname;
  }
  
  @Override
  public void send(ObjectOutputStream outputstream)
  {
    try {
      outputstream.writeObject(this);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
}