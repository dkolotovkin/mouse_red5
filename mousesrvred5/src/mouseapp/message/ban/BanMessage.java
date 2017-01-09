package mouseapp.message.ban;

import mouseapp.message.Message;

public class BanMessage extends Message {	
	public int fromId;
	public int toId;
	public byte bantype;
	
	public BanMessage(byte type, int roomId, int fromId, int toId, byte bantype){
		super(type, roomId);
	
		this.fromId = fromId;
		this.toId = toId;
		this.bantype = bantype;
	}
}
