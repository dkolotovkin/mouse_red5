package mouseapp.game;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.room.Room;
import mouseapp.room.game.GameRoom;
import mouseapp.room.gamebet.BetResult;
import mouseapp.room.gamebet.BetsInfo;
import mouseapp.room.gamebet.GameBetRoom;
import mouseapp.room.gamebet.GameBetRoomInfo;
import mouseapp.room.gamebet.GameBetsRoomInfo;
import mouseapp.room.gamebets.GameBetsRoom;
import mouseapp.user.UserConnection;
import mouseapp.utils.changeinfo.ChangeInfoParams;
import mouseapp.utils.gameaction.GameAction;
import mouseapp.utils.gameaction.GameActionType;
import mouseapp.utils.gameaction.start.GameActionBetsContent;
import mouseapp.utils.gameaction.start.GameActionWaitStart;
import mouseapp.utils.random.RoomRandom;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IServiceCapableConnection;

public class GameManager extends Object {
	public static Map<String, String> maps;
	public static Map<String, Integer> mapstimes;
	public static Map<String, GameRoom> gamerooms;
	public static GameRoom simplegame;
	public static GameRoom simplegamenews;				//1-4 уровни
	public static int jackpot = 0;
	
	public static File[] locations;
	public static File[] locationsbet;
	
	public String pathtoxml = "mouselocations/";
	public String pathtobetxml = "mousebetlocations/";	
	
	public GameManager(){
		gamerooms = new ConcurrentHashMap<String, GameRoom>();
		maps = new HashMap<String, String>();
		mapstimes = new HashMap<String, Integer>();		
		
		File directory = null;
		
		directory = new File(pathtoxml);
		if (directory.isDirectory()){			
			locations = directory.listFiles();			
		}
		directory = new File(pathtobetxml);
		if (directory.isDirectory()){			
			locationsbet = directory.listFiles();			
		}
		directory = null;
	}
	
	/*******************************************ИГРА НА СТАВКИ*****************************************/
	//создание игры на ставки
	public GameActionBetsContent createBetsGame(){
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		GameActionBetsContent action = new GameActionBetsContent(GameActionType.BETS_CONTENT, 0, Config.waitTimeToStartBet());
		
		if(user != null && user.user.popular >= 50 && user.user.experience >= 200){
			GameBetsRoom room = new GameBetsRoom(RoomRandom.getRoomID(), "Игра на ставки", user.user.id);
			action.roomID = room.id;
			gamerooms.put(Integer.toString(room.id), room);
			room.addUser(user);
			action.added = room.addwaituser(user);
			action.betsinfo = getBetsInfo(action.roomID);	
		}else{
			action.type = GameActionType.BAD_PARAMS;
		}		
		client = null;
		user = null;
		return action;
	}
	//войти в комнату игры на ставки (до начала забега)
	public GameActionBetsContent addToBetsGame(int roomID){
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		GameActionBetsContent action = new GameActionBetsContent(GameActionType.BETS_CONTENT, 0, Config.waitTimeToStartBet());
		
		GameRoom gbr = gamerooms.get(Integer.toString(roomID));		
		if (gbr != null && (gbr instanceof GameBetsRoom) && ((GameBetsRoom) gbr).started == false){
			action.roomID = gbr.id;
			gbr.addUser(user);
			action.added = true;
			action.betsinfo = getBetsInfo(roomID);					
		}else{
			GameAction a = new GameAction(GameActionType.NO_ROOM, 0);
			if (user.conection instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) user.conection).invoke("processGameAction", new Object[]{a});
	    	}
			a = null;
			action.added = false;
		}
		
		client = null;
		user = null;
		return action;
	}
	
	//получить список доступных игр на ставки
	public List<GameBetsRoomInfo> getBetsGamesInfo(){
		List<GameBetsRoomInfo> rooms = new ArrayList<GameBetsRoomInfo>();
		Set<Entry<String, GameRoom>> setroom = gamerooms.entrySet();
		for (Map.Entry<String, GameRoom> room:setroom){
			GameRoom gr = room.getValue();
			if (gr != null && (gr instanceof GameBetsRoom) && ((GameBetsRoom) gr).started == false){
				
				List<Integer> gameusers = new ArrayList<Integer>();
				Set<Entry<String, UserConnection>> set = gr.waitusers.entrySet();
				for (Map.Entry<String, UserConnection> user:set){
					gameusers.add(user.getValue().user.id);
				}	
				set = null;				
				
				rooms.add(new GameBetsRoomInfo(((GameBetsRoom)gr).id, ((GameBetsRoom)gr).creatorid, ((GameBetsRoom)gr).waitusers.size() < Config.maxUsersInGame(), gameusers));
				gameusers = null;
			}
			gr = null;
		}
		setroom = null;
		
		return rooms;
	}
	//получить информацию (участники и ставки) о конкретной игре на ставки
	public BetsInfo getBetsInfo(int roomID){
		BetsInfo binfo = new BetsInfo();
		GameRoom gbr = gamerooms.get(Integer.toString(roomID));		
		if (gbr != null && (gbr instanceof GameBetsRoom) && ((GameBetsRoom) gbr).started == false){	
			binfo.creatorid = ((GameBetsRoom) gbr).creatorid;
						
			Set<Entry<String, UserConnection>> set = gbr.waitusers.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				binfo.users.add(user.getValue().user.id);
			}	
			set = null;
			
			binfo.bets = ((GameBetsRoom) gbr).bets;
		}
		return binfo;
	}
	//сделать ставку
	public byte bet(int roomID, int betuser, int bet){
		GameActionBetsContent action = new GameActionBetsContent(GameActionType.BETS_CONTENT, 0, Config.waitTimeToStartBet());
		
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		
		GameRoom gbr = gamerooms.get(Integer.toString(roomID));		
		if (gbr != null && (gbr instanceof GameBetsRoom) && ((GameBetsRoom) gbr).started == false){
			if(user != null){
				if(user.user.money >= bet){					
					action.result = (byte)((GameBetsRoom) gbr).bet(user, betuser, bet);
					action.betsinfo = getBetsInfo(gbr.id);
					
					IConnection conn = null;
					Set<Entry<String, UserConnection>> set = gbr.users.entrySet();			
					for (Map.Entry<String, UserConnection> gu:set){						
						conn = gu.getValue().conection;
						if (conn instanceof IServiceCapableConnection) { 
							 ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});
				    	}						
					}					
				}else{
					action.result = BetResult.NO_MONEY;
				}
			}else{
				action.result = BetResult.OTHER;
			}
		}else{
			action.result = BetResult.NO_ROOM;
		}
		
		gbr = null;
		client = null;
		user = null;
		return action.result;
	}
	//послать запрос на участие в забег на ставки
	public void sendRequestBetsGame(int roomID){	
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		
		GameRoom gbr = gamerooms.get(Integer.toString(roomID));		
		if (gbr != null && (gbr instanceof GameBetsRoom) && ((GameBetsRoom) gbr).started == false){
			((GameBetsRoom) gbr).sendRequest(user.user.id);
		}	
		
		gbr = null;
		client = null;
		user = null;
	}
	//добавить игрока в участники забега на ставки(только организатор забега)
	public void addUserToBetsGame(int roomID, int userID){
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		
		GameRoom gbr = gamerooms.get(Integer.toString(roomID));		
		if (gbr != null && (gbr instanceof GameBetsRoom) && ((GameBetsRoom) gbr).started == false){
			UserConnection adduser = gbr.users.get(Integer.toString(userID));
			if(adduser != null && user.user.id == ((GameBetsRoom) gbr).creatorid){
				((GameBetsRoom) gbr).addwaituser(adduser);
			}
			
			GameActionBetsContent action = new GameActionBetsContent(GameActionType.BETS_CONTENT, 0, Config.waitTimeToStartBet());
			action.betsinfo = getBetsInfo(gbr.id);
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = gbr.users.entrySet();
			for (Map.Entry<String, UserConnection> gu:set){				
				conn = gu.getValue().conection;
				if (conn instanceof IServiceCapableConnection) {					
					((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});
		    	}
			}
			action = null;
		}
		
		gbr = null;
		client = null;
		user = null;
	}
	//запустить счетчик до начала забега на ставки
	public boolean startBetsGame(int roomID){
		boolean result = false;
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection u = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		
		GameRoom gbr = gamerooms.get(Integer.toString(roomID));		
		if (gbr != null && (gbr instanceof GameBetsRoom) && ((GameBetsRoom) gbr).started == false){
			if(u.user.id == ((GameBetsRoom) gbr).creatorid){
				if(((GameBetsRoom) gbr).waitusers.size() >= Config.minUsersInGame() && 
					((GameBetsRoom) gbr).waitusers.size() <= Config.maxUsersInGame()){
					result = true;
					((GameBetsRoom) gbr).started = true;
					((GameBetsRoom) gbr).setTimerToStart();
					
					GameActionWaitStart action = new GameActionWaitStart(GameActionType.WAIT_START, 0, Config.waitTimeToStartBets());
					IConnection conn = null;
					Set<Entry<String, UserConnection>> set = ((GameBetsRoom) gbr).users.entrySet();
					for (Map.Entry<String, UserConnection> user:set){
						conn = user.getValue().conection;
						if (conn instanceof IServiceCapableConnection) {							
				            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});
				    	}
					}		
					set = null;
				}
			}
		}
		
		gbr = null;
		client = null;
		u = null;
		return result;
	}
	//выйти из игры на ставки
	public boolean exitBetsGame(int roomID){
		boolean result = false;
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection u = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		
		GameRoom gbr = gamerooms.get(Integer.toString(roomID));		
		if (gbr != null && (gbr instanceof GameBetsRoom) && ((GameBetsRoom) gbr).started == false){
			((GameBetsRoom) gbr).removeUserByConnectionID(u.conection.getClient().getId());
		}
		
		gbr = null;
		client = null;
		u = null;
		return result;
	}	
	
	/**********************************ИГРА НА ДЕНЬГИ***********************************************/
	//создать игру на деньги
	public GameAction createBetGame(int bet, String pass){
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		GameActionWaitStart action = new GameActionWaitStart(GameActionType.WAIT_START, 0, Config.waitTimeToStartBet());
		
		if(user.user.money >= bet){
			GameBetRoom room = new GameBetRoom(RoomRandom.getRoomID(), "Игра", bet, pass);				
			gamerooms.put(Integer.toString(room.id), room);
			action.added = room.addwaituser(user);
		}else{
			GameAction a = new GameAction(GameActionType.NOT_ENOUGH_MONEY, 0);
			if (user.conection instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) user.conection).invoke("processGameAction", new Object[]{a});
	    	}
			a = null;
			action.added = false;
		}
		
		client = null;
		user = null;
		return action;
	}
	//войти в игру на деньги
	public GameAction addToBetGame(int roomID, String pass){
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		GameActionWaitStart action = new GameActionWaitStart(GameActionType.WAIT_START, 0, Config.waitTimeToStartBet());
		
		if(user != null){
			GameRoom gbr = gamerooms.get(Integer.toString(roomID));
			
			if (gbr != null && (gbr instanceof GameBetRoom)){
				if(user.user.money >= ((GameBetRoom) gbr).bet){
					if(!((GameBetRoom) gbr).passward.equals(pass)){
						GameAction a = new GameAction(GameActionType.BAD_PASSWARD, gbr.id);
						if (user.conection instanceof IServiceCapableConnection) {
				            ((IServiceCapableConnection) user.conection).invoke("processGameAction", new Object[]{a});
				    	}
						a = null;
						action.added = false;
					}else{
						action.waittime = ((GameBetRoom) gbr).durationtime - ((GameBetRoom) gbr).passedtime;
						action.added = gbr.addwaituser(ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId()));
						if (!action.added){
							GameAction a = new GameAction(GameActionType.NO_SEATS, 0);
							if (user.conection instanceof IServiceCapableConnection) {
					            ((IServiceCapableConnection) user.conection).invoke("processGameAction", new Object[]{a});
					    	}
							a = null;
							action.added = false;
						}
					}					
				}else{
					GameAction a = new GameAction(GameActionType.NOT_ENOUGH_MONEY, 0);
					if (user.conection instanceof IServiceCapableConnection) {
			            ((IServiceCapableConnection) user.conection).invoke("processGameAction", new Object[]{a});
			    	}
					a = null;
					action.added = false;
				}
			}else{
				GameAction a = new GameAction(GameActionType.NO_ROOM, 0);
				if (user != null && user.conection instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) user.conection).invoke("processGameAction", new Object[]{a});
		    	}
				a = null;
				action.added = false;
			}		
		}
		client = null;
		user = null;
		return action;
	}
	//получить информацию о доступных играх на деньги
	public List<GameBetRoomInfo> getBetGamesInfo(){
		List<GameBetRoomInfo> rooms = new ArrayList<GameBetRoomInfo>();
		Set<Entry<String, GameRoom>> setroom = gamerooms.entrySet();
		for (Map.Entry<String, GameRoom> room:setroom){
			GameRoom gr = room.getValue();
			if(gr instanceof GameBetRoom){
				
				List<Integer> gameusers = new ArrayList<Integer>();
				Set<Entry<String, UserConnection>> set = gr.waitusers.entrySet();
				for (Map.Entry<String, UserConnection> user:set){
					gameusers.add(user.getValue().user.id);
				}	
				set = null;
				
				boolean rlocked = false;
				if(((GameBetRoom) gr).passward != null && ((GameBetRoom) gr).passward.length() > 0){
					rlocked = true;
				}
				
				rooms.add(new GameBetRoomInfo(((GameBetRoom)gr).id, ((GameBetRoom)gr).bet, ((GameBetRoom)gr).durationtime - ((GameBetRoom)gr).passedtime, rlocked, ((GameBetRoom)gr).waitusers.size() < Config.maxUsersInGame(), gameusers));
				gameusers = null;
			}
			gr = null;
		}
		setroom = null;
		
		return rooms;
	}
	
	
	/****************************************ОБЫЧНАЯ ИГРА******************************************/
	//войти в игру(забег)
	public GameAction startRequest(){
		IClient client = Red5.getConnectionLocal().getClient();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(client.getId());
		GameActionWaitStart action = new GameActionWaitStart(GameActionType.WAIT_START, 0, Config.waitTimeToStart());
		
		if(user != null && !checkUserInGame(user.user.id)){
			if(user.user.level < 5){
				if(simplegamenews == null){
					simplegamenews = new GameRoom(RoomRandom.getRoomID(), "Игра", user.user.level);
					gamerooms.put(Integer.toString(simplegamenews.id), simplegamenews);
					action.added = simplegamenews.addwaituser(user);				
				}else{
					action.added = simplegamenews.addwaituser(user);
				}
			}else{
				if(simplegame == null){
					simplegame = new GameRoom(RoomRandom.getRoomID(), "Игра", user.user.level);
					gamerooms.put(Integer.toString(simplegame.id), simplegame);
					action.added = simplegame.addwaituser(user);				
				}else{
					action.added = simplegame.addwaituser(user);
				}
			}
		}
		
		client = null;
		user = null;
		return action;
	}
	
	public boolean checkUserInGame(int userid){
		Set<Entry<String, GameRoom>> set = gamerooms.entrySet();
		for (Map.Entry<String, GameRoom> room:set){
			if(room.getValue().waitusers.get(Integer.toString(userid)) != null) return true;
		}
		return false;
	}
	
	public void clearNewGameRoomByLevel(int level){
		if(level < 5){
			simplegamenews = null;
		}else{
			simplegame = null;
		}
	}	
	
	public void removeGameRoom(Room room){
		gamerooms.remove(Integer.toString(room.id));	
    }
	
	public void updateJackpot(int addMoney, int roomID, int bet){
		Connection _sqlconnection = null;
		PreparedStatement updatemoney = null;
		PreparedStatement getmoney = null;
		ResultSet getmoneyRes = null;
		boolean jpwin = false;

		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));
		if(gameroom != null){
			int probability = 1;
			probability = Math.min(100, probability * bet);
			double delta = (double) probability / 1000;
			if (gameroom.finishedusers.size() > 0 && Math.random() > (double)(1 - delta)){				
				UserConnection jpwinner = gameroom.finishedusers.get(0);
				if(jpwinner != null){
					jpwin = true;				
					jpwinner.user.updateMoney(jpwinner.user.money + jackpot);
					ServerMouseApplication.application.commonroom.changeUserInfoByID(jpwinner.user.id, ChangeInfoParams.USER_MONEY, jpwinner.user.money, 0);
					ServerMouseApplication.application.commonroom.sendJPWinMessage(jackpot, jpwinner.user.title);					
					jackpot = 0;
				}				
			}
		}
		gameroom = null;
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			if (jpwin == true){
				updatemoney = _sqlconnection.prepareStatement("UPDATE jackpot SET money=?");
			}else{
				updatemoney = _sqlconnection.prepareStatement("UPDATE jackpot SET money=money+?");
			}
			updatemoney.setInt(1, addMoney);			
			if (updatemoney.executeUpdate() > 0){
				getmoney = _sqlconnection.prepareStatement("SELECT * FROM jackpot");    		
				getmoneyRes = getmoney.executeQuery();				
	    		if (getmoneyRes.next()){
	    			jackpot = getmoneyRes.getInt("money");	    			
	    			ServerMouseApplication.application.commonroom.sendJPMessage(jackpot);
	    		}
			}					
		} catch (SQLException e) {
			ServerMouseApplication.application.logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();    			    	
		    	if (updatemoney != null) updatemoney.close(); 
		    	if (getmoney != null) getmoney.close(); 
		    	if (getmoneyRes != null) getmoneyRes.close(); 
		    	_sqlconnection = null;
		    	updatemoney = null;
		    	getmoney = null;
		    	getmoneyRes = null;
		    }
		    catch (SQLException sqlx) {   
		    }
		}
	}
	
	public void addArtefact(int aid, int uid){
		Connection _sqlconnection = null;		
		PreparedStatement insertart = null;
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			insertart = _sqlconnection.prepareStatement("INSERT INTO artefacts (aid, uid) VALUES(?,?)");
			insertart.setInt(1, aid);
			insertart.setInt(2, uid);			
			if (insertart.executeUpdate() > 0){				    	    				
			}
		}catch (SQLException e) {
			ServerMouseApplication.application.logger.error("GM1" + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (insertart != null) insertart.close(); 		    	
		    	_sqlconnection = null;
		    	insertart = null;		    	
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void gotoleft(int roomID, Boolean down, Float userx, Float usery, Float lvx, Float lvy){		
		IClient client = Red5.getConnectionLocal().getClient();
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));
		if (gameroom != null){
			gameroom.gotoleft(client, down, userx, usery, lvx, lvy);
		}
		gameroom = null;
		client = null;
	}
	public void gotoright(int roomID, Boolean down, Float userx, Float usery, Float lvx, Float lvy){	
		IClient client = Red5.getConnectionLocal().getClient();
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));
		if (gameroom != null){
			gameroom.gotoright(client, down, userx, usery, lvx, lvy);
		}
		gameroom = null;
		client = null;
	}	
	public void jump(int roomID, Boolean down, Float userx, Float usery, Float lvx, Float lvy){
		IClient client = Red5.getConnectionLocal().getClient();
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));
		if (gameroom != null){
			gameroom.jump(client, down, userx, usery, lvx, lvy);
		}
		gameroom = null;
		client = null;
	}
	
	public void findsource(int roomID, Float userx, Float usery){		
		IClient client = Red5.getConnectionLocal().getClient();
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));
		if (gameroom != null){
			gameroom.findsource(client, userx, usery);
		}
		gameroom = null;
		client = null;
	}
	public void findexit(int roomID, Float userx, Float usery){		
		IClient client = Red5.getConnectionLocal().getClient();
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));;
		if (gameroom != null){
			gameroom.findexit(client, userx, usery);
		}
		gameroom = null;
		client = null;
	}
	public void userout(int roomID){
		IClient client = Red5.getConnectionLocal().getClient();
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));;
		if (gameroom != null){
			gameroom.userout(client);
		}
		gameroom = null;
		client = null;
	}
	
	public void userexit(int roomID){
		IClient client = Red5.getConnectionLocal().getClient();
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));;
		if (gameroom != null){
			gameroom.userexit(client);
		}
		gameroom = null;
		client = null;
	}
	
	public void useitem(int roomID, int itemID, int itemtype, int gwitemID, IClient client, Float itemx, Float itemy){
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));		
		if (gameroom != null){
			gameroom.useitem(client, itemID, itemtype, gwitemID, itemx, itemy);
		}
		gameroom = null;
	}
	
	public void updateitems(int roomID, ArrayList<Object> items){
		GameRoom gameroom = gamerooms.get(Integer.toString(roomID));		
		if (gameroom != null){
			gameroom.updateitems(items);
		}
		gameroom = null;
	}
}
