package mouseapp.room.gamebets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.game.GameManager;
import mouseapp.message.MessageType;
import mouseapp.message.betgame.BetGameRequestMessage;
import mouseapp.message.status.MessageStatusGame;
import mouseapp.room.game.GameRoom;
import mouseapp.room.gamebet.BetInfo;
import mouseapp.room.gamebet.BetResult;
import mouseapp.shop.item.ItemType;
import mouseapp.user.GameInfo;
import mouseapp.user.User;
import mouseapp.user.UserConnection;
import mouseapp.utils.changeinfo.ChangeInfoParams;
import mouseapp.utils.gameaction.GameAction;
import mouseapp.utils.gameaction.GameActionSubType;
import mouseapp.utils.gameaction.GameActionType;
import mouseapp.utils.gameaction.event.GameActionEvent;
import mouseapp.utils.gameaction.finish.GameActionFinishBets;
import mouseapp.utils.gameaction.motion.GameActionMotion;
import mouseapp.utils.gameaction.retrn.GameActionReturn;
import mouseapp.utils.gameaction.start.GameActionBetsContent;
import mouseapp.utils.gameaction.start.GameActionStart;
import mouseapp.utils.gameaction.start.GameActionStartType;
import mouseapp.utils.gameaction.updateitems.GameActionUpdateItems;
import mouseapp.utils.gameaction.useitem.GameActionUseItem;
import mouseapp.utils.random.RoomRandom;
import mouseapp.utils.xmltostring.XmlToStringBuilder;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.service.IServiceCapableConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GameBetsRoom extends GameRoom {	
	public boolean started = false;
	public int creatorid;
	
	public List<BetInfo> bets = new ArrayList<BetInfo>();
	
	public GameBetsRoom(int id, String title, int creatorid){
		super(id, title, 0);	
		
		this.creatorid = creatorid;
		durationtime = Config.waitTimeToStartBets();
	}
	
	public int bet(UserConnection u, int betuser, int bet){
		if (waitusers.get(Integer.toString(betuser)) == null){
			return BetResult.NO_USER;
		}else{
			u.user.updateMoney(u.user.money - bet);
			ServerMouseApplication.application.commonroom.changeUserInfoByID(u.user.id, ChangeInfoParams.USER_MONEY, u.user.money, 0);
			
			for(int i = 0; i < bets.size(); i++){
				if(bets.get(i).userid == u.user.id && bets.get(i).betuserid == betuser){					
					bets.get(i).bet += bet;
					return BetResult.OK;
				}
			}
			
			BetInfo bi = new BetInfo(u.user.id, betuser, bet);
			bets.add(bi);
			return BetResult.OK;
		}
	}
	
	public void sendRequest(int userid){
		UserConnection user = users.get(Integer.toString(creatorid));
		if(user != null){
			BetGameRequestMessage message = new BetGameRequestMessage(MessageType.BET_GAME_REQUEST, this.id, userid);		
			IConnection conn = user.conection;
			if (conn instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
	    	}
		}
	}	
	
	@Override
	public void setTimerToStart(){
		if(started){
			timer = new Timer();
			timer.schedule(new TimerToStart(), 1000, 1000);
		}
	}
	
	@Override
	public Boolean addwaituser(UserConnection u){		
		if(waitusers.size() < Config.maxUsersInGame()){
			if (waitusers.get(Integer.toString(u.user.id)) == null){
				waitusers.remove(Integer.toString(u.user.id));
			}
			
			waitusers.put(Integer.toString(u.user.id), u);	
		}else{
			return false;
		}
		return true;
	}
	
	@Override
	public void startGame(){
		timer.cancel();
		
		startCount = users.size();
		
		GameActionStart action = createGameActionStart();
		action.gametype = GameActionStartType.BETS;
		setTimerToEnd(action.time);
		     
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			IConnection conn = user.getValue().conection;
			if (conn != null && (conn instanceof IServiceCapableConnection)) {
	            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
	    	}
			conn = null;
		}
		set = null;
		action = null;	
	}
	
	
	public void setTimerToEnd(int time){
		timer = new Timer();
		timer.schedule(new TimerToEnd(), time * 1000, time * 1000);
	}
	
	public void endGame(){
		if (timer != null){
			timer.cancel();
		}
		timer = null;		
		gameisover = true;		
		
		//возвращенные ставки
		Map<Integer, Integer> returnbets = new HashMap<Integer, Integer>();
		//выйгравшие ставки
		Map<Integer, Integer> winnerbets = new HashMap<Integer, Integer>();
		
		int winnerid = -1;
		if(finishedusers.size() > 0){
			winnerid = finishedusers.get(0).user.id;
		}
		
		int maxwinbet = 0;
		int winbets = 0;
		for(int i = 0; i < bets.size(); i++){
			if(bets.get(i).betuserid == winnerid){					
				winbets += bets.get(i).bet;
				if(bets.get(i).bet > maxwinbet){
					maxwinbet = bets.get(i).bet;
				}
			}			
		}
		
		int allbets = 0;
		for(int i = 0; i < bets.size(); i++){
			if(bets.get(i).betuserid != winnerid){				
				if(bets.get(i).bet > maxwinbet){
					int delta = Math.max(0, (bets.get(i).bet - maxwinbet));
					
					if(returnbets.get(bets.get(i).userid) != null){
						int lastvalue = returnbets.get(bets.get(i).userid);
						returnbets.put(bets.get(i).userid, lastvalue + delta);						
					}else{
						returnbets.put(bets.get(i).userid, delta);
					}
					
					UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(bets.get(i).userid));					
					ServerMouseApplication.application.userinfomanager.addMoney(bets.get(i).userid, delta, user);
					user = null;
					
					bets.get(i).bet = Math.min(bets.get(i).bet, maxwinbet);
				}
			}
			allbets += bets.get(i).bet;
		}		
		
		int jpfond = (int) Math.floor((float) allbets * 0.02);
		int winerprize = (int) Math.floor((float) allbets * 0.05);
		allbets = allbets - winerprize;								//деньги на ветер
		allbets = allbets - winerprize;								//приз победителю забега
		allbets = allbets - jpfond;									//вычитаем фонд ждекпота
		
		if(finishedusers.size() > 0){			
			UserConnection wuser = finishedusers.get(0);						
			if(wuser != null){
				UserConnection userInCommonRoom = ServerMouseApplication.application.commonroom.users.get(Integer.toString(wuser.user.id));
				ServerMouseApplication.application.userinfomanager.addMoney(finishedusers.get(0).user.id, winerprize, userInCommonRoom);
				userInCommonRoom = null;
			}else{
				ServerMouseApplication.application.userinfomanager.addMoney(finishedusers.get(0).user.id, winerprize, null);
			}
			wuser = null;
		}
		
		if(started){
			int prize = 0;
			float percent;
			for(int i = 0; i < bets.size(); i++){
				if(bets.get(i).betuserid == winnerid){
					percent = (float) bets.get(i).bet / winbets;
					prize = (int) Math.floor((float) allbets * percent);
					
					if(winnerbets.get(bets.get(i).userid) != null){
						int lastvalue = winnerbets.get(bets.get(i).userid);
						winnerbets.put(bets.get(i).userid, lastvalue + prize);						
					}else{
						winnerbets.put(bets.get(i).userid, prize);
					}					
					
					UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(bets.get(i).userid));
					ServerMouseApplication.application.userinfomanager.addMoney(bets.get(i).userid, prize, user);					
					user = null;
				}
			}
						
			//рассылаем всем сообщени€
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){				
				IConnection conn = user.getValue().conection;
				if (conn != null && conn instanceof IServiceCapableConnection) {
					
					int returnmoney = 0;
					if(returnbets.get(user.getValue().user.id) != null){
						returnmoney = returnbets.get(user.getValue().user.id);
					}
					int winnermoney = 0;
					if(finishedusers.size() > 0 && finishedusers.get(0) != null){
						if(finishedusers.get(0).user.id == user.getValue().user.id){
							winnermoney = winerprize;
						}
					}
					int prizemoney = 0;
					if(winnerbets.get(user.getValue().user.id) != null){
						prizemoney = winnerbets.get(user.getValue().user.id);
					}
					
					GameActionFinishBets action = new GameActionFinishBets(GameActionType.FINISH_BETS, this.id, returnmoney, winnermoney, prizemoney, -1);					
					((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});
					action = null;
		    	}			
				conn = null;
			}
			set = null;
			
			ServerMouseApplication.application.gamemanager.updateJackpot(jpfond, this.id, (int) Math.floor((float)allbets / bets.size()));
		}else{
			//если игра и не начиналась(вышел организатор забега)
			GameAction action = new GameAction(GameActionType.ORGANIZER_EXIT, this.id);
			
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){			
				IConnection conn = user.getValue().conection;
				if (conn != null && conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});
		    	}	
				conn = null;
			}
			set = null;
			action = null;
		}
		
		returnbets.clear();
		winnerbets.clear();
		
		roomClear();
		ServerMouseApplication.application.gamemanager.removeGameRoom(this);
	}
	
	@Override
	public GameActionStart createGameActionStart(){
		int time = 0;
		String locationXML = "";
		
		File locationFile = getLocationFile();
		
		if(GameManager.maps.get(locationFile.toString()) == null){			
			Document document = null;
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setValidating(false);
	        factory.setNamespaceAware(true);
	        try
	        {
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            document = builder.parse(locationFile);
	            document.getDocumentElement().normalize();
	            NodeList nodelist = document.getDocumentElement().getElementsByTagName("timer");
	            Element fstNmElmnt = (Element) nodelist.item(0);
	            
	            GameManager.maps.put(locationFile.toString(), XmlToStringBuilder.getStringFromNode(document));
	            GameManager.mapstimes.put(locationFile.toString(), new Integer(fstNmElmnt.getAttribute("time")));
	         
	            builder = null;
	            nodelist = null;
	            fstNmElmnt = null;
	        }
	        catch (Exception e){
	        	logger.log("Error Parse XML From Loading LocationXML");
	        	return null;
	        }
	        factory = null;
	        document = null;
		}
		
		locationXML = GameManager.maps.get(locationFile.toString());
		time = GameManager.mapstimes.get(locationFile.toString());
        
        List<Integer> gameusers = new ArrayList<Integer>();
		Set<Entry<String, UserConnection>> set = waitusers.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			gameusers.add(user.getValue().user.id);
		}
		
        GameActionStart action = new GameActionStart(GameActionType.START, this.id, gameusers, time, locationXML);
        
        locationFile = null;       
        gameusers = null;
        
        return action;
	}
	
	@Override
	public File getLocationFile(){
		File locationFile = null;
		do{			
			locationFile = GameManager.locationsbet[RoomRandom.getRandomFromTo(0, GameManager.locationsbet.length - 1)];				
		}while(!locationFile.isFile());
		return locationFile;
	}
	
	@Override
	public void removeUserByConnectionID(String connID){
		if(!started){
			UserConnection user = getUserByConnectionID(connID);	
			if(user != null && user.user.id != creatorid){
				if (waitusers.get(Integer.toString(user.user.id)) != null){
					waitusers.remove(Integer.toString(user.user.id));
				}
				for(int i = 0; i < bets.size(); i++){
					if(bets.get(i).betuserid == user.user.id){			
						UserConnection u = ServerMouseApplication.application.commonroom.users.get(Integer.toString(bets.get(i).userid));
						ServerMouseApplication.application.userinfomanager.addMoney(bets.get(i).userid, bets.get(i).bet, u);
						
						if(u != null){
							GameActionReturn raction = new GameActionReturn(GameActionType.RETURN_MONEY, this.id, user.user.id, bets.get(i).bet);
							if (u.conection != null && (u.conection instanceof IServiceCapableConnection)) {
					            ((IServiceCapableConnection) u.conection).invoke("processGameAction", new Object[]{raction});            
					    	}
						}
						
						bets.get(i).bet = 0;
						bets.remove(i);
						i = 0;
						u = null;
					}
				}				
				boolean last = gameisover;
				gameisover = true;
				super.removeUserByConnectionID(connID);
				gameisover = last;
				user = null;
				
				GameActionBetsContent action = new GameActionBetsContent(GameActionType.BETS_CONTENT, 0, Config.waitTimeToStartBet());
				action.betsinfo = ServerMouseApplication.application.gamemanager.getBetsInfo(this.id);
				
				IConnection conn = null;
				Set<Entry<String, UserConnection>> set = users.entrySet();
				for (Map.Entry<String, UserConnection> gu:set){				
					conn = gu.getValue().conection;
					if (conn instanceof IServiceCapableConnection) {					
						((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});
			    	}
				}
				action = null;				
			}else if(user != null && user.user.id == creatorid){
				endGame();
			}
		}		
    }	
	
	@Override
	public UserConnection getUserByConnectionID(String cID){
		String userID = userConnectionIDtoID.get(cID);
		if (userID != null && userID != "null"){
			return users.get(userID);
		}else{
			Set<Entry<String, UserConnection>> setWait = waitusers.entrySet();
			for (Map.Entry<String, UserConnection> user:setWait){				
				if(user.getValue().conection.getClient().getId() == cID){
					return user.getValue();
				}				
			}
			setWait = null;
		}
		return null;
	}
	
	@Override
	public void roomClear(){
		waitusers.clear();
		userswithsource.clear();
		finishedusers.clear();
		outusers.clear();
		exitusers.clear();
		outfinishusers.clear();
		bets.clear();
		link = null;
		if(timer != null){
			timer.cancel();
		}
		timer = null;
		
		super.roomClear();
    }
	
	@Override
	public void addUser(UserConnection u){
		users.put(Integer.toString(u.user.id), u);
		if (u.conection != null && u.conection.getClient() != null){
			userConnectionIDtoID.put(u.conection.getClient().getId(), Integer.toString(u.user.id));
		}
		userSocialIDtoID.put(u.user.idSocial, Integer.toString(u.user.id));
		
		MessageStatusGame message = new MessageStatusGame(MessageType.USER_IN, this, GameInfo.createFromUser(u.user));	
		List<GameInfo> usersinroom = new ArrayList<GameInfo>();
		
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			usersinroom.add(GameInfo.createFromUser(user.getValue().user));
			if(user.getValue().user.id != u.user.id){
				conn = user.getValue().conection;
				if (conn instanceof IServiceCapableConnection) { 
		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
		    	}
			}
		}
		
		message.messages = this.messages;	
		message.users = usersinroom;
		conn = u.conection;
		if (conn instanceof IServiceCapableConnection) { 
            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
    	}
		
		usersinroom = null;
		message = null;
		conn = null;
	}
	
	class TimerToStart extends TimerTask{
        public void run(){        	
        	passedtime++;
        	if (passedtime >= durationtime){
        		startGame();
        	}
         }  
     }
	class TimerToEnd extends TimerTask{
        public void run(){  
        	endGame();        	
        }
    }
	
	//ќЅ–јЅќ“„» » ѕќЋ№«ќ¬ј“я≈Ћ№— »’ —ќЅџ“»…
	public void gotoleft(IClient client, Boolean down, Float userx, Float usery, Float lvx, Float lvy){
		User initiator = getUserByConnectionID(client.getId()).user;
		GameActionMotion action = new GameActionMotion(GameActionType.ACTION, this.id, GameActionSubType.GOTOLEFT, initiator.id, down, userx, usery, lvx, lvy);
		
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			conn = user.getValue().conection;
			if (conn != null && user.getValue().user.id != initiator.id && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
		    	}
			}
		}
		set = null;
		conn = null;
		initiator = null;
		action = null;
	}
	
	public void gotoright(IClient client, Boolean down, Float userx, Float usery, Float lvx, Float lvy){
		User initiator = getUserByConnectionID(client.getId()).user;
		GameActionMotion action = new GameActionMotion(GameActionType.ACTION, this.id, GameActionSubType.GOTORIGHT, initiator.id, down, userx, usery, lvx, lvy);
		
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			conn = user.getValue().conection;
			if (conn != null && user.getValue().user.id != initiator.id && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
		    	}
			}
		}
		set = null;
		conn = null;
		initiator = null;
		action = null;
	}	
	public void jump(IClient client, Boolean down, Float userx, Float usery, Float lvx, Float lvy){
		User initiator = getUserByConnectionID(client.getId()).user;
		GameActionMotion action = new GameActionMotion(GameActionType.ACTION, this.id, GameActionSubType.JUMP, initiator.id, down, userx, usery, lvx, lvy);
		
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			conn = user.getValue().conection;
			if (conn != null && user.getValue().user.id != initiator.id && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
		    	}
			}
		}
		set = null;
		conn = null;
		initiator = null;
		action = null;
	}
	
	public void findsource(IClient client, Float userx, Float usery){
		UserConnection initiatorConn = getUserByConnectionID(client.getId());
		if (initiatorConn != null){
			User initiator = initiatorConn.user;
			
			if(userswithsource.get(Integer.toString(initiator.id)) == null){
				userswithsource.put(Integer.toString(initiator.id), getUserByConnectionID(client.getId()));
				
				GameActionEvent action = new GameActionEvent(GameActionType.ACTION, this.id, GameActionSubType.FINDSOURCE, initiator.id, initiator.title, (byte)userswithsource.size(), userx, usery);
				
				Set<Entry<String, UserConnection>> set = users.entrySet();
				for (Map.Entry<String, UserConnection> user:set){
					IConnection conn = user.getValue().conection;
					if(conn != null && exitusers.get(Integer.toString(user.getValue().user.id)) == null){ 
						if (conn instanceof IServiceCapableConnection) {
				            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
				    	}
					}
				}
				set = null;
				action = null;
			}
			initiator = null;
			initiatorConn = null;
		}		
	}
	
	public void findexit(IClient client, Float userx, Float usery){
		UserConnection initiatorConn = getUserByConnectionID(client.getId());
		if (initiatorConn != null){
			User initiator = initiatorConn.user;
			if(userswithsource.get(Integer.toString(initiator.id)) != null && outusers.get(Integer.toString(initiator.id)) == null && exitusers.get(Integer.toString(initiator.id)) == null){
				userswithsource.remove(Integer.toString(initiator.id));
				finishedusers.add(getUserByConnectionID(client.getId()));		
				
				GameActionEvent action = new GameActionEvent(GameActionType.ACTION, this.id, GameActionSubType.FINDEXIT, initiator.id, initiator.title, (byte)finishedusers.size(), userx, usery);
				
				Set<Entry<String, UserConnection>> set = users.entrySet();
				for (Map.Entry<String, UserConnection> user:set){
					IConnection conn = user.getValue().conection;
					if(conn != null && exitusers.get(Integer.toString(user.getValue().user.id)) == null){ 
						if (conn instanceof IServiceCapableConnection) {
				            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
				    	}
					}
				}
				action = null;				
				if(waitusers.get(Integer.toString(initiator.id)) != null){
					endGame();
				}
			}
			initiator = null;
			initiatorConn = null;
		}
	}
	
	public void userout(IClient client){
		UserConnection initiatorConn = getUserByConnectionID(client.getId());
		if (initiatorConn != null && (exitusers.get(Integer.toString(initiatorConn.user.id)) != null)){
			exitusers.remove(Integer.toString(initiatorConn.user.id));
		}
		
		if (initiatorConn != null && (outusers.get(Integer.toString(initiatorConn.user.id)) == null)){			
			
			if (outusers.get(Integer.toString(initiatorConn.user.id)) == null){
				outusers.put(Integer.toString(initiatorConn.user.id), initiatorConn);
			}				
			if (outusers.size() + exitusers.size() == waitusers.size()){				
				endGame();
			}
		}
		initiatorConn = null;
	}
	
	public void userexit(IClient client){
		UserConnection initiatorConn = getUserByConnectionID(client.getId());
		if (initiatorConn != null && (outusers.get(Integer.toString(initiatorConn.user.id)) != null)){
			outusers.remove(Integer.toString(initiatorConn.user.id));
		}		
		
		if (initiatorConn != null && (exitusers.get(Integer.toString(initiatorConn.user.id)) == null)){
			if (exitusers.get(Integer.toString(initiatorConn.user.id)) == null){
				exitusers.put(Integer.toString(initiatorConn.user.id), initiatorConn);
			}
			if (outusers.size() + exitusers.size() == waitusers.size()){				
				endGame();
			}
		}
		initiatorConn = null;
	}
	
	public void useitem(IClient client, int itemID, int itemtype, int gwitemID, Float itemx, Float itemy){
		UserConnection initiatorConn = getUserByConnectionID(client.getId());	
		if (initiatorConn != null){
			User initiator = initiatorConn.user;					
			
			GameActionUseItem action = null;
			if(itemtype == ItemType.MAGIC_HAND || itemtype == ItemType.GUN){
				action = new GameActionUseItem(GameActionType.USE_GAMEITEM, this.id, itemID, gwitemID, itemtype, initiator.id, itemx, itemy);
			}else{
				action = new GameActionUseItem(GameActionType.USE_GAMEITEM, this.id, itemID, currentItemID++, itemtype, initiator.id, itemx, itemy);
			}
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if(conn != null && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
					if (conn instanceof IServiceCapableConnection) {
			            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
			    	}
				}
			}
			conn = null;
			set = null;
			action = null;
			initiator = null;
			initiatorConn = null;
		}
	}
	
	public void updateitems(ArrayList<Object> items){		
		GameActionUpdateItems action = new GameActionUpdateItems(GameActionType.UPDATE_GWITEMS, this.id, items);
		
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			IConnection conn = user.getValue().conection;
			if(conn != null && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
		    	}
			}
		}
		set = null;
		action = null;
	}
}
