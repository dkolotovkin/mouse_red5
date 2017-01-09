package mouseapp.message.newlevel;

import mouseapp.message.Message;

public class MessageNewLevel extends Message {
	public int level;
	public int prize;
	
	public MessageNewLevel(byte type, int roomID, int l, int p){
		super(type, roomID);
		this.level = l;
		this.prize = p;
	}
}
