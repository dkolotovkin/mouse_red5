package mouseapp.room.gamebet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.clan.ClanDeposit;
import mouseapp.clan.ClanRole;
import mouseapp.game.GameManager;
import mouseapp.room.game.GameRoom;
import mouseapp.user.User;
import mouseapp.user.UserConnection;
import mouseapp.utils.changeinfo.ChangeInfoParams;
import mouseapp.utils.extraction.ExtractionData;
import mouseapp.utils.gameaction.GameAction;
import mouseapp.utils.gameaction.GameActionSubType;
import mouseapp.utils.gameaction.GameActionType;
import mouseapp.utils.gameaction.event.GameActionEvent;
import mouseapp.utils.gameaction.finish.GameActionFinish;
import mouseapp.utils.gameaction.start.GameActionStart;
import mouseapp.utils.gameaction.start.GameActionStartType;
import mouseapp.utils.gameparams.GameParamsManager;
import mouseapp.utils.random.RoomRandom;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.service.IServiceCapableConnection;

public class GameBetRoom extends GameRoom {
	
	public int bet;
	public int bank;
	public String passward;
	
	public GameBetRoom(int id, String title, int bet, String passward){
		super(id, title, 0);
		this.bet = bet;
		this.passward = passward;
		bank = 0;		
		
		durationtime = Config.waitTimeToStartBet();
	}	
	
	@Override
	public void removeUserByConnectionID(String connID){
		if(!gameisover){
			UserConnection user = getUserByConnectionID(connID);			
			if (user != null){
				if (waitusers.size() > 0){
					if(bank >= bet){
						bank -= bet;
						user.user.updateMoney(user.user.money + bet);
					}
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
	public Boolean addwaituser(UserConnection u){
		if(waitusers.size() < Config.maxUsersInGame()){
			if (waitusers.get(Integer.toString(u.user.id)) == null){
				waitusers.put(Integer.toString(u.user.id), u);
				
				u.user.updateMoney(u.user.money - bet);
				bank += bet;
				
				ServerMouseApplication.application.commonroom.changeUserInfoByID(u.user.id, ChangeInfoParams.USER_MONEY, u.user.money, 0);
			}else{
				return false;
			}
		}else{
			return false;
		}
		return true;
	}
	
	@Override
	public void startGame(){
		timer.cancel();		
		
		if (waitusers.size() >= Config.minUsersInGame()){
			Set<Entry<String, UserConnection>> setWait = waitusers.entrySet();
			for (Map.Entry<String, UserConnection> user:setWait){				
				addUser(user.getValue());				
			}
			setWait = null;
			waitusers.clear();
			startCount = users.size();
			
			GameActionStart action = createGameActionStart();
			action.gametype = GameActionStartType.BET;
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
				
				user.getValue().user.updateMoney( user.getValue().user.money + bet);
				ServerMouseApplication.application.commonroom.changeUserInfoByID(user.getValue().user.id, ChangeInfoParams.USER_MONEY, user.getValue().user.money, 0);
				
				conn = null;
			}
			setWait = null;
			a = null;
			roomClear();
			ServerMouseApplication.application.gamemanager.removeGameRoom(this);			
		}		
	}
	
	@Override
	public void endGame(){
		if (timer != null){
			timer.cancel();
		}
		timer = null;		
		gameisover = true;
		
		Map<Integer, ClanDeposit> clandeposits = new HashMap<Integer, ClanDeposit>();
		
		boolean iswinner = false;
		
		double koef = (double) startCount / Config.maxUsersInGame();			
		int expprize = (int) Math.ceil(Config.experiencePrize() * koef);
		if (finishedusers.size() > 0){
			iswinner = true;
			for(int i = 0; i < finishedusers.size(); i++){			
				ExtractionData extraction = new ExtractionData(0, 0);
				
				UserConnection fuser = null;
				if(i < finishedusers.size()){
					fuser = finishedusers.get(i);
				}
				
				if(GameParamsManager.getExperienceBonus(fuser.user.accessorytype, fuser.user.colortype)){					
					extraction.experiencebonus = Config.experienceBonus();
				}
				if(GameParamsManager.getMoneyBonus(fuser.user.accessorytype, fuser.user.colortype)){						
					extraction.moneybonus = Config.moneyBonus();
				}
				if(GameParamsManager.getExperienceClanBonus(fuser.user.accessorytype, fuser.user.colortype)){
					extraction.cexperiencebonus = Config.experienceClanBonus();
				}
				
				int komission = (int)Math.ceil(((float) bank * 0.12));
				int moneyPrize = bank - komission;
				
				fuser.user.updateExperience(fuser.user.experience + expprize + extraction.experiencebonus);				
				
				if(fuser.user.claninfo.clanid > 0 && fuser.user.claninfo.clanrole > ClanRole.INVITED){
					extraction.cexperience = 1;
					if (clandeposits.get(fuser.user.claninfo.clanid) == null){
						ClanDeposit deposit = new ClanDeposit();
						deposit.depositm += (int)Math.floor((float) komission / 4);
						deposit.deposite += (1 + extraction.cexperiencebonus);
						clandeposits.put(fuser.user.claninfo.clanid, deposit);
						deposit = null;
					}else{
						clandeposits.get(fuser.user.claninfo.clanid).depositm += (int)Math.floor((float) komission / 4);
						clandeposits.get(fuser.user.claninfo.clanid).deposite += (1 + extraction.cexperiencebonus);
					}
					fuser.user.updateMoney(fuser.user.money + moneyPrize + extraction.moneybonus, (int)Math.floor((float) komission / 4), (1 + extraction.cexperiencebonus));								
				}else{
					fuser.user.updateMoney(fuser.user.money + moneyPrize + extraction.moneybonus);					
				}
				
				extraction.experience = expprize;
				extraction.money = moneyPrize;
				
				changeUserInfoByID(fuser.user.id, ChangeInfoParams.USER_MONEY_EXPERIENCE, fuser.user.money, fuser.user.experience);
				GameActionFinish action = new GameActionFinish(GameActionType.FINISH, this.id, extraction, (byte)(i + 1));
				
				if (fuser.conection != null && (fuser.conection instanceof IServiceCapableConnection)) {
		            ((IServiceCapableConnection) fuser.conection).invoke("processGameAction", new Object[]{action});            
		    	}
				extraction = null;
				action = null;			
				
				ServerMouseApplication.application.gamemanager.updateJackpot((int)Math.floor((float) komission / 3), this.id, bet);
				
				UserConnection u = fuser;
				if (u != null && u.conection != null && u.conection.getClient() != null){
					removeUserByConnectionID(u.conection.getClient().getId());
					u = null;
				}
				
				fuser = null;
			}
		}
		
		GameActionFinish action = new GameActionFinish(GameActionType.FINISH, this.id, null, (byte)0);
		
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){		
			if (!iswinner){
				user.getValue().user.updateMoney(user.getValue().user.money + bet);				
				changeUserInfoByID(user.getValue().user.id, ChangeInfoParams.USER_MONEY, user.getValue().user.money, 0);
			}
			IConnection conn = user.getValue().conection;
			if (conn != null && conn instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
	    	}			
			
			conn = null;
		}
		action = null;
		
		ServerMouseApplication.application.clanmanager.updateClanDeposits(clandeposits);
		clandeposits = null;
		
		roomClear();
		ServerMouseApplication.application.gamemanager.removeGameRoom(this);
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
	public void findexit(IClient client, Float userx, Float usery){
		UserConnection initiatorConn = getUserByConnectionID(client.getId());
		if (initiatorConn != null){
			User initiator = initiatorConn.user;
			if(userswithsource.get(Integer.toString(initiator.id)) != null && outusers.get(Integer.toString(initiator.id)) == null){
				userswithsource.remove(Integer.toString(initiator.id));
				finishedusers.add(getUserByConnectionID(client.getId()));			
				
				GameActionEvent action = new GameActionEvent(GameActionType.ACTION, this.id, GameActionSubType.FINDEXIT, initiator.id, initiator.title, (byte)finishedusers.size(), userx, usery);
				
				Set<Entry<String, UserConnection>> set = users.entrySet();
				for (Map.Entry<String, UserConnection> user:set){
					IConnection conn = user.getValue().conection;
					if(conn != null &&
						(outusers.get(Integer.toString(user.getValue().user.id)) == null) && 
						(outfinishusers.get(Integer.toString(user.getValue().user.id)) == null)){ 
						if (conn instanceof IServiceCapableConnection) {
				            ((IServiceCapableConnection) conn).invoke("processGameAction", new Object[]{action});            
				    	}
					}
					conn = null;
				}
				action = null;				
				endGame();
			}
			initiator = null;
			initiatorConn = null;
		}
	}
}
