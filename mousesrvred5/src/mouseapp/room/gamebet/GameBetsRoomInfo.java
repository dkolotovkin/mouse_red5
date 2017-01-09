package mouseapp.room.gamebet;

import java.util.List;

public class GameBetsRoomInfo {
	public int id;
	public int creatorid;
	public boolean isseats;
	public List<Integer> users;
	
	public GameBetsRoomInfo(int id, int creatorid, boolean isseats, List<Integer> users){
		this.id = id;
		this.creatorid = creatorid;
		this.isseats = isseats;
		this.users = users;
	}
}
