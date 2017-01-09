package mouseapp.message.status;

import java.util.List;

import mouseapp.message.Message;
import mouseapp.room.Room;
import mouseapp.user.GameInfo;

public class MessageStatusGame extends Message{
	public GameInfo initiator;
	public String roomTitle;
	public List<Message> messages;
	public List<GameInfo> users;
	
	public MessageStatusGame(byte type, Room room, GameInfo initiator){
		super(type, room.id);
		this.roomTitle = room.title;
		this.initiator = initiator;
	}
}
