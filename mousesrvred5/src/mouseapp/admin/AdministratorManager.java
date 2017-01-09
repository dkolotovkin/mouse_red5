package mouseapp.admin;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.logger.MyLogger;
import mouseapp.user.UserConnection;
import mouseapp.user.UserForInit;
import mouseapp.user.UserRole;
import mouseapp.utils.changeinfo.ChangeInfoParams;
import mouseapp.utils.md5.MD5;

import org.red5.server.api.Red5;

public class AdministratorManager {
	public MyLogger logger = new MyLogger(AdministratorManager.class.getName());
	
	public Timer timer;
	public String currentNotification;
	List<String> vkUids = new ArrayList<String>();
	List<String> mmUids = new ArrayList<String>();
	
	public int updateAllUsersParams(){
		int result = 0;
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(user != null){
			if(user.user.role == UserRole.ADMINISTRATOR_MAIN){
				Set<Entry<String, UserConnection>> setuser = ServerMouseApplication.application.commonroom.users.entrySet();
				for (Map.Entry<String, UserConnection> u:setuser){
					ServerMouseApplication.application.userinfomanager.updateUser(u.getValue().user);			
				}
				setuser = null;
				result = 1;
			}
		}
		
		return result;
	}
	
	public int setParam(int userID, int param, int value){
		int result = 0;
		Connection _sqlconnection = null;
		PreparedStatement update = null;
		
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(userID));
		
		if(initiator.user.role == UserRole.ADMINISTRATOR_MAIN){			
			if(user != null){
				if(param == ParamType.MONEY){
					user.user.money = value;
					ServerMouseApplication.application.commonroom.changeUserInfoByID(userID, ChangeInfoParams.USER_MONEY, value, 0);
				}
				if(param == ParamType.EXPERIENCE){
					user.user.experience = value;
					ServerMouseApplication.application.commonroom.changeUserInfoByID(userID, ChangeInfoParams.USER_EXPERIENCE, value, 0);
				}
				if(param == ParamType.POPULAR){
					user.user.popular = value;
					ServerMouseApplication.application.commonroom.changeUserInfoByID(userID, ChangeInfoParams.USER_POPULAR, value, 0);
				}
				result = 1;
			}else{
				try {				
					_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
					if(param == ParamType.MONEY){
						update = _sqlconnection.prepareStatement("UPDATE user SET money=? WHERE id=?");
						update.setInt(1, value);
						update.setInt(2, userID);
					}
					if(param == ParamType.EXPERIENCE){
						update = _sqlconnection.prepareStatement("UPDATE user SET experience=? WHERE id=?");
						update.setInt(1, value);
						update.setInt(2, userID);
					}
					if(param == ParamType.POPULAR){
						update = _sqlconnection.prepareStatement("UPDATE user SET popular=? WHERE id=?");
						update.setInt(1, value);
						update.setInt(2, userID);
					}								
					if (update.executeUpdate() > 0){	
						result = 1;
					}
				} catch (SQLException e) {
					logger.error(e.toString());
				}
				finally
				{
				    try{
				    	if (_sqlconnection != null) _sqlconnection.close(); 
				    	if (update != null) update.close(); 
				    	_sqlconnection = null;
				    	update = null;
				    }
				    catch (SQLException sqlx) {		     
				    }
				}
			}
		}

		return result;
	}
	
	public int setNameParam(int userID, int param, String value){
		int result = 0;
		Connection _sqlconnection = null;
		PreparedStatement update = null;
		
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(userID));
		
		if(initiator.user.role == UserRole.ADMINISTRATOR || initiator.user.role == UserRole.ADMINISTRATOR_MAIN){
			if(param == ParamType.NAME){
				if(user != null){
					logger.log("CHANGE NAME adminID: " + initiator.user.id + " userID: " + userID + " lastUserName: " + user.user.title + " newUserName: " + value);
				}else{
					logger.log("CHANGE NAME adminID: " + initiator.user.id + " userID: " + userID + " newUserName: " + value);
				}
				try {				
					_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
					
					update = _sqlconnection.prepareStatement("UPDATE user SET title=? WHERE id=?");
					update.setString(1, value);
					update.setInt(2, userID);
					update.execute();
					
					result = 1;
					
					if(user != null){
						user.conection.close();
					}
				} catch (SQLException e) {
					logger.error(e.toString());
				}
				finally
				{
				    try{
				    	if (_sqlconnection != null) _sqlconnection.close(); 
				    	if (update != null) update.close(); 
				    	_sqlconnection = null;
				    	update = null;
				    }
				    catch (SQLException sqlx) {		     
				    }
				}
			}
		}
		return result;
	}
	
	public int setModerator(int userID){
		int result = 0;
		Connection _sqlconnection = null;
		PreparedStatement update = null;
		
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(initiator.user.role == UserRole.ADMINISTRATOR_MAIN || initiator.user.role == UserRole.ADMINISTRATOR){			
			try {				
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				
				update = _sqlconnection.prepareStatement("UPDATE user SET role=? WHERE id=?");
				update.setInt(1, 1);
				update.setInt(2, userID);								
				
				if (update.executeUpdate() > 0){	
					result = 1;
				}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close(); 
			    	if (update != null) update.close(); 
			    	_sqlconnection = null;
			    	update = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			}
		}
		return result;
	}
	
	public int deleteModerator(int userID){
		int result = 0;
		Connection _sqlconnection = null;
		PreparedStatement update = null;
		
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(initiator.user.role == UserRole.ADMINISTRATOR_MAIN || initiator.user.role == UserRole.ADMINISTRATOR){			
			try {				
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				
				update = _sqlconnection.prepareStatement("UPDATE user SET role=? WHERE id=?");
				update.setInt(1, 0);
				update.setInt(2, userID);								
				
				if (update.executeUpdate() > 0){	
					result = 1;
				}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close(); 
			    	if (update != null) update.close(); 
			    	_sqlconnection = null;
			    	update = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			}
		}
		return result;
	}
	
	public int deleteUser(int userID){
		int result = 0;
		Connection _sqlconnection = null;
		PreparedStatement delete = null;
		
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(initiator.user.role == UserRole.ADMINISTRATOR_MAIN){			
			try {				
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				
				delete = _sqlconnection.prepareStatement("DELETE FROM user WHERE id=?");
				delete.setInt(1, userID);				
				
				if (delete.executeUpdate() > 0){
					result = 1;
				}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close(); 
			    	if (delete != null) delete.close(); 
			    	_sqlconnection = null;
			    	delete = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			}
		}
		return result;
	}
	
	public UserForInit showInfo(int userID){
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(initiator.user.role == UserRole.ADMINISTRATOR || initiator.user.role == UserRole.ADMINISTRATOR_MAIN){
			if(userID > 0) return ServerMouseApplication.application.getUserInfoByID(userID);			
		}
		return null;
	}
	
	public int sendNotification(String msg){
		Connection _sqlconnection = null;
    	PreparedStatement find = null;
    	ResultSet findRes = null;
    	
		UserConnection initiator = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		int result = 0;	
		
		currentNotification = msg;
		
		vkUids = new ArrayList<String>();
		mmUids = new ArrayList<String>();
		
		String currentVkUids = new String();
		int currentVkUidsNumbers = 0;
		String currentMmUids = new String();
		int currentMmUidsNumbers = 0;
		
		if(initiator.user.role == UserRole.ADMINISTRATOR_MAIN){
			try {
	    		_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();    		    		
	    		find = _sqlconnection.prepareStatement("SELECT * FROM user");	    		
	    		findRes = find.executeQuery();    		
	    		while (findRes.next()){
	    			String idsocial = findRes.getString("idsocial");
	    			if(idsocial != null && idsocial.length() > 2){
		    			String social = idsocial.substring(0, 2);
		    			
		    			if(social.equals("vk")){
		    				if(currentVkUidsNumbers >= 100){
		    					vkUids.add(currentVkUids);		    					
		    					currentVkUids = new String();
		    					currentVkUidsNumbers = 0;		    					
		    				}
		    				if(currentVkUidsNumbers == 0 && currentVkUids.length() == 0){
		    					currentVkUids = idsocial.substring(2, idsocial.length());
		    				}else{
		    					currentVkUids = currentVkUids + "," + idsocial.substring(2, idsocial.length());
		    				}
		    				currentVkUidsNumbers++;
		    			}else if(social.equals("mm")){
		    				if(currentMmUidsNumbers >= 100){
		    					mmUids.add(currentMmUids);		    					
		    					currentMmUids = new String();
		    					currentMmUidsNumbers = 0;		    					
		    				}
		    				if(currentMmUidsNumbers == 0 && currentMmUids.length() == 0){
		    					currentMmUids = idsocial.substring(2, idsocial.length());
		    				}else{
		    					currentMmUids = currentMmUids + "," + idsocial.substring(2, idsocial.length());
		    				}
		    				currentMmUidsNumbers++;
		    			}
	    			}
	    		}
			} catch (SQLException e) {			
				logger.error("AM1 " + e.toString());
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
			if(currentVkUids.length() > 0) {vkUids.add(currentVkUids);}
			if(currentMmUids.length() > 0) {mmUids.add(currentMmUids);}			
			
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			timer = new Timer();
			timer.schedule(new TimerToStart(), 1000, 1000);
			
			result = 1;
		}
		return result;
	}
	
	class TimerToStart extends TimerTask{
        public void run(){
        	logger.log("send notification " + vkUids.size() + " : " + mmUids.size());
        	if(vkUids.size() > 0 && currentNotification != null && currentNotification.length() > 3){
        		sendVkNotification(vkUids.get(0), currentNotification, Config.apiUrlVK(), Config.appIdVK());
        		vkUids.remove(0);
        	}else if(mmUids.size() > 0 && currentNotification != null && currentNotification.length() > 3){
        		sendMmNotification(mmUids.get(0), currentNotification, Config.apiUrlMM(), Config.appIdMM());
        		mmUids.remove(0);
        	}else{
        		logger.log("SEND NOTIFICATION COMPLETED " + vkUids.size() + " : " + mmUids.size() + " : ");
        		timer.cancel();
        		timer = null;
        		currentNotification = null;
        	}
         }  
     }
	
	public void sendVkNotification(String uids, String msg, String api_url, int appID){
		try
		{
			URL url = new URL(api_url);
			DataOutputStream wr = null;			
			try{				 
				long time = (new Date()).getTime();
				int random = Math.round((float)Math.random() * Integer.MAX_VALUE);							 

				String sig = MD5.getMD5("api_id=" + appID + "message=" + msg + "method=" + "secure.sendNotification" + "random=" + random + "timestamp=" + time + "uids=" + uids + "v=3.0" + Config.protectedSecretVK());
				
				try{msg = URLEncoder.encode(msg, "UTF-8");
				}catch(Throwable e){logger.log("AM7 " + e.toString());}		
				 
				String urlparams = "api_id=" + appID + "&method=" + "secure.sendNotification" +
				                   "&message=" + msg + "&random=" + random + "&timestamp=" + time +
					 			   "&uids=" + uids + "&v=3.0" + "&sig=" + sig;
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
		         
//		        InputStream is = urlConnection.getInputStream();			        
//		        BufferedReader rd = new BufferedReader(new InputStreamReader(is));			         
//		        String line;
//		        StringBuffer response = new StringBuffer();
//		        int counter = 0;
//		        while((line = rd.readLine()) != null && counter < 250) {
//		        	response.append(line);
//					response.append('\r');
//					counter++;				
//		        }		         
//		        logger.log("sendVkNotification: " + response.toString());
		         
//		        rd.close();
//		        rd = null;
//		        is.close();
//		        is = null;
		        wr = null;
		        url = null;
		        urlConnection.disconnect();
		        urlConnection = null;
			 }				
			 catch(IOException e){
				 logger.log("AM2" + e.toString());
			 }
			 finally
				{
				    try{
				    	if (wr != null) wr.close();				    	
				    	wr = null;			    	
				    }
				    catch (Throwable e) {
				    	logger.log("AM3" + e.getMessage());
				    }
				}
		}catch(MalformedURLException e){
			logger.log("AM4" + e.toString());
		}
	}
	
	public void sendMmNotification(String uids, String msg, String api_url, int appID){
		String sig = MD5.getMD5("app_id=" + appID + "format=JSON" + "method=" + "notifications.send" + "secure=" + "1" + "text=" + msg + "uids=" + uids + Config.protectedSecretMM());
		
		try{msg = URLEncoder.encode(msg, "UTF-8");
		}catch(Throwable e){logger.log("AM7 " + e.toString());}		
				
		try{			 
			URL url = new URL(api_url + "?method=notifications.send&app_id=" + appID + "&format=JSON" + "&secure=1" + "&text=" + msg + "&uids=" + uids + "&sig=" + sig);
			
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestMethod("GET");
					         
//			InputStream is = urlConnection.getInputStream();
//			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//			String line;
//			StringBuffer response = new StringBuffer(); 
//			while((line = rd.readLine()) != null) {
//				response.append(line);
//				response.append('\r');
//			}
//			logger.log("sendMmNotification: " + response.toString());			
	  	
//			rd.close();
//			rd = null;
//			is.close();
//	        is = null;
	        urlConnection.disconnect();
	        urlConnection = null;
	        url = null;
		}catch(IOException e){
			ServerMouseApplication.application.logger.log("AM6 " + e.toString());
		}
	}
}
