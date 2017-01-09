package mouseapp.utils.gameaction;

public class GameAction {
	public byte type;
	public int roomID;
	
	public GameAction(byte type, int roomID){
		this.type = type;
		this.roomID = roomID;
	}
}
