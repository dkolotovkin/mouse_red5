package mouseapp.utils.gameaction.start;

import mouseapp.room.gamebet.BetResult;
import mouseapp.room.gamebet.BetsInfo;
import mouseapp.utils.gameaction.GameAction;

public class GameActionBetsContent extends GameAction {
	public int waittime;
	public Boolean added = false;
	public BetsInfo betsinfo;
	public byte result = BetResult.OTHER;
	
	public GameActionBetsContent(byte type, int roomID, int waittime){
		super(type, roomID);
		this.waittime = waittime;		
	}
}
