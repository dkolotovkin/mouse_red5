package mouseapp.utils.gameaction.start;

import mouseapp.utils.gameaction.GameAction;

public class GameActionWaitStartBetsGame extends GameAction {
	public int waittime;
	public byte error;
	
	public GameActionWaitStartBetsGame(byte type, int roomID, int waittime, byte error){
		super(type, roomID);
		this.waittime = waittime;	
		this.error = error;
	}
}
