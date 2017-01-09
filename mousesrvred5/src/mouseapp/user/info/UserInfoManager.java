package mouseapp.user.info;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.clan.ClanUserInfo;
import mouseapp.game.GameManager;
import mouseapp.logger.MyLogger;
import mouseapp.message.MessageType;
import mouseapp.message.newlevel.MessageNewLevel;
import mouseapp.room.Room;
import mouseapp.room.game.GameRoom;
import mouseapp.shop.buyresult.BuyErrorCode;
import mouseapp.shop.buyresult.BuyResult;
import mouseapp.shop.checkluckresult.CheckLuckResult;
import mouseapp.user.AccessoryType;
import mouseapp.user.ColorType;
import mouseapp.user.User;
import mouseapp.user.UserConnection;
import mouseapp.user.UserForInit;
import mouseapp.user.UserForTop;
import mouseapp.user.UserFriend;
import mouseapp.user.UserMailMessage;
import mouseapp.user.UserRole;
import mouseapp.user.chage.ChangeResult;
import mouseapp.user.pet.PetInfo;
import mouseapp.utils.authorization.GameMode;
import mouseapp.utils.ban.BanResult;
import mouseapp.utils.ban.BanType;
import mouseapp.utils.changeinfo.ChangeInfoParams;
import mouseapp.utils.sex.Sex;

import org.red5.server.api.IConnection;
import org.red5.server.api.service.IServiceCapableConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class UserInfoManager {
	private MyLogger _logger = new MyLogger(UserInfoManager.class.getName());
	public Map<Integer, Integer> levels = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> levelsEnergy = new HashMap<Integer, Integer>();	
	public List<Integer> popularparts = Arrays.asList(0, 50, 500, 5000, 20000, 60000, 150000, 300000, 1000000);
	public List<String> populartitles = Arrays.asList("Никому не известный", "Узнаваемый", "Активист", "Популярный", "Уважаемый", "Авторитет", "Звезда", "Король мышей");
	
	public ArrayList<UserForTop> topUsers = new ArrayList<UserForTop>();
	public ArrayList<UserForTop> topHourUsers = new ArrayList<UserForTop>();
	public ArrayList<UserForTop> topDayUsers = new ArrayList<UserForTop>();
	public ArrayList<UserForTop> topPopularUsers = new ArrayList<UserForTop>();
	public int mousecounter = 0;
	
	public UserInfoManager(){
		Connection _sqlconnection = null;
		PreparedStatement getlevels = null;	
		ResultSet res = null;
		PreparedStatement selectusers = null;	
		ResultSet selectusersRes = null;
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			getlevels = _sqlconnection.prepareStatement("SELECT * FROM level");
			
			res = getlevels.executeQuery();					
			while(res.next()){				
				levelsEnergy.put(res.getInt("id"), res.getInt("energy"));
				levels.put(res.getInt("id"), res.getInt("experience"));
    		}
			selectusers = _sqlconnection.prepareStatement("SELECT count(id) FROM user");
			selectusersRes = selectusers.executeQuery();
			selectusersRes.next();
			mousecounter = selectusersRes.getInt(1);		
		}catch (SQLException e) {
			_logger.error("UM1 " + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (getlevels != null) getlevels.close();
		    	if (res != null) res.close();
		    	if (selectusers != null) selectusers.close();
		    	if (selectusersRes != null) selectusersRes.close();
		    	_sqlconnection = null;
		    	getlevels = null;
		    	res = null;
		    	selectusers = null;
		    	selectusersRes = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		
		updateTopUsers();
		updateTopHourUsers();
		updateTopDayUsers();
		updatePopularTopUsers();
	}
	
	public Boolean clientConnect(IConnection conn, String socialid, String passward, String ip){
		Boolean good = true;
    	if (socialid != null && socialid.length() > 0){
    		if (findAndAddUserToRoom(conn, socialid, ip)){
    			good = true;
    		}else{
    			if (insertUserToDataBase(conn, socialid) > 0){    				
    				if (findAndAddUserToRoom(conn, socialid, ip)){
    	    			good = true;
    	    		}else{
    	    			good = false;
    	    		}
    			}else{
    				_logger.error("User don't create in DataBase");        				
    				good = false;
    			}
    		}
    	}else{
    		_logger.log("Connect user failed. This is not social game(send title and passward and check user)");
    		good = false;
    	}
    	return good;
	}
	
	public void removeUserByConnID(String connID){
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(connID);
		if(user != null){
			Set<Entry<String, Room>> set = ServerMouseApplication.application.rooms.entrySet();
			for (Map.Entry<String, Room> room:set){
				room.getValue().removeUserByConnectionID(connID);			
			}
			
			Set<Entry<String, GameRoom>> gset = GameManager.gamerooms.entrySet();		
			for (Map.Entry<String, GameRoom> room:gset){    	
				room.getValue().removeUserByConnectionID(connID);			
			}
	    	user = null;
		}
	}
	
	public void updateParams(UserConnection user, String url){
		Connection _sqlconnection = null;
		PreparedStatement updateparams = null;
		
		try {
			if (user != null && (user.user.url == null || user.user.url.toLowerCase() == "null" || user.user.url.length() == 0) && url != null){
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				updateparams = _sqlconnection.prepareStatement("UPDATE user SET url=? WHERE id=?");
				updateparams.setString(1, url);
				updateparams.setInt(2, user.user.id);				
				updateparams.executeUpdate();
				
				user.user.url = url;
			}
		}catch (SQLException e) {			
			_logger.error("UM2 " + e.toString());
		} 
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 		    	
		    	if (updateparams != null) updateparams.close();
		    	_sqlconnection = null;		    	
		    	updateparams = null;
		    }
		    catch (SQLException sqlx) {   
		    }
		}
	}
	
	private Boolean findAndAddUserToRoom(IConnection conn, String socialid, String ip){
    	Boolean good = true;    	
    	Connection _sqlconnection = null;
    	PreparedStatement find = null;
    	ResultSet findRes = null;
    	PreparedStatement updateparams = null;
    	
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();    		    		
    		find = _sqlconnection.prepareStatement("SELECT * FROM user INNER JOIN clan ON user.clanid=clan.id INNER JOIN pets ON user.petid=pets.id WHERE idsocial=?");
    		find.setString(1, socialid);
    		findRes = find.executeQuery();    		
    		if (findRes.next()){
    			UserConnection user = null;    					
    			ClanUserInfo claninfo = new ClanUserInfo(findRes.getInt("user.clanid"), findRes.getString("clan.title"), findRes.getInt("user.clandepositm"), findRes.getInt("user.clandeposite"), findRes.getByte("user.clanrole"), findRes.getInt("user.getclanmoneyat"));
    			PetInfo pet = new PetInfo(findRes.getInt("pets.id"), findRes.getInt("pets.level"), findRes.getInt("pets.experience"), findRes.getInt("pets.energy"), findRes.getInt("pets.changeenergyat"));
    			
    			user = new UserConnection(findRes.getInt("user.id"), findRes.getString("user.idsocial"), ip, findRes.getInt("user.sex"), 
    										findRes.getString("user.title"), findRes.getInt("user.popular"), findRes.getInt("user.experience"), 
    										findRes.getInt("user.exphour"), findRes.getInt("user.expday"),findRes.getInt("user.lastlvl"),
    										findRes.getInt("user.money"), findRes.getByte("user.role"), (byte)findRes.getInt("user.bantype"),
    										findRes.getInt("user.setbanat"), findRes.getInt("user.changebanat"), findRes.getByte("user.colortype"), findRes.getInt("user.setcolorat"),
    										findRes.getByte("user.accessorytype"), findRes.getInt("user.setaccessoryat"), findRes.getString("user.url"), claninfo, pet, conn);
    			
    			Date date = new Date();
				int currenttime = (int)(date.getTime() / 1000);
    			int timepassed = currenttime - user.user.setbanat;
    			date = null;
    			
    			UserForInit ufi = UserForInit.createfromUser(user.user);
    			ufi.jackpot = GameManager.jackpot;
    			ufi.popularparts = popularparts;
    			ufi.populartitles = populartitles;
    			
    			if (user.user.bantype == BanType.NO_BAN){
    				ufi.bantime = 0;
    			}else if (user.user.bantype == BanType.MINUT5){
    				ufi.bantime = 5 * 60 - timepassed;    				
    			}else if (user.user.bantype == BanType.MINUT15){
    				ufi.bantime = 15 * 60 - timepassed;    				
    			}else if (user.user.bantype == BanType.MINUT30){
    				ufi.bantime = 30 * 60 - timepassed;    			
    			}else if (user.user.bantype == BanType.HOUR1){
    				ufi.bantime = 60 * 60 - timepassed;
    			}else if (user.user.bantype == BanType.DAY1){
    				ufi.bantime = 60 * 60 * 24 - timepassed;
    			}
    			ufi.colortime = ServerMouseApplication.application.commonroom.updateColor(user, currenttime);
    			ufi.accessorytime = ServerMouseApplication.application.commonroom.updateAccessories(user, currenttime);
    			
    			if (conn instanceof IServiceCapableConnection) {
    	            ((IServiceCapableConnection) conn).invoke("initPersParams", new Object[]{ufi});
    	    	}
    			
    			if(user.user.role == UserRole.MODERATOR || user.user.role == UserRole.ADMINISTRATOR || user.user.role == UserRole.ADMINISTRATOR_MAIN){
    				ServerMouseApplication.application.modsroom.addUser(user);
    			}
    			ServerMouseApplication.application.commonroom.addUser(user);
    			ServerMouseApplication.application.commonroom.updateBan(user, currenttime);
    			ServerMouseApplication.application.commonroom.showStartInfo(user);
    			
    			good = true;
    			ufi = null;
    			user = null;
    			conn = null;
    		}else{
    			good = false;
    		}
		} catch (Throwable e) {
			_logger.error("UM3 " + e.toString());	
			good = false;
		} 
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (find != null) find.close(); 
		    	if (findRes != null) findRes.close();
		    	if (updateparams != null) updateparams.close();
		    	_sqlconnection = null;
		    	find = null;
		    	findRes = null;
		    	updateparams = null;
		    }
		    catch (SQLException sqlx) {     
		    }
		}
    	return good;
    }
    private int insertUserToDataBase(IConnection conn, String socialid){
    	int countInsert = 0;
    	Connection _sqlconnection = null;
    	PreparedStatement insert = null;
    	
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		mousecounter++;
    		
    		insert = _sqlconnection.prepareStatement("INSERT INTO user (idsocial, sex, title, money) VALUES(?,?,?,?)");
    		insert.setString(1, socialid);
    		insert.setInt(2, Sex.MALE);
    		insert.setString(3, "Мышь " + mousecounter);
    		insert.setInt(4, 50);    		
			countInsert = insert.executeUpdate();
		} catch (SQLException e) {
			_logger.error("UM4 " + e.toString());			
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (insert != null) insert.close();
		    	_sqlconnection = null;
		    	insert = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
    	return countInsert;
    }
	
	public ChangeResult changeInfo(String title, int sex, String clientConnID, ChangeResult result, boolean startMode){
		Connection _sqlconnection = null;
		PreparedStatement finduser = null;
		ResultSet finduserRes = null;
		PreparedStatement update = null;
		
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		finduser = _sqlconnection.prepareStatement("SELECT * FROM user WHERE title=?");
    		finduser.setString(1, title);
    		finduserRes = finduser.executeQuery();    		
    		if (finduserRes.next() && startMode == false){
    			result.errorCode = ChangeResult.USER_EXIST;
    		}else{
    			UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(clientConnID);
    			if(initiator != null && initiator.conection != null){
	    			if (initiator.user.money >= Config.changeInfoPrice() || startMode == true){
	    				
	    				if(startMode == false){	    					
	    					initiator.user.updateMoney(initiator.user.money - Config.changeInfoPrice());
	    					ServerMouseApplication.application.commonroom.changeUserInfoByID(initiator.user.id, ChangeInfoParams.USER_MONEY, initiator.user.money, 0);
	    				}
	    				update = _sqlconnection.prepareStatement("UPDATE user SET title=?,sex=? WHERE id=?");
	    				update.setString(1, title);
	    				update.setInt(2, sex);    				
	    				update.setInt(3, initiator.user.id);
	    				
	            		if (update.executeUpdate() > 0){
	            			initiator.user.title = title;
	            			initiator.user.sex = sex;
	            			
	            			result.errorCode = ChangeResult.OK;                			
	            			result.user = initiator.user;            			
	            		}else{
	            			result.errorCode = ChangeResult.UNDEFINED;             			
	            		}                		
	    			}else{
	    				result.errorCode = ChangeResult.NO_MONEY;        				
	    			}
    			}
        		initiator = null;
    		}	
		} catch (SQLException e) {		
			_logger.error("UM5 " + e.toString());
		}	
		finally
		{
		    try{
		    	if (_sqlconnection != null)  _sqlconnection.close(); 
		    	
		    	if (finduser != null)  finduser.close(); 
		    	if (finduserRes != null)  finduserRes.close();
		    	if (update != null)  update.close();
		    	finduser = null;
		    	finduserRes = null;
		    	update = null;
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		return result;
	}	
	
	public Boolean addMoney(int iduser, int addmoney, UserConnection user){
		Connection _sqlconnection = null;
		PreparedStatement pstm = null;
		ResultSet res = null;
		PreparedStatement update = null;
		
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		if(user != null){
    			user.user.updateMoney(user.user.money + addmoney);
    			ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
    			
    			update = _sqlconnection.prepareStatement("UPDATE user SET money=? WHERE id=?");
				update.setInt(1, user.user.money);
				update.setInt(2, iduser);				
        		if (update.executeUpdate() > 0){
        			update.close();
        			return true;
        		}
    		}else{    		
	    		pstm = _sqlconnection.prepareStatement("SELECT * FROM user WHERE id=?");
	    		pstm.setInt(1, iduser);
	    		res = pstm.executeQuery();
	    		if (!res.next()){
	    		}else{    			
	    			update = _sqlconnection.prepareStatement("UPDATE user SET money=? WHERE id=?");
					update.setInt(1, Math.max(0, res.getInt("money") + addmoney));
					update.setInt(2, iduser);				
	        		if (update.executeUpdate() > 0){
	        			update.close();
	        			return true;
	        		}
	    		}
    		}
		} catch (SQLException e) {		
			_logger.error("UM6 " + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (pstm != null) pstm.close(); 
		    	if (res != null) res.close(); 
		    	if (update != null) update.close();
		    	_sqlconnection = null;
		    	pstm = null;
		    	res = null;
		    	update = null;
		    }
		    catch (SQLException sqlx) {
		    }
		}
		return false;
	}
	
	public byte ban(int bannedId, String initiatorConnId, byte type, boolean byip){	
		byte errorCode = 0;
		
		int price = 0;
		if (type == BanType.MINUT5){
			price = 100;
		}else if (type == BanType.MINUT15){
			price = 400;
		}else if (type == BanType.MINUT30){
			price = 800;
		}else if (type == BanType.HOUR1){
			price = 2000;
		}else if (type == BanType.DAY1){
			price = 25000;
		}
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(initiatorConnId);
		UserConnection banned = ServerMouseApplication.application.commonroom.users.get(Integer.toString(bannedId));
		
		Connection _sqlconnection = null;
		PreparedStatement updatemoney = null;
		PreparedStatement setban = null;
		
		Date date = new Date();
		int currenttime = (int)(date.getTime() / 1000);
		date = null; 		
		
		if (initiator.user.role == UserRole.MODERATOR || initiator.user.role == UserRole.ADMINISTRATOR){
			price = (int) Math.floor((float) price / 10);
		}
		if (initiator.user.role == UserRole.ADMINISTRATOR_MAIN){
			price = 0;
		}
		
		if(initiator != null && initiator.conection != null){
			if (initiator.user.money >= price){
				if(banned != null && banned.user.bantype <= type){
					initiator.user.updateMoney(initiator.user.money - price);
					ServerMouseApplication.application.commonroom.changeUserInfoByID(initiator.user.id, ChangeInfoParams.USER_MONEY, initiator.user.money, 0);
					
					try {						
						_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
						
						if((initiator.user.role == UserRole.ADMINISTRATOR || initiator.user.role == UserRole.ADMINISTRATOR_MAIN) && byip){
							setban = _sqlconnection.prepareStatement("UPDATE user SET bantype=?,setbanat=?,changebanat=? WHERE id=? or ip=?");
	        				setban.setInt(1, type);
	        				setban.setInt(2, currenttime);
	        				setban.setInt(3, currenttime);
	        				setban.setInt(4, bannedId);
	        				setban.setString(5, banned.user.ip);
	        				if (setban.executeUpdate() > 0){
	        					errorCode = BanResult.OK;
	        					
	        					Set<Entry<String, UserConnection>> set = ServerMouseApplication.application.commonroom.users.entrySet();
	        					for (Map.Entry<String, UserConnection> user:set){        						
	        						if(user.getValue().user.ip.toString().equals(banned.user.ip)){
	        							user.getValue().user.setbanat = currenttime;
	        							user.getValue().user.changebanat = currenttime;
	        							user.getValue().user.bantype = type;
	            						
	                					ServerMouseApplication.application.commonroom.sendBanMessage(user.getValue().user.id, initiatorConnId, type);
	        						}
	        					}			
	        					set = null;
	        				}		       
						}else{
							setban = _sqlconnection.prepareStatement("UPDATE user SET bantype=?,setbanat=?,changebanat=? WHERE id=?");
	        				setban.setInt(1, type);
	        				setban.setInt(2, currenttime);
	        				setban.setInt(3, currenttime);
	        				setban.setInt(4, bannedId);
	        				if (setban.executeUpdate() > 0){
	        					errorCode = BanResult.OK;
	        					
	        					banned.user.setbanat = currenttime;
	        					banned.user.changebanat = currenttime;
	        					banned.user.bantype = type;
	        					
            					ServerMouseApplication.application.commonroom.sendBanMessage(banned.user.id, initiatorConnId, type);
	        				}
						}
					}catch (SQLException e) {
						_logger.error("UM8 " + e.toString());
					}
					finally
					{
					    try{
					    	if (_sqlconnection != null) _sqlconnection.close();
					    	if (updatemoney != null) updatemoney.close();
					    	if (setban != null) setban.close();		
					    	_sqlconnection = null;
					    	updatemoney = null;
					    	setban = null;
					    }
					    catch (SQLException sqlx) {   
					    }
					}
				}else{
					errorCode = BanResult.OTHER; 
				}
			}else{
				errorCode = BanResult.NO_MONEY; 
			}
		}else{
			errorCode = BanResult.OTHER;
		}
		
		initiator = null;
		banned = null;
		return errorCode;
	}	
	
	public void banoff(int userId, String userIp){
		Connection _sqlconnection = null;
		PreparedStatement banoffst = null;		
				
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			banoffst = _sqlconnection.prepareStatement("UPDATE user SET bantype=?,setbanat=? WHERE id=?");
			banoffst.setInt(1, BanType.NO_BAN);
			banoffst.setInt(2, 0);
			banoffst.setInt(3, userId);			
			if (banoffst.executeUpdate() > 0){
			}   			
		}catch (SQLException e) {
			_logger.error("UM9 " + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (banoffst != null) banoffst.close();
		    	_sqlconnection = null;
		    	banoffst = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void coloroff(int userId){
		Connection _sqlconnection = null;
		PreparedStatement coloroffst = null;		
				
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			coloroffst = _sqlconnection.prepareStatement("UPDATE user SET colortype=?,setcolorat=? WHERE id=?");
			coloroffst.setInt(1, ColorType.GRAY);
			coloroffst.setInt(2, 0);
			coloroffst.setInt(3, userId);
			if (coloroffst.executeUpdate() > 0){
			}   			
		}catch (SQLException e) {
			_logger.error("UM10 " + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (coloroffst != null) coloroffst.close();
		    	_sqlconnection = null;
		    	coloroffst = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void accessoryoff(int userId){
		Connection _sqlconnection = null;
		PreparedStatement accessoryoffst = null;		
				
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			accessoryoffst = _sqlconnection.prepareStatement("UPDATE user SET accessorytype=?,setaccessoryat=? WHERE id=?");
			accessoryoffst.setInt(1, AccessoryType.NOACCESSORY);
			accessoryoffst.setInt(2, 0);
			accessoryoffst.setInt(3, userId);
			if (accessoryoffst.executeUpdate() > 0){
			}   			
		}catch (SQLException e) {
			_logger.error("UM11 " + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (accessoryoffst != null) accessoryoffst.close();
		    	_sqlconnection = null;
		    	accessoryoffst = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void updateTopUsers(){		
		byte from = 1;
		byte to = 125;
		byte limit = 30;
		
		Connection _sqlconnection = null;
		PreparedStatement pstm = null;	
		ResultSet resExp = null;
		
		int fromvalue = Integer.MAX_VALUE;
		int tovalue = Integer.MAX_VALUE;
		if(levels.get(new Integer(from)) != null) fromvalue = levels.get(new Integer(from));
		if (levels.get(new Integer(to + 1)) != null) tovalue = levels.get(new Integer(to + 1)) - 1;
		
		topUsers.clear();
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			pstm = _sqlconnection.prepareStatement("SELECT * FROM user WHERE experience>=? AND experience<=? ORDER BY experience DESC LIMIT ?");
			pstm.setInt(1, fromvalue);
			pstm.setInt(2, tovalue);
			pstm.setInt(3, limit);
			resExp = pstm.executeQuery();
			while(resExp.next()){				
				UserForTop u = new UserForTop(resExp.getInt("id"), resExp.getString("title"), getLevelByExperience(resExp.getInt("experience")), resExp.getInt("exphour"), resExp.getInt("expday"), resExp.getInt("popular"), resExp.getString("url"));
				
				if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(u.id)) != null){
    				u.isonline = true;
    			}else{
    				u.isonline = false;
    			}
				
				topUsers.add(u);
    		}
		}catch (SQLException e) {
			_logger.error("UM12 " + e.toString());
		}
		finally
		{
		    try{		    	
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (pstm != null) pstm.close();
		    	if (resExp != null) resExp.close();
		    	_sqlconnection = null;
		    	pstm = null;
		    	resExp = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}   	
    }
	
	public void updateTopHourUsers(){	
		byte limit = 10;
		
		Connection _sqlconnection = null;
		PreparedStatement pstm = null;	
		ResultSet resExp = null;		
		
		topHourUsers.clear();
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			pstm = _sqlconnection.prepareStatement("SELECT * FROM user ORDER BY exphour DESC LIMIT ?");			
			pstm.setInt(1, limit);
			resExp = pstm.executeQuery();
			while(resExp.next()){				
				UserForTop u = new UserForTop(resExp.getInt("id"), resExp.getString("title"), getLevelByExperience(resExp.getInt("experience")), resExp.getInt("exphour"), resExp.getInt("expday"), resExp.getInt("popular"), resExp.getString("url"));
				
				if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(u.id)) != null){
    				u.isonline = true;
    			}else{
    				u.isonline = false;
    			}
				
				topHourUsers.add(u);
    		}
		}catch (SQLException e) {
			_logger.error("UM12 " + e.toString());
		}
		finally
		{
		    try{		    	
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (pstm != null) pstm.close();
		    	if (resExp != null) resExp.close();
		    	_sqlconnection = null;
		    	pstm = null;
		    	resExp = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
    }
	
	public void updateTopDayUsers(){	
		byte limit = 10;
		
		Connection _sqlconnection = null;
		PreparedStatement pstm = null;	
		ResultSet resExp = null;		
		
		topDayUsers.clear();
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			pstm = _sqlconnection.prepareStatement("SELECT * FROM user ORDER BY expday DESC LIMIT ?");			
			pstm.setInt(1, limit);
			resExp = pstm.executeQuery();
			while(resExp.next()){				
				UserForTop u = new UserForTop(resExp.getInt("id"), resExp.getString("title"), getLevelByExperience(resExp.getInt("experience")), resExp.getInt("exphour"), resExp.getInt("expday"), resExp.getInt("popular"), resExp.getString("url"));
				
				if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(u.id)) != null){
    				u.isonline = true;
    			}else{
    				u.isonline = false;
    			}
				
				topDayUsers.add(u);
    		}
		}catch (SQLException e) {
			_logger.error("UM12 " + e.toString());
		}
		finally
		{
		    try{		    	
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (pstm != null) pstm.close();
		    	if (resExp != null) resExp.close();
		    	_sqlconnection = null;
		    	pstm = null;
		    	resExp = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}   	
    }
	
	public void updatePopularTopUsers(){		
		byte limit = 30;
		
		Connection _sqlconnection = null;
		PreparedStatement pstm = null;	
		ResultSet resExp = null;		
		
		topPopularUsers.clear();
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			pstm = _sqlconnection.prepareStatement("SELECT * FROM user ORDER BY popular DESC LIMIT ?");			
			pstm.setInt(1, limit);
			resExp = pstm.executeQuery();				
			while(resExp.next()){				
				UserForTop u = new UserForTop(resExp.getInt("id"), resExp.getString("title"), getLevelByExperience(resExp.getInt("experience")), resExp.getInt("exphour"), resExp.getInt("expday"), resExp.getInt("popular"), resExp.getString("url"));
				
				if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(u.id)) != null){
    				u.isonline = true;
    			}else{
    				u.isonline = false;
    			}
				
				topPopularUsers.add(u);
    		}
		}catch (SQLException e) {
			_logger.error("UM13 " + e.toString());
		}
		finally
		{
		    try{		    	
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (pstm != null) pstm.close();
		    	if (resExp != null) resExp.close();
		    	_sqlconnection = null;
		    	pstm = null;
		    	resExp = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
    }
	
	public void addToFriend(int uid,int fid){
		Connection _sqlconnection = null;
		PreparedStatement findfriend = null;
		ResultSet findfriendRes = null;
		PreparedStatement insertfriend = null;		
		
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		findfriend = _sqlconnection.prepareStatement("SELECT * FROM friends WHERE uid=? AND fid=?");
    		findfriend.setInt(1, uid);
    		findfriend.setInt(2, fid);
    		findfriendRes = findfriend.executeQuery();    		
    		if (findfriendRes.next()){    			
    			return;		
    		}else{    			
    			insertfriend = _sqlconnection.prepareStatement("INSERT INTO friends (uid, fid) VALUES(?,?)");    			
    			insertfriend.setInt(1, uid);
    			insertfriend.setInt(2, fid); 			    		
    			insertfriend.executeUpdate();
    		}	
		} catch (SQLException e) {		
			_logger.error("UM14 " + e.toString());
		}	
		finally
		{
		    try{
		    	if (_sqlconnection != null)  _sqlconnection.close();
		    	if (findfriend != null)  findfriend.close(); 
		    	if (findfriendRes != null)  findfriendRes.close(); 
		    	if (insertfriend != null)  insertfriend.close();
		    	findfriend = null;
		    	findfriendRes = null;
		    	insertfriend = null;		    	
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void addToEnemy(int uid,int eid){
		Connection _sqlconnection = null;
		PreparedStatement findenemy = null;
		ResultSet findenemyRes = null;
		PreparedStatement insertenemy = null;		
		
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		findenemy = _sqlconnection.prepareStatement("SELECT * FROM enemies WHERE uid=? AND eid=?");
    		findenemy.setInt(1, uid);
    		findenemy.setInt(2, eid);
    		findenemyRes = findenemy.executeQuery();    		
    		if (findenemyRes.next()){    			
    			return;		
    		}else{
    			insertenemy = _sqlconnection.prepareStatement("INSERT INTO enemies (uid, eid) VALUES(?,?)");    			
    			insertenemy.setInt(1, uid);
    			insertenemy.setInt(2, eid); 			    		
    			insertenemy.executeUpdate();
    		}	
		} catch (SQLException e) {		
			_logger.error("UM40 " + e.toString());
		}	
		finally
		{
		    try{
		    	if (_sqlconnection != null)  _sqlconnection.close();
		    	if (findenemy != null)  findenemy.close(); 
		    	if (findenemyRes != null)  findenemyRes.close(); 
		    	if (insertenemy != null)  insertenemy.close();
		    	findenemy = null;
		    	findenemyRes = null;
		    	insertenemy = null;		    	
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void removeFriend(int uid,int fid){
		Connection _sqlconnection = null;
		PreparedStatement deletefriend = null;			
		
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		
    		deletefriend = _sqlconnection.prepareStatement("DELETE FROM friends WHERE uid=? AND fid=?");
    		deletefriend.setInt(1, uid);
    		deletefriend.setInt(2, fid);
    		deletefriend.executeUpdate();
		} catch (SQLException e) {		
			_logger.error("UM15 " + e.toString());
		}	
		finally
		{
		    try{
		    	if (_sqlconnection != null)  _sqlconnection.close();
		    	if (deletefriend != null)  deletefriend.close();
		    	deletefriend = null;
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public void removeEnemy(int uid,int eid){
		Connection _sqlconnection = null;
		PreparedStatement deleteenemy = null;			
		
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		
    		deleteenemy = _sqlconnection.prepareStatement("DELETE FROM enemies WHERE uid=? AND eid=?");
    		deleteenemy.setInt(1, uid);
    		deleteenemy.setInt(2, eid);
    		deleteenemy.executeUpdate();
		} catch (SQLException e) {		
			_logger.error("UM15 " + e.toString());
		}	
		finally
		{
		    try{
		    	if (_sqlconnection != null)  _sqlconnection.close();
		    	if (deleteenemy != null)  deleteenemy.close();
		    	deleteenemy = null;
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public ArrayList<UserFriend> getFiends(int uid){		
    	Connection _sqlconnection = null;
    	PreparedStatement find = null;
    	ResultSet findRes = null;
    	
    	ArrayList<UserFriend> friends = new ArrayList<UserFriend>();
    	
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();    		    		
    		find = _sqlconnection.prepareStatement("SELECT * FROM friends INNER JOIN user ON friends.fid=user.id WHERE friends.uid=?");
    		find.setInt(1, uid);
    		findRes = find.executeQuery();    		
    		while (findRes.next()){
    			UserFriend user = new UserFriend(findRes.getInt("user.id"), findRes.getString("user.title"), findRes.getInt("user.experience"), findRes.getInt("user.popular"), findRes.getString("user.url"));
    			
    			if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(user.id)) != null){
    				user.isonline = true;
    			}else{
    				user.isonline = false;
    			}
    			
    			friends.add(user);    			
    		}
		} catch (SQLException e) {			
			_logger.error("UM16 " + e.toString());
			
		} 
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (find != null) find.close(); 
		    	if (findRes != null) findRes.close();		    	
		    	_sqlconnection = null;
		    	find = null;
		    	findRes = null;		    	
		    }
		    catch (SQLException sqlx) {     
		    }
		}
    	return friends;
	}
	
	public ArrayList<UserFriend> getEnemies(int uid){		
    	Connection _sqlconnection = null;
    	PreparedStatement find = null;
    	ResultSet findRes = null;
    	
    	ArrayList<UserFriend> friends = new ArrayList<UserFriend>();
    	
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();    		    		
    		find = _sqlconnection.prepareStatement("SELECT * FROM enemies INNER JOIN user ON enemies.eid=user.id WHERE enemies.uid=?");
    		find.setInt(1, uid);
    		findRes = find.executeQuery();    		
    		while (findRes.next()){
    			UserFriend user = new UserFriend(findRes.getInt("user.id"), findRes.getString("user.title"), findRes.getInt("user.experience"), findRes.getInt("user.popular"), findRes.getString("user.url"));
    			
    			if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(user.id)) != null){
    				user.isonline = true;
    			}else{
    				user.isonline = false;
    			}
    			
    			friends.add(user);    			
    		}
		} catch (SQLException e) {			
			_logger.error("UM16 " + e.toString());
			
		} 
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (find != null) find.close(); 
		    	if (findRes != null) findRes.close();		    	
		    	_sqlconnection = null;
		    	find = null;
		    	findRes = null;		    	
		    }
		    catch (SQLException sqlx) {     
		    }
		}
    	return friends;
	}
	
	public UserConnection getOfflineUser(int userID){    	
    	Connection _sqlconnection = null;
    	PreparedStatement find = null;
    	ResultSet findRes = null;
    	
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();    		    		
    		find = _sqlconnection.prepareStatement("SELECT * FROM user INNER JOIN clan ON user.clanid=clan.id INNER JOIN pets ON user.petid=pets.id WHERE user.id=?");
    		find.setInt(1, userID);
    		findRes = find.executeQuery();    		
    		if (findRes.next()){
    			UserConnection user = null;    					
    			ClanUserInfo claninfo = new ClanUserInfo(findRes.getInt("user.clanid"), findRes.getString("clan.title"), findRes.getInt("user.clandepositm"), findRes.getInt("user.clandeposite"), findRes.getByte("user.clanrole"), findRes.getInt("user.getclanmoneyat"));
    			PetInfo pet = new PetInfo(findRes.getInt("pets.id"), findRes.getInt("pets.level"), findRes.getInt("pets.experience"), findRes.getInt("pets.energy"), findRes.getInt("pets.changeenergyat"));
    			
    			user = new UserConnection(findRes.getInt("user.id"), findRes.getString("user.idsocial"), findRes.getString("user.ip"), findRes.getInt("user.sex"), 
    										findRes.getString("user.title"), findRes.getInt("user.popular"), findRes.getInt("user.experience"), 
    										findRes.getInt("user.exphour"), findRes.getInt("user.expday"), findRes.getInt("user.lastlvl"),
    										findRes.getInt("user.money"), findRes.getByte("user.role"), (byte)findRes.getInt("user.bantype"),
    										findRes.getInt("user.setbanat"), findRes.getInt("user.changebanat"), findRes.getByte("user.colortype"), findRes.getInt("user.setcolorat"),
    										findRes.getByte("user.accessorytype"), findRes.getInt("user.setaccessoryat"), findRes.getString("user.url"), claninfo, pet, null);
    			claninfo = null;
    			pet = null;
    			return user;
    		}
		} catch (SQLException e) {			
			_logger.error("UM17 " + e.toString());
		} 
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (find != null) find.close(); 
		    	if (findRes != null) findRes.close();		    	
		    	_sqlconnection = null;
		    	find = null;
		    	findRes = null;		    	
		    }
		    catch (SQLException sqlx) {     
		    }
		}
		return null;
    }
	
	public BuyResult sendMail(UserConnection fromUser, int toID, String msg){
		BuyResult buyresult = new BuyResult();
		buyresult.error = BuyErrorCode.OTHER;
		
		Connection _sqlconnection = null;		
		PreparedStatement insert = null;
		
		if(fromUser != null){		
			if(fromUser.user.money > Config.sendMailPrice()){		
		    	try {		    		
		    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
		    		insert = _sqlconnection.prepareStatement("INSERT INTO messages (fromid, toid, msg) VALUES(?,?,?)");
		    		insert.setInt(1, fromUser.user.id);
		    		insert.setInt(2, toID);
		    		insert.setString(3, msg);
		    		insert.executeUpdate();
					
					buyresult.error = BuyErrorCode.OK;
					
					fromUser.user.updateMoney(fromUser.user.money - Config.sendMailPrice());
					ServerMouseApplication.application.commonroom.changeUserInfoByID(fromUser.user.id, ChangeInfoParams.USER_MONEY, fromUser.user.money, 0);
				} catch (SQLException e) {		
					_logger.error("UM18 " + e.toString());
				}	
				finally
				{
				    try{
				    	if (_sqlconnection != null)  _sqlconnection.close();		    	
				    	if (insert != null)  insert.close();		    	
				    	insert = null;		    	
				    	_sqlconnection = null;
				    }
				    catch (SQLException sqlx) {		     
				    }
				}
			}else{
				buyresult.error = BuyErrorCode.NOT_ENOUGH_MONEY;
			}
		}
		return buyresult;
	}
	
	public ArrayList<UserMailMessage> getMailMessages(int uid){		
    	Connection _sqlconnection = null;
    	PreparedStatement find = null;
    	ResultSet findRes = null;
    	
    	ArrayList<UserMailMessage> friends = new ArrayList<UserMailMessage>();
    	
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();    		    		
    		find = _sqlconnection.prepareStatement("SELECT * FROM messages INNER JOIN user ON messages.fromid=user.id WHERE messages.toid=? ORDER BY messages.id DESC");
    		find.setInt(1, uid);
    		findRes = find.executeQuery();    		
    		while (findRes.next()){
    			UserMailMessage user = new UserMailMessage(findRes.getInt("user.id"), findRes.getString("user.title"), findRes.getInt("user.experience"), findRes.getInt("user.popular"), findRes.getString("user.url"));
    			
    			if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(user.id)) != null){
    				user.isonline = true;
    			}else{
    				user.isonline = false;
    			}
    			user.message = findRes.getString("messages.msg");
    			user.messageid = findRes.getInt("messages.id");
    			
    			friends.add(user);
    		}
		} catch (SQLException e) {			
			_logger.error("UM19 " + e.toString());
			
		} 
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (find != null) find.close(); 
		    	if (findRes != null) findRes.close();		    	
		    	_sqlconnection = null;
		    	find = null;
		    	findRes = null;		    	
		    }
		    catch (SQLException sqlx) {     
		    }
		}
    	return friends;
	}
	
	public void removeMailMessage(int mid){
		Connection _sqlconnection = null;
		PreparedStatement delete = null;			
		
    	try {
    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
    		
    		delete = _sqlconnection.prepareStatement("DELETE FROM messages WHERE id=?");
    		delete.setInt(1, mid);
    		delete.executeUpdate();
		} catch (SQLException e) {		
			_logger.error("UM20 " + e.toString());
		}	
		finally
		{
		    try{
		    	if (_sqlconnection != null)  _sqlconnection.close();
		    	if (delete != null)  delete.close();
		    	delete = null;
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
	}
	
	public int getFriendsBonus(UserConnection user, String vid, int mode, String appID, String sessionKey){
		if(user != null){
			if (mode == GameMode.DEBUG){			
		    	return 6;
	    	}else if (mode == GameMode.VK){
	    		return 0;
	    	}else if (mode == GameMode.MM){
	    		int countfriends = 0;
	    		
	    		 MessageDigest md5;
	    		 String sigstr = "app_id=" + appID + "format=xmlmethod=friends.getAppUserssecure=1session_key=" + sessionKey + Config.protectedSecretMM();
	    		 String sig = null;
				 try {
					 md5 = MessageDigest.getInstance("MD5");
					 md5.reset();
					 md5.update(sigstr.getBytes());
					 byte[] bytecode = md5.digest();	 
					 StringBuffer hexString = new StringBuffer();
					 for (int i = 0; i < bytecode.length; i++) {
						 String hex = Integer.toHexString(0xff & bytecode[i]);
			   	     	 if(hex.length() == 1) hexString.append('0');
			   	      	 hexString.append(hex);
		             }
					 
					 sig = hexString.toString();
					 
					 bytecode = null;
					 hexString = null;
				 } catch (NoSuchAlgorithmException e) {
					 ServerMouseApplication.application.logger.log("Error getting MD5 object" + e);
				 }
				 md5 = null;			 
				
				String urlstr = "http://www.appsmail.ru/platform/api?method=friends.getAppUsers&app_id=" +
				appID + "&format=xml" +"&session_key=" + sessionKey + "&secure=1" + "&sig=" + sig;
				
				try{			 
					URL url = new URL(urlstr);
					
					HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);
					urlConnection.setUseCaches(false);
					urlConnection.setRequestMethod("GET");
							         
					InputStream is = urlConnection.getInputStream();
					BufferedReader rd = new BufferedReader(new InputStreamReader(is));
					String line;
					StringBuffer response = new StringBuffer(); 
					while((line = rd.readLine()) != null) {
						response.append(line);
						response.append('\r');
					}
					rd.close();
					String answer = response.toString();			
					
					Document document = null;
			        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			        factory.setValidating(false);
			        factory.setNamespaceAware(true);       
			        try
			        { 
						if(answer.indexOf("user") == -1) {
							 ServerMouseApplication.application.logger.log("BAD ANSWER BONUS: " + answer);
							 answer = "bad answer";
						}
			        	
			        	InputStream istr = new ByteArrayInputStream(answer.getBytes());
						DocumentBuilder builder = factory.newDocumentBuilder();
						document = builder.parse(istr);
						document.getDocumentElement().normalize();
						NodeList nodelist = document.getDocumentElement().getElementsByTagName("user");
						
						countfriends = Math.min(5, nodelist.getLength());				
						
			            istr.close();
			            istr = null;
			            builder = null;
			            nodelist = null;		          
			        }
			        catch (Exception e){
			        	ServerMouseApplication.application.logger.log("UM21 " + e.toString());        	
			        }
			         
			        factory = null;
			        document = null;
				
			        is.close();
			        is = null;
			        urlConnection = null;
				}catch(IOException e){
					ServerMouseApplication.application.logger.log("UM22 " + e.toString());
				}
				
				int friendsdelta = 0;
				
				Connection _sqlconnection = null;
				PreparedStatement find = null;
				ResultSet findRes = null;	
				PreparedStatement update = null;
			
				try {
					_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
					find = _sqlconnection.prepareStatement("SELECT * FROM user WHERE id=?");
					find.setInt(1, user.user.id);
					findRes = find.executeQuery();    		
					if(findRes.next()){
						int countfriendsDB = 0;
						countfriendsDB = findRes.getInt("countfriends");
						friendsdelta = Math.max(0, (countfriends - countfriendsDB));
						
						if(friendsdelta > 0){
							ServerMouseApplication.application.logger.log("User id = " + user.user.id + " invite " + friendsdelta + " new friends. Bonus: " + (friendsdelta * Config.friendBonus()));
							
							user.user.updateMoney(user.user.money + friendsdelta * Config.friendBonus());
		    				ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
						}
		    		}
				} catch (SQLException e) {				
					ServerMouseApplication.application.logger.error("UM23 " + e.toString());
				}
				finally
				{
				    try{
				    	if (find != null) find.close();
				    	if (findRes != null) findRes.close();		    	
				    	if (update != null) update.close();
				    	if (_sqlconnection != null) _sqlconnection.close();
				    	find = null;
				    	findRes = null;
				    	update = null;
				    	_sqlconnection = null;
				    }
				    catch (SQLException sqlx) {		     
				    }
				}
				
				return friendsdelta;
	    	}
		}
		return 0;
	}
	
	public void setBonusNewLevel(int userID){
		UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(userID));
		if(user != null){
			int prize = user.user.level * user.user.level * 8;
			user.user.updateMoney(user.user.money + prize);
			
			MessageNewLevel message = new MessageNewLevel(MessageType.NEW_LEVEL, 0, user.user.level, prize);
			
			try{
				IConnection connection = user.conection;
				if (connection instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) connection).invoke("processMassage", new Object[]{message});
		    	}
				connection = null;
			}catch(Throwable e){
				ServerMouseApplication.application.logger.log("UM24 " + e.toString());
			}
			message = null;
		}
	}
	
	public int allwin = 0;
	public int allbets = 0;
	public int allcounts = 0;
	public CheckLuckResult checkLuck(UserConnection user, int bet){
		CheckLuckResult result = new CheckLuckResult();
		if(user != null){
			if(bet >= 5){
				if(user.user.money >= bet){					
					int v =(int) Math.floor((float) Math.random() * 14) - 3;
					if (v < 0){
						v =(int) Math.floor((float) Math.random() * 4);
					}
					float k = (float) 1 / 5;
					int win = (int) Math.floor((float) k * v * bet);
					user.user.updateMoney(user.user.money - bet + win);
					result.result = v;
					result.addmoney = win;
					result.usermoney = user.user.money;
					result.error = BuyErrorCode.OK;
					
					allcounts++;
					allbets = allbets + bet;
					allwin = allwin - bet + win;
					
					if(win > 1000){
						ServerMouseApplication.application.commonroom.sendSystemMessage("Поздравляем " + user.user.title + "! Выигрыш на колеcе фортуны: " + win + " евро.");
					}
					
					//ServerMouseApplication.application.logger.log("allcounts:allbets:allwin--------> " + allcounts + " : " + allbets + " : " + allwin);
				}else{
					result.error = BuyErrorCode.NOT_ENOUGH_MONEY;
				}
			}else{
				result.error = BuyErrorCode.MIN_BET;
			}
		}else{
			result.error = BuyErrorCode.OTHER;
		}
		return result;
	}
	
	public int getLevelByExperience(int exp){
		for(int i = 1; i <= levels.size(); i++){
			if (levels.get(new Integer(i)) >  exp){
				return (i - 1);
			}
		}
		return 1;
	}
	
	public void updateUser(User user){
		if(user != null){			
			Connection _sqlconnection = null;				
			PreparedStatement uppdate = null;
			PreparedStatement uppdatePet = null;
			
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				
				uppdate = _sqlconnection.prepareStatement("UPDATE user SET ip=?,experience=?,exphour=?,expday=?,money=?,clandepositm=?,clandeposite=?,getclanmoneyat=?,changebanat=?,popular=? WHERE id=?");
				uppdate.setString(1, user.ip);
				uppdate.setInt(2, user.experience);
				uppdate.setInt(3, user.exphour);
				uppdate.setInt(4, user.expday);
				uppdate.setInt(5, user.money);
				uppdate.setInt(6, user.claninfo.clandepositm);
				uppdate.setInt(7, user.claninfo.clandeposite);
				uppdate.setInt(8, user.claninfo.getclanmoneyat);
				uppdate.setInt(9, user.changebanat);
				uppdate.setInt(10, user.popular);
				uppdate.setInt(11, user.id);
				
				uppdate.executeUpdate();
				
				uppdatePet = _sqlconnection.prepareStatement("UPDATE pets SET experience=?,energy=? WHERE id=?");
				uppdatePet.setInt(1, user.pet.experience);
				uppdatePet.setInt(2, user.pet.energy);
				uppdatePet.setInt(3, user.id);
				
				uppdatePet.executeUpdate();
			} catch (SQLException e) {
				ServerMouseApplication.application.logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close();		    	
			    	if (uppdate != null) uppdate.close(); 
			    	if (uppdatePet != null) uppdatePet.close(); 
			    	
			    	_sqlconnection = null;
			    	uppdate = null;
			    	uppdatePet = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			}
		}else{
			ServerMouseApplication.application.logger.error("Null user Update");
		}
	}
	
	//зарплата	
	public int getOnlineTimeMoneyInfo(UserConnection initiator){
		if(initiator != null){
			Date date = new Date();
			int currenttime = (int)(date.getTime() / 1000);
			int starttime = ServerMouseApplication.application.commonroom.userstimeonline.get(initiator.user.id);
			if(starttime > 0){
				int seconds = (currenttime - starttime);
				int minute = (int) Math.floor((float)seconds / 60);
				int money = (int) Math.floor((float) minute * initiator.user.level / 15);
				if(initiator.user.role == UserRole.ADMINISTRATOR || initiator.user.role == UserRole.MODERATOR){
					money = money * 3;
				}
				return money;
			}
		}
		return 0;
	}
	
	public void getOnlineTimeMoney(UserConnection initiator){		
		int money = getOnlineTimeMoneyInfo(initiator);
		if(money > 0){
			Date date = new Date();
			int currenttime = (int)(date.getTime() / 1000);
			ServerMouseApplication.application.commonroom.userstimeonline.remove(initiator.user.id);
			ServerMouseApplication.application.commonroom.userstimeonline.put(initiator.user.id, currenttime);
			
			initiator.user.updateMoney(initiator.user.money + money);
			initiator.user.popular = Math.max(0, initiator.user.popular - (int)Math.ceil((float) money / 5));
			ServerMouseApplication.application.commonroom.changeUserInfoByID(initiator.user.id, ChangeInfoParams.USER_MONEY_POPULAR, initiator.user.money, initiator.user.popular, 0);
		}
	}
}
