package mouseapp.message.status;

import mouseapp.message.Message;
import mouseapp.room.Room;

public class MessageOutStatus extends Message{
	public int initiatorId;	
	
	public MessageOutStatus(byte type, Room room, int initiatorId){
		super(type, room.id);
		this.initiatorId = initiatorId;
	}
}
