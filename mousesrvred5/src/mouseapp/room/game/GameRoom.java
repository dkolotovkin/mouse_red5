package mouseapp.room.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.clan.ClanDeposit;
import mouseapp.clan.ClanRole;
import mouseapp.game.GameManager;
import mouseapp.message.MessageType;
import mouseapp.message.status.MessageStatusGame;
import mouseapp.room.Room;
import mouseapp.shop.item.ItemType;
import mouseapp.user.GameInfo;
import mouseapp.user.User;
import mouseapp.user.UserConnection;
import mouseapp.utils.changeinfo.ChangeInfoParams;
import mouseapp.utils.extraction.ExtractionData;
import mouseapp.utils.gameaction.GameAction;
import mouseapp.utils.gameaction.GameActionSubType;
import mouseapp.utils.gameaction.GameActionType;
import mouseapp.utils.gameaction.event.GameActionEvent;
import mouseapp.utils.gameaction.finish.GameActionFinish;
import mouseapp.utils.gameaction.motion.GameActionMotion;
import mouseapp.utils.gameaction.start.GameActionStart;
import mouseapp.utils.gameaction.start.GameActionStartType;
import mouseapp.utils.gameaction.updateitems.GameActionUpdateItems;
import mouseapp.utils.gameaction.useitem.GameActionUseItem;
import mouseapp.utils.gameparams.GameParamsManager;
import mouseapp.utils.random.RoomRandom;
import mouseapp.utils.xmltostring.XmlToStringBuilder;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.service.IServiceCapableConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GameRoom extends Room {
	public Map<String, UserConnection> waitusers;
	public List<UserConnection> finishedusers;	
	public Map<String, UserConnection> userswithsource;
	public Map<String, UserConnection> outusers;				//те кто упали и след€т за происход€щим
	public Map<String, UserConnection> exitusers;				//те кто сами вышли из игры и не след€т за происход€щим
	public Map<String, UserConnection> outfinishusers;
	
	public Timer timer;
	public int startCount = 0;
	public boolean gameisover = false;
	public int currentItemID = 0;
	public GameRoom link;
	
	public int durationtime;
	public int passedtime = 0;
	public int creatorlevel = 0;
	
	public GameRoom(int id, String title, int creatorlevel){
		super(id, title);
		
		this.creatorlevel = creatorlevel;
		link = this;
		finishedusers = new ArrayList<UserConnection>();
		outusers = new ConcurrentHashMap<String, UserConnection>();
		exitusers = new ConcurrentHashMap<String, UserConnection>();
		outfinishusers = new ConcurrentHashMap<String, UserConnection>();
		waitusers = new ConcurrentHashMap<String, UserConnection>();
		userswithsource = new ConcurrentHashMap<String, UserConnection>();
		
		durationtime = Config.waitTimeToStart();
		setTimerToStart();
	}
	
	public void setTimerToStart(){
		timer = new Timer();
		timer.schedule(new TimerToStart(), 1000, 1000);
	}
	
	public Boolean addwaituser(UserConnection u){	
		if(waitusers.size() < Config.maxUsersInGame()){
			if (waitusers.get(Integer.toString(u.user.id)) == null){
				waitusers.remove(Integer.toString(u.user.id));
			}
			
			waitusers.put(Integer.toString(u.user.id), u);	
			if (waitusers.size() == Config.maxUsersInGame()){
				startGame();
			}
		}else{
			return false;
		}
		return true;
	}
	
	public void startGame(){
		timer.cancel();
		ServerMouseApplication.application.gamemanager.clearNewGameRoomByLevel(creatorlevel);
		
		if (waitusers.size() >= Config.minUsersInGame()){
			Set<Entry<String, UserConnection>> setWait = waitusers.entrySet();
			for (Map.Entry<String, UserConnection> user:setWait){				
				addUser(user.getValue());
				
				user.getValue().user.pet.energy = Math.min(100, Math.max(user.getValue().user.pet.energy - Config.energyPetForGame(), 0));				
			}
			setWait = null;
			
			waitusers.clear();
			startCount = users.size();
			
			GameActionStart action = createGameActionStart();		
			action.gametype = GameActionStartType.SIMPLE;
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
		}else{
			GameAction a = new GameAction(GameActionType.NOT_ENOUGH_USERS, this.id);
			Set<Entry<String, UserConnection>> setWait = waitusers.entrySet();
			for (Map.Entry<String, UserConnection> user:setWait){
				IConnection conn = user.getValue().conection;
				if (conn != null && (conn instanceof IServiceCapableConnection)) {
		            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{a});            
		    	}
				conn = null;
			}
			setWait = null;
			a = null;
			
			roomClear();
			ServerMouseApplication.application.gamemanager.removeGameRoom(this);			
		}		
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
		
		Map<Integer, ClanDeposit> clandeposits = new HashMap<Integer, ClanDeposit>();
		
		double koef = (double) startCount / Config.maxUsersInGame();			
		int currentExperiencePrize = (int) Math.ceil(Config.experiencePrize() * koef);
		int currentMoneyPrize = Config.moneyPrize();
		
		for(int i = 0; i < finishedusers.size(); i++){
			ExtractionData extraction = new ExtractionData(0, 0);
			
			UserConnection fuser = null;
			if(i < finishedusers.size()){
				fuser = finishedusers.get(i);
			}			
			
			if (currentExperiencePrize > 0 && fuser != null){
				int moneyPrize = 0;
				
				if(GameParamsManager.getExperienceBonus(fuser.user.accessorytype, fuser.user.colortype)){				
					extraction.experiencebonus = Config.experienceBonus();
				}
				if(GameParamsManager.getMoneyBonus(fuser.user.accessorytype, fuser.user.colortype)){						
					extraction.moneybonus = Config.moneyBonus();
				}
				if(GameParamsManager.getExperienceClanBonus(fuser.user.accessorytype, fuser.user.colortype)){
					extraction.cexperiencebonus = Config.experienceClanBonus();
				}
				
				extraction.artefactID = updatePetParams(fuser);
				if(extraction.artefactID > 0){
					fuser.user.pet.experience = Math.max(0, fuser.user.pet.experience + 1);					
					ServerMouseApplication.application.gamemanager.addArtefact(extraction.artefactID, fuser.user.id);
				}
				
				fuser.user.updateExperience(fuser.user.experience + currentExperiencePrize + extraction.experiencebonus);
				double koefMoney = (double) (i + 1) / startCount;				
				if (Math.round((double) (100 * koefMoney)) <= Config.percentMoneyUsers() && currentMoneyPrize > 0){
					moneyPrize = currentMoneyPrize;
					
					if(fuser.user.claninfo.clanid > 0 && fuser.user.claninfo.clanrole > ClanRole.INVITED){
						extraction.cexperience = 1;
						if (clandeposits.get(fuser.user.claninfo.clanid) == null){
							ClanDeposit deposit = new ClanDeposit();
							deposit.depositm += 1;
							deposit.deposite += (1 + extraction.cexperiencebonus);
							clandeposits.put(fuser.user.claninfo.clanid, deposit);
							deposit = null;
						}else{
							clandeposits.get(fuser.user.claninfo.clanid).depositm += 1;
							clandeposits.get(fuser.user.claninfo.clanid).deposite += (1 + extraction.cexperiencebonus);
						}
						fuser.user.updateMoney(fuser.user.money + moneyPrize + extraction.moneybonus, 1, (1 + extraction.cexperiencebonus));										
					}else{
						fuser.user.updateMoney(fuser.user.money + moneyPrize + extraction.moneybonus);
					}
					currentMoneyPrize -= 3;
				}
				
				
				extraction.experience = currentExperiencePrize;
				extraction.money = moneyPrize;				
				
				changeUserInfoByID(fuser.user.id, ChangeInfoParams.USER_MONEY_EXPERIENCE_PET_EXPERIENCE, fuser.user.money, fuser.user.experience, fuser.user.pet.experience);
			}
			GameActionFinish action = new GameActionFinish(GameActionType.FINISH, this.id, extraction, (byte)(i + 1));
			
			if (fuser.conection != null && (fuser.conection instanceof IServiceCapableConnection)) {
	            ((IServiceCapableConnection) fuser.conection).invoke("processGameAction", new Object[]{action});            
	    	}
			extraction = null;
			action = null;
			
			currentExperiencePrize = Math.max(currentExperiencePrize - Config.experiencePrizeDelta(), 0);
			UserConnection u = fuser;
			if (u != null && u.conection != null && u.conection.getClient() != null){
				removeUserByConnectionID(u.conection.getClient().getId());
				u = null;
			}
			
			fuser = null;
		}		
		
		GameActionFinish action = new GameActionFinish(GameActionType.FINISH, this.id, null, (byte)0);
		
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
		
		ServerMouseApplication.application.clanmanager.updateClanDeposits(clandeposits);
		clandeposits.clear();
		clandeposits = null;
		
		roomClear();
		ServerMouseApplication.application.gamemanager.removeGameRoom(this);
	}
	
	public int updatePetParams(UserConnection user){
		double probability = 0;
		double probabilityenergy = (double) user.user.pet.energy / 100;
		
		if(user.user.pet.level > 0 && ServerMouseApplication.application.petmanager.params.probability.size() >= user.user.pet.level){
			probability = (double) ServerMouseApplication.application.petmanager.params.probability.get(user.user.pet.level - 1);			
			probability = probability * probabilityenergy;
			
			if (Math.random() < probability){
				if(ServerMouseApplication.application.petmanager.petsartefacts.size() >= user.user.pet.level){
					List<Integer> artefacts = ServerMouseApplication.application.petmanager.petsartefacts.get(user.user.pet.level - 1);
					
					int index = (int) Math.floor((double) Math.random() * (artefacts.size() - 1));
					int artefactID = artefacts.get(index);
					
					return artefactID;					
				}
			}			
		}
		return 0;
	}
	
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
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			gameusers.add(user.getValue().user.id);
		}		
		
        GameActionStart action = new GameActionStart(GameActionType.START, this.id, gameusers, time, locationXML);
        
        locationFile = null;             
        gameusers = null;
        
        return action;
	}
	
	public File getLocationFile(){
		File locationFile = null;
		do{			
			locationFile = GameManager.locations[RoomRandom.getRandomFromTo(0, GameManager.locations.length - 1)];				
		}while(!locationFile.isFile());
		return locationFile;
	}
	
	@Override
	public void removeUserByConnectionID(String connID){
		if(!gameisover){
			UserConnection user = getUserByConnectionID(connID);			
			if (user != null){
				if (waitusers.size() > 0){
					waitusers.remove(Integer.toString(user.user.id));
				}else{
					boolean infinish = false;
					for( int i = 0; i < finishedusers.size(); i ++){
						if (finishedusers.get(i).user.id == user.user.id){
							infinish = true;
						}
					}
					if (!infinish){
						if (exitusers.get(Integer.toString(user.user.id)) == null){
							exitusers.put(Integer.toString(user.user.id), user);
						}
					}else{
						if (outfinishusers.get(Integer.toString(user.user.id)) == null){
							outfinishusers.put(Integer.toString(user.user.id), user);
						}						
					}
					
					if (outusers.size() + finishedusers.size() + exitusers.size() == users.size()){
						endGame();
					}
				}
			}
			user = null;
		}else{
			super.removeUserByConnectionID(connID);
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
		UserConnection initiator = getUserByConnectionID(client.getId());
		if(initiator != null){
			GameActionMotion action = new GameActionMotion(GameActionType.ACTION, this.id, GameActionSubType.GOTOLEFT, initiator.user.id, down, userx, usery, lvx, lvy);
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if (conn != null && user.getValue().user.id != initiator.user.id && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
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
	}
	
	public void gotoright(IClient client, Boolean down, Float userx, Float usery, Float lvx, Float lvy){
		UserConnection initiator = getUserByConnectionID(client.getId());
		if(initiator != null){
			GameActionMotion action = new GameActionMotion(GameActionType.ACTION, this.id, GameActionSubType.GOTORIGHT, initiator.user.id, down, userx, usery, lvx, lvy);
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if (conn != null && user.getValue().user.id != initiator.user.id && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
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
	}	
	public void jump(IClient client, Boolean down, Float userx, Float usery, Float lvx, Float lvy){
		UserConnection initiator = getUserByConnectionID(client.getId());
		if(initiator != null){
			GameActionMotion action = new GameActionMotion(GameActionType.ACTION, this.id, GameActionSubType.JUMP, initiator.user.id, down, userx, usery, lvx, lvy);
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if (conn != null && user.getValue().user.id != initiator.user.id && exitusers.get(Integer.toString(user.getValue().user.id)) == null){
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
				if (finishedusers.size() + outusers.size() + exitusers.size() == users.size()){					
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
			Boolean infinished = false;
			for(int i = 0; i < finishedusers.size(); i++){
				if(finishedusers.get(i).user.id == initiatorConn.user.id){
					infinished = true;
				}
			}
			if(!infinished){
				if (outusers.get(Integer.toString(initiatorConn.user.id)) == null){
					outusers.put(Integer.toString(initiatorConn.user.id), initiatorConn);
				}				
				if (finishedusers.size() + outusers.size() + exitusers.size() == users.size()){				
					endGame();
				}
			}else{
				if (outfinishusers.get(Integer.toString(initiatorConn.user.id)) == null){
					outfinishusers.put(Integer.toString(initiatorConn.user.id), initiatorConn);
				}				
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
			Boolean infinished = false;
			for(int i = 0; i < finishedusers.size(); i++){
				if(finishedusers.get(i).user.id == initiatorConn.user.id){
					infinished = true;
				}
			}
			if(!infinished){
				if (exitusers.get(Integer.toString(initiatorConn.user.id)) == null){
					exitusers.put(Integer.toString(initiatorConn.user.id), initiatorConn);
				}				
				if (finishedusers.size() + outusers.size() + exitusers.size() == users.size()){				
					endGame();
				}
			}else{
				if (outfinishusers.get(Integer.toString(initiatorConn.user.id)) == null){
					outfinishusers.put(Integer.toString(initiatorConn.user.id), initiatorConn);
				}		
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
