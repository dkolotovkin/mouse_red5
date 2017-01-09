package mouseapp.room.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.message.MessageType;
import mouseapp.message.auctionstate.AuctionPrizeMessage;
import mouseapp.message.simple.MessageSimple;
import mouseapp.room.Room;
import mouseapp.user.AccessoryType;
import mouseapp.user.ColorType;
import mouseapp.user.UserConnection;
import mouseapp.user.UserForTop;
import mouseapp.utils.ban.BanType;
import mouseapp.utils.changeinfo.ChangeInfoParams;

import org.red5.server.api.IConnection;
import org.red5.server.api.service.IServiceCapableConnection;

public class CommonRoom extends Room {
	public Timer servertimer;
	public int update5minute = 0;
	public int update2minute = 0;
	public int update30second = 0;
	public int lastprizehour = 0;
	public Date date;
	
	public Map<Integer, Integer> userstimeonline;
	
	public CommonRoom(int id, String title){
		super(id, title);
		userstimeonline = new ConcurrentHashMap<Integer, Integer>();
		servertimer = new Timer();
		servertimer.schedule(new ServerTimerTask(), 10 * 1000, 10 * 1000);	
	}
	
	@Override
	public void addUser(UserConnection u){
		super.addUser(u);
		date = new Date();
		int currenttime = (int)(date.getTime() / 1000);
		if(userstimeonline.get(u.user.id) != null) userstimeonline.remove(u.user.id);
		userstimeonline.put(u.user.id, currenttime);
	}
	
	@Override
	public void removeUserByConnectionID(String connID){
		int userID = new Integer(userConnectionIDtoID.get(connID));		
		userstimeonline.remove(userID);
		super.removeUserByConnectionID(connID);	
	}

	public void showStartInfo(UserConnection u){
		if(u.user.level < 2 && u.user.colortype == ColorType.GRAY && u.user.setcolorat == 0){
			MessageSimple message = new MessageSimple(MessageType.START_INFO, this.id, "", u.user.id, 0);
			
			IConnection conn = u.conection;			
			if (conn instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
	    	}
		}
	}
	
	public void updateBan(UserConnection u, int currenttime){		
		int timepassed = currenttime - u.user.setbanat;
		boolean passed = false;
		
		int duration = 0;
		if (u.user.bantype == BanType.MINUT5){
			duration = 5 * 60;
			if (timepassed >= duration) passed = true;
		}else if (u.user.bantype == BanType.MINUT15){
			duration = 15 * 60;
			if (timepassed >= duration) passed = true;
		}else if (u.user.bantype == BanType.MINUT30){
			duration = 30 * 60;
			if (timepassed >= duration) passed = true;
		}else if (u.user.bantype == BanType.HOUR1){
			duration = 60 * 60;
			if (timepassed >= duration) passed = true;
		}else if (u.user.bantype == BanType.DAY1){
			duration = 60 * 60 * 24;
			if (timepassed >= duration) passed = true;
		}
		
		if (u.user.bantype != BanType.NO_BAN && u.user.changebanat != 0){
			int banexperience = Math.min((int) Math.floor((float)(Math.min(currenttime, u.user.setbanat + duration) - u.user.changebanat) / 60) * Config.valueEnergyUpdateInBan(), Config.maxValueEnergyUpdateInBan());
			int banpopular = Math.min((int) Math.floor((float)(Math.min(currenttime, u.user.setbanat + duration) - u.user.changebanat) / 60) * Config.valuePopularUpdateInBan(), Config.maxValueEnergyUpdateInBan());
			
			if (banexperience > 0 || banpopular > 0){
				u.user.updateExpAndPopul(u.user.experience - banexperience, u.user.popular - banpopular, currenttime);
				u.user.changebanat = currenttime;				
				changeUserInfoByID(u.user.id, ChangeInfoParams.USER_EXPERIENCE_AND_POPULAR, u.user.experience, u.user.popular);
			}
		}
		
		if(passed == true){
			u.user.bantype = BanType.NO_BAN;
			u.user.setbanat = 0;
			ServerMouseApplication.application.userinfomanager.banoff(u.user.id, u.user.ip);
		}
	}
	public int updateColor(UserConnection u, int currenttime){	
		int colortime = 0;
		int timepassed = currenttime - u.user.setcolorat;
		boolean passed = false;
		int day = 60 * 60 * 24;
		
		if(u.user.colortype != ColorType.GRAY){
			if (u.user.colortype == ColorType.BLACK1 || u.user.colortype == ColorType.WHITE1 ||
				u.user.colortype == ColorType.BLUE1 || u.user.colortype == ColorType.ORANGE1 || 
				u.user.colortype == ColorType.FIOLET1){
				if (timepassed >= day) passed = true;
				colortime = day - timepassed;
			}else if (u.user.colortype == ColorType.BLACK3 || u.user.colortype == ColorType.WHITE3 ||
					u.user.colortype == ColorType.BLUE3 || u.user.colortype == ColorType.ORANGE3 || 
					u.user.colortype == ColorType.FIOLET3){
				if (timepassed >= day * 3) passed = true;
				colortime = day * 3 - timepassed;
			}else if (u.user.colortype == ColorType.BLACK10 || u.user.colortype == ColorType.WHITE10 ||
					u.user.colortype == ColorType.BLUE10 || u.user.colortype == ColorType.ORANGE10 || 
					u.user.colortype == ColorType.FIOLET10){
				if (timepassed >= day * 10) passed = true;
				colortime = day * 10 - timepassed;
			}		
			
			if(passed == true){
				u.user.colortype = ColorType.GRAY;
				u.user.setcolorat = 0;
				ServerMouseApplication.application.userinfomanager.coloroff(u.user.id);
			}
		}
		
		colortime = Math.max(0, colortime);
		return colortime;
	}
	
	public int updateAccessories(UserConnection u, int currenttime){	
		int accessorytime = 0;
		int timepassed = currenttime - u.user.setaccessoryat;
		boolean passed = false;
		int day = 60 * 60 * 24;		
		if (u.user.accessorytype != AccessoryType.NOACCESSORY){			
			if (u.user.accessorytype == AccessoryType.PEN1 || u.user.accessorytype == AccessoryType.BANDAGE1 ||
				u.user.accessorytype == AccessoryType.CRONE1 || u.user.accessorytype == AccessoryType.CYLINDER1 || 
				u.user.accessorytype == AccessoryType.COOKHAT1 || u.user.accessorytype == AccessoryType.KOVBOYHAT1 || 
				u.user.accessorytype == AccessoryType.FLASHHAIR1 ||	u.user.accessorytype == AccessoryType.PUMPKIN1 || 
				u.user.accessorytype == AccessoryType.NYHAT1 ||	u.user.accessorytype == AccessoryType.NY1 ||
				u.user.accessorytype == AccessoryType.DOCTOR1 ||	u.user.accessorytype == AccessoryType.HELMET1 ||
				u.user.accessorytype == AccessoryType.ANGEL1 ||	u.user.accessorytype == AccessoryType.DEMON1 ||
				u.user.accessorytype == AccessoryType.POLICEHAT1 || u.user.accessorytype == AccessoryType.HIPHOP1 ||
				u.user.accessorytype == AccessoryType.GLAMUR1){
				if (timepassed >= day) passed = true;
				accessorytime = day - timepassed;
			}else if (u.user.accessorytype == AccessoryType.PEN3 || u.user.accessorytype == AccessoryType.BANDAGE3 ||
					u.user.accessorytype == AccessoryType.CRONE3 || u.user.accessorytype == AccessoryType.CYLINDER3 || 
					u.user.accessorytype == AccessoryType.COOKHAT3 || u.user.accessorytype == AccessoryType.KOVBOYHAT3 || 
					u.user.accessorytype == AccessoryType.FLASHHAIR3 ||	u.user.accessorytype == AccessoryType.PUMPKIN3 || 
					u.user.accessorytype == AccessoryType.NYHAT3 ||	u.user.accessorytype == AccessoryType.NY3 ||
					u.user.accessorytype == AccessoryType.DOCTOR3 || u.user.accessorytype == AccessoryType.HELMET3 ||
					u.user.accessorytype == AccessoryType.ANGEL3 || u.user.accessorytype == AccessoryType.DEMON3 ||
					u.user.accessorytype == AccessoryType.POLICEHAT3 || u.user.accessorytype == AccessoryType.HIPHOP3 ||
					u.user.accessorytype == AccessoryType.GLAMUR3){
				if (timepassed >= day * 3) passed = true;
				accessorytime = day * 3 - timepassed;
			}else if (u.user.accessorytype == AccessoryType.PEN10 || u.user.accessorytype == AccessoryType.BANDAGE10 ||
					u.user.accessorytype == AccessoryType.CRONE10 || u.user.accessorytype == AccessoryType.CYLINDER10 || 
					u.user.accessorytype == AccessoryType.COOKHAT10 || u.user.accessorytype == AccessoryType.KOVBOYHAT10 || 
					u.user.accessorytype == AccessoryType.FLASHHAIR10 || u.user.accessorytype == AccessoryType.PUMPKIN10 || 
					u.user.accessorytype == AccessoryType.NYHAT10 || u.user.accessorytype == AccessoryType.NY10 ||
					u.user.accessorytype == AccessoryType.DOCTOR10 || u.user.accessorytype == AccessoryType.HELMET10 ||
					u.user.accessorytype == AccessoryType.ANGEL10 || u.user.accessorytype == AccessoryType.DEMON10 ||
					u.user.accessorytype == AccessoryType.POLICEHAT10 || u.user.accessorytype == AccessoryType.HIPHOP10 ||
					u.user.accessorytype == AccessoryType.GLAMUR10){
				if (timepassed >= day * 10) passed = true;
				accessorytime = day * 10 - timepassed;
			}		
			
			if(passed == true){
				u.user.accessorytype = AccessoryType.NOACCESSORY;
				u.user.setaccessoryat = 0;
				ServerMouseApplication.application.userinfomanager.accessoryoff(u.user.id);
			}
		}
		accessorytime = Math.max(0, accessorytime);
		return accessorytime;
	}
	
	public void updateTransactionsMM(){		
		Connection _sqlconnection = null;		
		PreparedStatement updatetransaction = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;		
		
		try {			
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();			
			select = _sqlconnection.prepareStatement("SELECT * FROM transactionmm WHERE status=?");
			select.setInt(1, 0);
			selectRes = select.executeQuery();
    		while(selectRes.next()){    			
    			int sms_price = selectRes.getInt("smsprice");
    			int other_price = selectRes.getInt("otherprice");
    			//String uidsocial = selectRes.getString("uidsocial");
    			int transactionid = selectRes.getInt("id");
    			int userid = selectRes.getInt("userid");
    			
    			int money = 0;
    			if(sms_price == 1){
    				money += 2000;
    			}else if(sms_price == 3){
    				money += 7000;
    			}else if(sms_price == 5){
    				money += 13000;
    			}    			
    			if(other_price >= 2500 && other_price < 8000){
    				money += 2000;
    			}else if(other_price >= 8000 && other_price < 12000){
    				money += 7000;
    			}else if(other_price >= 12000 && other_price < 40000){
    				money += 13000;
    			}else if(other_price >= 40000){
    				money += 45000;
    			}
    			
    			UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(userid));
    			
    			if(user != null){
	    			ServerMouseApplication.application.userinfomanager.addMoney(userid, money, user);   			
					user = null;
					
					updatetransaction = _sqlconnection.prepareStatement("UPDATE transactionmm SET status=1 WHERE id=?");
					updatetransaction.setInt(1, transactionid);
					updatetransaction.executeUpdate();
    			}
    		}
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (select != null) select.close(); 
		    	if (selectRes != null) selectRes.close();		    	
		    	if (updatetransaction != null) updatetransaction.close();
		    	_sqlconnection = null;
		    	select = null;
		    	selectRes = null;		    	
		    	updatetransaction = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void updateTransactionsOD(){		
		Connection _sqlconnection = null;		
		PreparedStatement updatetransaction = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;		
		
		try {			
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();			
			select = _sqlconnection.prepareStatement("SELECT * FROM transactionod WHERE status=?");
			select.setInt(1, 0);
			selectRes = select.executeQuery();
    		while(selectRes.next()){    			
    			int price = selectRes.getInt("price");    			
    			//String uidsocial = selectRes.getString("uidsocial");
    			int transactionid = selectRes.getInt("id");
    			int userid = selectRes.getInt("userid");
    			
    			int money = 0;
    			if(price == 20){
    				money += 1500;
    			}else if(price == 50){
    				money += 4000;
    			}else if(price == 160){
    				money += 14000;
    			}else if(price == 240){
    				money += 23000;
    			}else if(price == 400){
    				money += 40000;
    			}
    			UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(userid));
    			
    			if(user != null && money > 0){
	    			ServerMouseApplication.application.userinfomanager.addMoney(userid, money, user);   			
					user = null;
					
					updatetransaction = _sqlconnection.prepareStatement("UPDATE transactionod SET status=1 WHERE id=?");
					updatetransaction.setInt(1, transactionid);
					updatetransaction.executeUpdate();
    			}
    		}
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (select != null) select.close(); 
		    	if (selectRes != null) selectRes.close();		    	
		    	if (updatetransaction != null) updatetransaction.close();
		    	_sqlconnection = null;
		    	select = null;
		    	selectRes = null;		    	
		    	updatetransaction = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void updateClansMoney(){
		Connection _sqlconnection = null;		
		PreparedStatement updateclan = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();			
			select = _sqlconnection.prepareStatement("SELECT * FROM clan WHERE money>0");	
			selectRes = select.executeQuery();
    		while(selectRes.next()){
    			UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(selectRes.getInt("ownerid")));    			
    			ServerMouseApplication.application.userinfomanager.addMoney(selectRes.getInt("ownerid"), selectRes.getInt("money"), user);
    			
    			updateclan = _sqlconnection.prepareStatement("UPDATE clan SET money=0 WHERE ownerid=?");
				updateclan.setInt(1, selectRes.getInt("ownerid"));
				updateclan.executeUpdate();
				
				user = null;				
    		}
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (select != null) select.close(); 
		    	if (selectRes != null) selectRes.close();		    	
		    	if (updateclan != null) updateclan.close();
		    	_sqlconnection = null;
		    	select = null;
		    	selectRes = null;		    	
		    	updateclan = null;
		    }
		    catch (SQLException sqlx) {
		    }
		}
	}
	
	public void sendHourPrize(){
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){			
			ServerMouseApplication.application.userinfomanager.updateUser(user.getValue().user);
		}
		ServerMouseApplication.application.userinfomanager.updateTopHourUsers();
		
		UserConnection user;
		UserForTop usertop;
		for(int i = 0; i < 5; i++){
			usertop = ServerMouseApplication.application.userinfomanager.topHourUsers.get(i);
			if(usertop != null && usertop.exphour > 0){
				user = users.get(Integer.toString(usertop.id));
				
				ServerMouseApplication.application.userinfomanager.addMoney(usertop.id, Config.exphourprizes.get(i), user);
				
				if(user != null){					
					AuctionPrizeMessage message = new AuctionPrizeMessage(MessageType.BEST_HOUR, this.id, Config.exphourprizes.get(i));
					IConnection conn = user.conection;
					if(conn != null){
						if (conn instanceof IServiceCapableConnection) {
				            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
				    	}
					}
					message = null;
				}				
				sendSystemMessage("Лучший игрок за час. "  + (i + 1) + " место : " + usertop.title + ". Приз - " + Config.exphourprizes.get(i) + " евро.");
			}
		}
		user = null;
		usertop = null;
		setNullExpHour();
	}
	private void setNullExpHour(){		
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){			
			user.getValue().user.exphour = 0;
		}
		
		Connection _sqlconnection = null;				
		PreparedStatement uppdate = null;
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();			
			uppdate = _sqlconnection.prepareStatement("UPDATE user SET exphour=?");
			uppdate.setInt(1, 0);
			uppdate.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();		    	
		    	if (uppdate != null) uppdate.close();		    	
		    	_sqlconnection = null;
		    	uppdate = null;		    	
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void sendDayPrize(){
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){			
			ServerMouseApplication.application.userinfomanager.updateUser(user.getValue().user);
		}
		ServerMouseApplication.application.userinfomanager.updateTopDayUsers();
		
		UserConnection user;
		UserForTop usertop;
		for(int i = 0; i < 5; i++){
			usertop = ServerMouseApplication.application.userinfomanager.topDayUsers.get(i);
			if(usertop != null && usertop.expday > 0){
				user = users.get(Integer.toString(usertop.id));
				
				ServerMouseApplication.application.userinfomanager.addMoney(usertop.id, Config.expdayprizes.get(i), user);
				
				if(user != null){					
					AuctionPrizeMessage message = new AuctionPrizeMessage(MessageType.BEST_DAY, this.id, Config.expdayprizes.get(i));
					IConnection conn = user.conection;
					if(conn != null){
						if (conn instanceof IServiceCapableConnection) {
				            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
				    	}
					}
					message = null;
				}				
				sendSystemMessage("Лучший игрок за день. "  + (i + 1) + " место : " + usertop.title + ". Приз - " + Config.expdayprizes.get(i) + " евро.");
			}
		}
		user = null;
		usertop = null;
		setNullExpDay();
	}
	private void setNullExpDay(){		
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){			
			user.getValue().user.expday = 0;
		}
		
		Connection _sqlconnection = null;				
		PreparedStatement uppdate = null;
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();			
			uppdate = _sqlconnection.prepareStatement("UPDATE user SET expday=?");
			uppdate.setInt(1, 0);
			uppdate.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();		    	
		    	if (uppdate != null) uppdate.close();		    	
		    	_sqlconnection = null;
		    	uppdate = null;		    	
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	@Override
	public void roomClear(){
		super.roomClear();
    }
	
	class ServerTimerTask extends TimerTask{
        public void run(){
        	update30second++;
        	update2minute++;
        	update5minute++;
        	try{
	        	date = new Date();
				int currenttime = (int)(date.getTime() / 1000);
				boolean needupdate2minute = false;
				boolean needupdate30second = false;
				
				int second = 1;
				int minute = second * 60;
				int hour = minute * 60;
				int hoursInDay = 24;
				
				int currenthour = (int) Math.floor((float) currenttime / hour);
				int currentminute = (int) Math.floor((float) (currenttime - currenthour * hour) / minute);
//				int currentsecond = currenttime - currentminute * minute - currenthour * hour;
				
				if(Config.SERVERTYPE == Config.MAINSERVER){
					if(((currenthour + 6) % hoursInDay == 0) && (currentminute == 0) && lastprizehour != currenthour){			//everyday in 22-00
						lastprizehour = currenthour;
						sendDayPrize();					
					}else if(currentminute == 0 && lastprizehour != currenthour){												//every hour
						lastprizehour = currenthour;
						sendHourPrize();					
					}
				}
				
				if (update2minute >= 6 * 2 + 1){
					update2minute = 0;	    				
					needupdate2minute = true;					
    			}
				
				if (update30second >= 3){
					update30second = 0;	    				
					needupdate30second = true;
    			}
				
				if (update5minute >= 6 * 5 + 3){
					update5minute = 0;
    				ServerMouseApplication.application.userinfomanager.updateTopUsers();
    				ServerMouseApplication.application.userinfomanager.updateTopHourUsers();
    				ServerMouseApplication.application.userinfomanager.updateTopDayUsers();
    				ServerMouseApplication.application.userinfomanager.updatePopularTopUsers();    				
    			}
								
				updateTransactionsMM();
				updateTransactionsOD();
				if(needupdate2minute) updateClansMoney();
				
	        	Set<Entry<String, UserConnection>> set = users.entrySet();
	    		for (Map.Entry<String, UserConnection> user:set){
	    			if (needupdate2minute == true){
	    				updateColor(user.getValue(), currenttime);
	    				updateAccessories(user.getValue(), currenttime);
	    			}
	    			
	    			if(needupdate30second){
	    				updateBan(user.getValue(), currenttime);
	    			}
	    		}	    		
        	}catch(Throwable e){
        		ServerMouseApplication.application.logger.log("Throwable: " + e.toString());
        	}
         }  
     }
}
