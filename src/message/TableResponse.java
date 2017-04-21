package message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import gamelogic.*;

public class TableResponse implements Sendable, Serializable{
	
	public CopyOnWriteArrayList<Card> table1;
	public double randomnum;
	
	public TableResponse(CopyOnWriteArrayList <Card> t) {
		this.table1 = new CopyOnWriteArrayList<Card>(t);
	}
	
	public void send(ObjectOutputStream outputstream)
	  {
		this.randomnum = Math.random();
		for(Card card: this.table1) {
			System.out.println(card.getDescription());
		}
	    try {
	      outputstream.writeObject(this);
	    } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	  }
}
