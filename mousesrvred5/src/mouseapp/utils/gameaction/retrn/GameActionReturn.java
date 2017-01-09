package mouseapp.utils.gameaction.retrn;

import mouseapp.utils.gameaction.GameAction;

public class GameActionReturn extends GameAction {
	public int userid;
	public int money;
	
	public GameActionReturn(byte type, int roomID, int userid, int money){
		super(type, roomID);
		this.userid = userid;
		this.money = money;
	}
}
