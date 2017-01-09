package mouseapp.utils.gameaction.start;

import java.util.List;

import mouseapp.utils.gameaction.GameAction;

public class GameActionStart extends GameAction {
	public int time;
	public String locationXML;
	public List<Integer> users;
	public byte gametype;
	
	public GameActionStart(byte type, int roomID, List<Integer> users, int t, String xml){
		super(type, roomID);
		this.time = t;
		this.locationXML = xml;
		this.users = users;		
	}
}
