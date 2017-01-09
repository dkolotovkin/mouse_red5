package mouseapp.room.gamebet;

import java.util.List;

public class GameBetRoomInfo {
	public int id;
	public int bet;
	public int time;
	public boolean rlocked;
	public boolean isseats;
	public List<Integer> users;
	
	public GameBetRoomInfo(int id, int bet, int time, boolean rlocked, boolean isseats, List<Integer> users){
		this.id = id;
		this.bet = bet;
		this.time = time;
		this.isseats = isseats;
		this.rlocked = rlocked;
		this.users = users;
	}
}
