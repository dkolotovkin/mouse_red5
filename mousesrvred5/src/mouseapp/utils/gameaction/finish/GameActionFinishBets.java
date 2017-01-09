package mouseapp.utils.gameaction.finish;

import mouseapp.utils.gameaction.GameAction;

public class GameActionFinishBets extends GameAction {
	public int returnmoney;
	public int winnermoney;
	public int prizemoney;
	public int useroutid;
	
	public GameActionFinishBets(byte type, int roomID, int returnmoney, int winnermoney, int prizemoney, int useroutid){
		super(type, roomID);
		this.returnmoney = returnmoney;
		this.winnermoney = winnermoney;
		this.prizemoney = prizemoney;
		this.useroutid = useroutid;
	}
}
