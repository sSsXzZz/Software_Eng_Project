package message;

import java.io.IOException;
import java.io.ObjectOutputStream;

/*
 * SetSelectMessage
 * Contains the cards and username
 * That a certain client picked.
 */

public class SetSelectMessage implements Sendable {

  public int cards[];
  public String username;
  
  public SetSelectMessage(String username, int cards[])
  {
    this.username = username;
    this.cards = cards;
  }
  
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
