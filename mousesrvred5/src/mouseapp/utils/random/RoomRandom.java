package mouseapp.utils.random;

import mouseapp.ServerMouseApplication;
import mouseapp.game.GameManager;
import mouseapp.room.Room;

public class RoomRandom {
	public static int maxRoomID = 100000;
	
	public static int getRoomID(){
		int rID = 0;
		Room r;
		Boolean good = false;
		do{
			rID = Math.round((float)Math.random() * maxRoomID);			
			r = ServerMouseApplication.application.rooms.get(Integer.toString(rID));
			if (r != null) good = false;
			else good = true;
			r = GameManager.gamerooms.get(Integer.toString(rID));
			if (r != null) good = false;
			else good = true;			
		}while(!good);
		
		return rID;
	}
	
	public static int getRandomFromTo(int from, int to){
		int random = 0;
		do{
			random = Math.round((float)Math.random() * to);			
						
		}while(random < from);
		
		return random;
	}
}
