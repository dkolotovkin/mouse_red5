package mouseapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import mouseapp.admin.AdministratorManager;
import mouseapp.clan.ClanManager;
import mouseapp.db.DataBaseConfig;
import mouseapp.game.GameManager;
import mouseapp.logger.MyLogger;
import mouseapp.pet.PetManager;
import mouseapp.room.Room;
import mouseapp.room.auction.AuctionRoom;
import mouseapp.room.common.CommonRoom;
import mouseapp.room.victorina.VictorinaRoom;
import mouseapp.shop.ShopManager;
import mouseapp.shop.buyresult.BuyResult;
import mouseapp.shop.checkluckresult.CheckLuckResult;
import mouseapp.startbonus.StartBonusManager;
import mouseapp.user.UserConnection;
import mouseapp.user.UserForInit;
import mouseapp.user.UserForTop;
import mouseapp.user.UserFriend;
import mouseapp.user.UserMailMessage;
import mouseapp.user.chage.ChangeResult;
import mouseapp.user.info.UserInfoManager;
import mouseapp.utils.authorization.Authorization;
import mouseapp.utils.jsonutil.JSONUtil;
import mouseapp.utils.md5.MD5;
import mouseapp.utils.random.RoomRandom;

import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.Red5;

import snaq.db.ConnectionPool;

public class ServerMouseApplication extends ApplicationAdapter{	
	public static ServerMouseApplication application;
	public MyLogger logger = new MyLogger(ServerMouseApplication.class.getName());
	
	public HashMap<String, Room> rooms;
	public CommonRoom commonroom;
	public Room modsroom;
	public AuctionRoom auctionroom;
	public VictorinaRoom victorinaroom;
	public UserInfoManager userinfomanager;
	public GameManager gamemanager;
	public ShopManager shopmanager;
	public ClanManager clanmanager;
	public PetManager petmanager;
	public StartBonusManager startbonus;
	public AdministratorManager adminmanager;
	
	public ConnectionPool sqlpool;
	
	@Override
	public boolean appStart(IScope app)
    {
		logger.log("Start Application...");
		application = this;
		gamemanager = new GameManager();
		clanmanager = new ClanManager();
		
		rooms = new HashMap<String, Room>();		
		commonroom = new CommonRoom(1, "Œ·˘ËÈ ˜‡Ú");
		rooms.put(Integer.toString(commonroom.id), commonroom);
		
		modsroom = new Room(RoomRandom.getRoomID(), "ÃÓ‰Â‡ÚÓ˚");
		rooms.put(Integer.toString(modsroom.id), modsroom);
		
		auctionroom = new AuctionRoom(RoomRandom.getRoomID(), "¿ÛÍˆËÓÌ");
		rooms.put(Integer.toString(auctionroom.id), auctionroom);	
		
		File f = new File("main.txt");
		if (!f.exists()){
			Config.SERVERTYPE = Config.HELPSERVER;
		}else{
			Config.SERVERTYPE = Config.MAINSERVER;
		}
		
		try {
    		Class c = Class.forName("com.mysql.jdbc.Driver");
    		Driver driver = (Driver)c.newInstance();
    		DriverManager.registerDriver(driver);
    		logger.log("Loading JDBC Driver ok");
		} catch (Exception e) {
			logger.error(e.toString());
			return false;
		}
		sqlpool = new ConnectionPool(DataBaseConfig.dbname(), 50, 2000, 2000, 200000, DataBaseConfig.connectionUrl(), DataBaseConfig.dblogin(), DataBaseConfig.dbpassward());
		
		victorinaroom = new VictorinaRoom(RoomRandom.getRoomID(), "¬ËÍÚÓËÌ‡");
		rooms.put(Integer.toString(victorinaroom.id), victorinaroom);
		
		userinfomanager = new UserInfoManager();
		petmanager = new PetManager();
		shopmanager = new ShopManager();
		adminmanager = new AdministratorManager();
		startbonus = new StartBonusManager();
		gamemanager.updateJackpot(0, 0, 0);
        return true;
    }	
	
	@Override
    public void appStop(IScope app){		
		logger.log("Stop application. Send Closed Message to client");		
		
		Set<Entry<String, Room>> set = rooms.entrySet();
		for (Map.Entry<String, Room> room:set){
			room.getValue().roomClear();		
		}
		rooms.clear();
    }
    
    @Override
    public boolean appConnect( IConnection conn , Object[] params){    	
    	return true;
    }
    @Override
    public boolean appJoin(IClient client, IScope scope){	
    	return true;
    }
    @Override
    public boolean roomConnect(IConnection conn, Object[] params){    	
    	return true;
    }
    @Override
    public boolean roomJoin(IClient client, IScope scope){    	
    	return true;
    }
  
    public boolean logIn(String socialid, String passward, String authkey, String vid, int mode, String appID, int version){
    	IConnection conn = Red5.getConnectionLocal();    	
    	
    	try{
    		if(version == Config.currentVersion()){
		    	if (Authorization.check(authkey, vid, mode, appID) == true && conn != null){	
		    		UserConnection user = commonroom.getUserBySocialID(socialid);
		    		if(user != null){
			    		userinfomanager.removeUserByConnID(user.conection.getClient().getId());
			    		userinfomanager.updateUser(user.user);
			    		if(user.conection != null){
			    			user.conection.close();
			    		}
			    		user = null;
		    		}
		    		
		    		if(userinfomanager.clientConnect(conn, socialid, passward, Red5.getConnectionLocal().getRemoteAddress())){    			
		    			return true;
		    		}else{
		    			logger.log("Client not be connect: " + socialid);
		    		}
		    	}else{
		    		logger.log("Authorization bad: " + socialid);
		    	}
    		}
    	}catch(Throwable e){
    		logger.log("START ERROR: " + e.toString() + " ||| " + e.getMessage());
    	}
    	conn.close();
    	conn = null;
    	return false;
    }
    
    public boolean loginSiteVK(String code, int siteAppId, int version){
    	IConnection conn = Red5.getConnectionLocal();    
    	
    	try
		{
    		if(version == Config.currentVersion()){
				URL url = new URL(Config.loginUrlVK());
				DataOutputStream wr = null;			
				try{			 
					String urlparams = "client_id=" + siteAppId + "&client_secret=" + Config.protectedSecretSiteVK() + "&code=" + code;
			        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			        urlConnection.setConnectTimeout(1000);
			        urlConnection.setReadTimeout(1000);
			         
			        urlConnection.setDoInput(true);
			        urlConnection.setDoOutput(true);
			        urlConnection.setUseCaches(false);
			        urlConnection.setRequestMethod("GET");		         
			         
			        wr = new DataOutputStream (urlConnection.getOutputStream());
			        wr.writeBytes(urlparams);
			        wr.flush();
			        wr.close();
			         
			        InputStream is = urlConnection.getInputStream();			        
			        BufferedReader rd = new BufferedReader(new InputStreamReader(is));			         
			        String line;
			        StringBuffer response = new StringBuffer();
			        int counter = 0;
			        while((line = rd.readLine()) != null && counter < 1000) {
			        	response.append(line);
						response.append('\r');
						counter++;				
			        }
			         
			        rd.close();
			        rd = null;
			        is.close();
			        is = null;
			        wr = null;
			        url = null;
			        urlConnection.disconnect();
			        urlConnection = null;
			        
			        String userId = JSONUtil.getValueByName(response.toString(), "user_id");			        
			        if(userId != null){
			        	String socialid = "vk" + userId;
			       	        
				        UserConnection user = commonroom.getUserBySocialID(socialid);			        
			    		if(user != null){
				    		userinfomanager.removeUserByConnID(user.conection.getClient().getId());
				    		userinfomanager.updateUser(user.user);
				    		if(user.conection != null){
				    			user.conection.close();
				    		}
				    		user = null;
			    		}
			    		
			    		if(userinfomanager.clientConnect(conn, socialid, null, Red5.getConnectionLocal().getRemoteAddress())){    			
			    			return true;
			    		}else{
			    			logger.log("Client not be connect: " + socialid);
			    		}
			        }else{
			        	return false;
			        }
				 }				
				 catch(IOException e){
					 logger.log("SM2" + e.toString());				
				 }
				 finally
					{
					    try{
					    	if (wr != null) wr.close();				    	
					    	wr = null;			    	
					    }
					    catch (Throwable e) {
					    	logger.log("SM3" + e.getMessage());				    	
					    }
					}
    		}
		}catch(MalformedURLException e){
			logger.log("SM4" + e.toString());			
		}
		conn.close();
    	conn = null;
    	return false;
    }
    
    public boolean loginSiteOD(String code, int siteAppId, int version){
    	IConnection conn = Red5.getConnectionLocal();
    	
    	try
		{
    		if(version == Config.currentVersion()){
				URL url = new URL(Config.loginUrlOD());
				DataOutputStream wr = null;			
				try{			 
					String urlparams = "client_id=" + siteAppId + "&client_secret=" + Config.protectedSecretSiteOD() + 
										"&grant_type=" + "authorization_code" + "&code=" + code +
										"&redirect_uri=" + Config.oficalSiteUrl();
			        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			        urlConnection.setConnectTimeout(1000);
			        urlConnection.setReadTimeout(1000);
			         
			        urlConnection.setDoInput(true);
			        urlConnection.setDoOutput(true);
			        urlConnection.setUseCaches(false);
			        urlConnection.setRequestMethod("POST");		         
			         
			        wr = new DataOutputStream (urlConnection.getOutputStream());
			        wr.writeBytes(urlparams);
			        wr.flush();
			        wr.close();
			         
			        InputStream is = urlConnection.getInputStream();			        
			        BufferedReader rd = new BufferedReader(new InputStreamReader(is));			         
			        String line;
			        StringBuffer response = new StringBuffer();
			        int counter = 0;
			        while((line = rd.readLine()) != null && counter < 1000) {
			        	response.append(line);
						response.append('\r');
						counter++;				
			        }
			         
			        rd.close();
			        rd = null;
			        is.close();
			        is = null;
			        wr = null;
			        url = null;
			        urlConnection.disconnect();
			        urlConnection = null;
			        
			        String access_token = JSONUtil.getValueByName(response.toString(), "access_token");
			        access_token = access_token.substring(1, access_token.length() - 1);			        
			        
			        url = new URL("http://api.odnoklassniki.ru/api/users/getLoggedInUser");
			        String appkey = "application_key=" + Config.applicationKeySiteOD();
			        String access_token_param = "access_token=" + access_token;			       
			        String and = "&";
			        String sig = "sig=" + MD5.getMD5(new String(appkey) + MD5.getMD5(access_token + Config.protectedSecretSiteOD()));
			        urlparams = appkey + and + sig + and + access_token_param;
			        
					urlConnection = (HttpURLConnection)url.openConnection();
					urlConnection.setConnectTimeout(1000);
					urlConnection.setReadTimeout(1000);
					 
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);
					urlConnection.setUseCaches(false);
					urlConnection.setRequestMethod("GET");		         
					 
					wr = new DataOutputStream (urlConnection.getOutputStream());
					wr.writeBytes(urlparams);
					wr.flush();
					wr.close();
			        
					is = urlConnection.getInputStream();			        
			        rd = new BufferedReader(new InputStreamReader(is));
			        response = new StringBuffer();
			        counter = 0;
			        while((line = rd.readLine()) != null && counter < 1000) {
			        	response.append(line);
						response.append('\r');
						counter++;				
			        }
			         
			        rd.close();
			        rd = null;
			        is.close();
			        is = null;
			        wr = null;
			        url = null;
			        urlConnection.disconnect();
			        urlConnection = null;
			        
			        String userId = response.toString();			       
			        userId = userId.substring(1, userId.length() - 2);
			       
			        if(userId != null){
			        	String socialid = "od" + userId;
			       	        
				        UserConnection user = commonroom.getUserBySocialID(socialid);			        
			    		if(user != null){
				    		userinfomanager.removeUserByConnID(user.conection.getClient().getId());
				    		userinfomanager.updateUser(user.user);
				    		if(user.conection != null){
				    			user.conection.close();
				    		}
				    		user = null;
			    		}
			    		
			    		if(userinfomanager.clientConnect(conn, socialid, null, Red5.getConnectionLocal().getRemoteAddress())){    			
			    			return true;
			    		}else{
			    			logger.log("Client not be connect: " + socialid);
			    		}
			        }else{
			        	return false;
			        }
				 }				
				 catch(IOException e){
					 logger.log("SM2" + e.toString());				
				 }
				 finally
					{
					    try{
					    	if (wr != null) wr.close();				    	
					    	wr = null;			    	
					    }
					    catch (Throwable e) {
					    	logger.log("SM3" + e.getMessage());				    	
					    }
					}
    		}
		}catch(MalformedURLException e){
			logger.log("SM4" + e.toString());			
		}
		conn.close();
    	conn = null;
    	return false;
    }
    
    public boolean loginSiteMM(String code, int siteAppId, int version){
    	IConnection conn = Red5.getConnectionLocal();    
    	
    	try
		{
    		if(version == Config.currentVersion()){
				URL url = new URL(Config.loginUrlMM());
				DataOutputStream wr = null;			
				try{				 
					String urlparams = "client_id=" + siteAppId + "&client_secret=" + Config.protectedSecretSiteMM() + 
										"&grant_type=" + "authorization_code" + "&code=" + code +
										"&redirect_uri=" + Config.oficalSiteUrl();
			        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			        urlConnection.setConnectTimeout(1000);
			        urlConnection.setReadTimeout(1000);
			         
			        urlConnection.setDoInput(true);
			        urlConnection.setDoOutput(true);
			        urlConnection.setUseCaches(false);
			        urlConnection.setRequestMethod("POST");		         
			         
			        wr = new DataOutputStream (urlConnection.getOutputStream());
			        wr.writeBytes(urlparams);
			        wr.flush();
			        wr.close();
			         
			        InputStream is = urlConnection.getInputStream();			        
			        BufferedReader rd = new BufferedReader(new InputStreamReader(is));			         
			        String line;
			        StringBuffer response = new StringBuffer();
			        int counter = 0;
			        while((line = rd.readLine()) != null && counter < 1000) {
			        	response.append(line);
						response.append('\r');
						counter++;				
			        }
			         
			        rd.close();
			        rd = null;
			        is.close();
			        is = null;
			        wr = null;
			        url = null;
			        urlConnection.disconnect();
			        urlConnection = null;			       
			        
			        String userId = JSONUtil.getValueByName(response.toString(), "x_mailru_vid");			        
			        userId = userId.substring(1, userId.length() - 1);
			        
			        if(userId != null){
			        	String socialid = "mm" + userId;
			       	        
				        UserConnection user = commonroom.getUserBySocialID(socialid);			        
			    		if(user != null){
				    		userinfomanager.removeUserByConnID(user.conection.getClient().getId());
				    		userinfomanager.updateUser(user.user);
				    		if(user.conection != null){
				    			user.conection.close();
				    		}
				    		user = null;
			    		}
			    		
			    		if(userinfomanager.clientConnect(conn, socialid, null, Red5.getConnectionLocal().getRemoteAddress())){    			
			    			return true;
			    		}else{
			    			logger.log("Client not be connect: " + socialid);
			    		}
			        }else{
			        	return false;
			        }
				 }				
				 catch(IOException e){
					 logger.log("SM2" + e.toString());				
				 }
				 finally
					{
					    try{
					    	if (wr != null) wr.close();				    	
					    	wr = null;			    	
					    }
					    catch (Throwable e) {
					    	logger.log("SM3" + e.getMessage());				    	
					    }
					}
    		}
		}catch(MalformedURLException e){
			logger.log("SM4" + e.toString());			
		}
		conn.close();
    	conn = null;
    	return false;
    }
    
    @Override
    public void appDisconnect(IConnection conn){    	
    	if(conn != null){
	    	UserConnection user = commonroom.getUserByConnectionID(conn.getClient().getId());
	    	
	    	userinfomanager.removeUserByConnID(conn.getClient().getId());
	    	if(user != null) userinfomanager.updateUser(user.user);
	    	user = null;
    	}
    }
    
    public int getOnlineUsersCount(){
    	return commonroom.users.size();
    }
    
    public int getFriendsBonus(String vid, int mode, String appID, String sessionKey){
    	return userinfomanager.getFriendsBonus(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()), vid, mode, appID, sessionKey);
    }
    
    public void updateUser(){
    	UserConnection user = commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    	if(user != null) userinfomanager.updateUser(user.user);
    	user = null;
    }
    
    public void updateParams(String url){
    	UserConnection user = commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    	userinfomanager.updateParams(user, url);
    }
  
    
    //Œ“œ–¿¬ ¿ —ŒŒ¡Ÿ≈Õ»… ¬ ◊¿“
    public void sendMessage(String mtext, String receiverID, String roomID){
    	IClient client = Red5.getConnectionLocal().getClient(); 	
    	Room room = rooms.get(roomID);
    	if (room != null){    		
    		room.sendMessage(mtext, client.getId(), receiverID);
    	}else{    		
    		Room gameroom = GameManager.gamerooms.get(roomID);
    		if (gameroom != null){    			
    			gameroom.sendMessage(mtext, client.getId(), receiverID);
    		}else{    			
    			commonroom.sendPrivateMessage(mtext, client.getId(), receiverID, new Integer(roomID));
    		}
    		gameroom = null;
    	}
    	room = null;
    	client = null;
    }
    
    public BuyResult sendMail(int uid, String message){	
    	return userinfomanager.sendMail(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()), uid, message);
    }
    
    public ArrayList<UserMailMessage> getMailMessages(){
    	return userinfomanager.getMailMessages(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()).user.id);
    }
    
    public void removeMailMessage(int mid){
    	userinfomanager.removeMailMessage(mid);
    }
    
    public void isBadPlayer(){
    	UserConnection u = commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    	if(u != null){
    		logger.log("BAD PLAYER->  id:" + u.user.id + " title:" + u.user.title);
    		u.conection.close();
    	}
    }
    
    public CheckLuckResult checkLuck(int bet){
    	UserConnection u = commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    	return userinfomanager.checkLuck(u, bet);
    }
    
    //»«Ã≈Õ≈Õ»≈ œ¿–¿Ã≈“–Œ¬ œ≈–—ŒÕ¿∆¿
    public ChangeResult changeInfo(String title, int sex){
    	ChangeResult result = new ChangeResult();
    	result = userinfomanager.changeInfo(title, sex, Red5.getConnectionLocal().getClient().getId(), result, false);
    	return result;
    }
    
    public void addToFriend(int uid){	
    	userinfomanager.addToFriend(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()).user.id, uid);
    }
    
    public void addToEnemy(int uid){
    	userinfomanager.addToEnemy(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()).user.id, uid);
    }
    
    public void removeFriend(int uid){
    	userinfomanager.removeFriend(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()).user.id, uid);
    }
    
    public void removeEnemy(int uid){
    	userinfomanager.removeEnemy(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()).user.id, uid);
    }
    
    public ArrayList<UserFriend> getFiends(){	
    	return userinfomanager.getFiends(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()).user.id);
    }
    
    public ArrayList<UserFriend> getEnemies(){
    	return userinfomanager.getEnemies(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()).user.id);
    }
    
    public ArrayList<UserForTop> gettopusers(){
    	return userinfomanager.topUsers;
    }
    
    public ArrayList<UserForTop> gettophourusers(){
    	return userinfomanager.topHourUsers;
    }
    
    public ArrayList<UserForTop> gettopdayusers(){
    	return userinfomanager.topDayUsers;
    }
    
    public ArrayList<UserForTop> gettoppopularusers(){
    	return userinfomanager.topPopularUsers;
    }
    
    public byte ban(int userID, byte type, boolean byip){
    	return userinfomanager.ban(userID, Red5.getConnectionLocal().getClient().getId(), type, byip);
    }
    
    public UserForInit getUserInfoByID(int userID){
    	UserConnection user = commonroom.users.get(Integer.toString(userID));
    	if (user != null){
    		Date date = new Date();
			int currenttime = (int)(date.getTime() / 1000);	
			date = null;
			
    		UserForInit ufi = UserForInit.createfromUser(user.user);
    		ufi.colortime = ServerMouseApplication.application.commonroom.updateColor(user, currenttime);
			ufi.accessorytime = ServerMouseApplication.application.commonroom.updateAccessories(user, currenttime);
			
    		return ufi;
    	}else{
    		return UserForInit.createfromUser(userinfomanager.getOfflineUser(userID).user); 
    	}
    }
    
    public void userInAuction(){
    	UserConnection u = commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    	auctionroom.addUser(u);
    	u = null;
    }    
    public void userOutAuction(){
    	auctionroom.removeUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    }    
    public int auctionBet(int b){
    	return auctionroom.bet(commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId()), b);
    }
    
    public void userInVictorina(){
    	UserConnection u = commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    	victorinaroom.addUser(u);
    	u = null;
    }
    public void userOutVictorina(){
    	victorinaroom.removeUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    }
    
    public int getOnlineTimeMoneyInfo(){
    	UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		return userinfomanager.getOnlineTimeMoneyInfo(initiator);
	}
    public void getOnlineTimeMoney(){	
    	UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    	userinfomanager.getOnlineTimeMoney(initiator);
    }
}