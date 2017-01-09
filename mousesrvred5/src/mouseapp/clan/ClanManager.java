package mouseapp.clan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.logger.MyLogger;
import mouseapp.message.Message;
import mouseapp.message.MessageType;
import mouseapp.message.clan.MessageClanInvite;
import mouseapp.shop.buyresult.BuyErrorCode;
import mouseapp.shop.buyresult.BuyResult;
import mouseapp.user.UserConnection;
import mouseapp.utils.changeinfo.ChangeInfoParams;

import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IServiceCapableConnection;

public class ClanManager {
	public MyLogger logger = new MyLogger(ClanManager.class.getName());
	
	public ArrayList<ClanInfo> getClansInfo(){
		ArrayList<ClanInfo> clans = new ArrayList<ClanInfo>();
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			select = _sqlconnection.prepareStatement("SELECT * FROM clan INNER JOIN user ON clan.ownerid=user.id ORDER BY clan.experience DESC");
			selectRes = select.executeQuery();    		
    		while(selectRes.next()){
    			if(selectRes.getInt("id") > 0){
	    			ClanInfo clan = new ClanInfo(selectRes.getInt("id"), selectRes.getString("title"), selectRes.getString("user.title"), selectRes.getInt("ownerid"), selectRes.getInt("user.money"), selectRes.getInt("clan.experience"));
	    			clans.add(clan);
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
		    	_sqlconnection = null;
		    	select = null;
		    	selectRes = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		
		return clans;
	}
	
	public ClanAllInfo getClanAllInfo(int clanID){
		ArrayList<UserOfClan> users = new ArrayList<UserOfClan>();
		ClanInfo claninfo = null;
		
		Connection _sqlconnection = null;
		PreparedStatement selectclan = null;
		ResultSet selectclanRes = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;		
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			selectclan = _sqlconnection.prepareStatement("SELECT * FROM clan INNER JOIN user ON clan.ownerid=user.id AND clan.id=?");
			selectclan.setInt(1, clanID);
			selectclanRes = selectclan.executeQuery();
			if (selectclanRes.next()){
				claninfo = new ClanInfo(selectclanRes.getInt("id"), selectclanRes.getString("title"), selectclanRes.getString("user.title"), selectclanRes.getInt("ownerid"), selectclanRes.getInt("user.money"), selectclanRes.getInt("clan.experience"));
				select = _sqlconnection.prepareStatement("SELECT * FROM user WHERE clanid=? AND clanrole>? ORDER BY clandeposite DESC");
				select.setInt(1, clanID);
				select.setInt(2, (int)ClanRole.INVITED);
				selectRes = select.executeQuery();
				while(selectRes.next()){					
					if(selectRes.getByte("clanrole") > ClanRole.INVITED && selectRes.getByte("clanrole") != ClanRole.OWNER){						
						UserOfClan user = new UserOfClan(selectRes.getInt("id"), selectRes.getString("title"), ServerMouseApplication.application.userinfomanager.getLevelByExperience(selectRes.getInt("experience")), selectRes.getInt("popular"), selectRes.getInt("clandepositm"), selectRes.getInt("clandeposite"), selectRes.getByte("clanrole"), selectRes.getInt("getclanmoneyat"));
						
						if(ServerMouseApplication.application.commonroom.users.get(Integer.toString(user.id)) != null){
		    				user.isonline = true;
		    			}else{
		    				user.isonline = false;
		    			}
						users.add(user);
					}
	    		}				
			}    		
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (selectclan != null) selectclan.close(); 
		    	if (selectclanRes != null) selectclanRes.close();
		    	if (select != null) select.close(); 
		    	if (selectRes != null) selectRes.close();
		    	_sqlconnection = null;
		    	selectclan = null;
		    	selectclanRes = null;
		    	select = null;
		    	selectRes = null;
		    }
		    catch (SQLException sqlx) {	     
		    }
		}
		
		ClanAllInfo allinfo = new ClanAllInfo(claninfo, users);
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		Date date = new Date();
		int currenttime = (int)(date.getTime() / 1000);
		allinfo.time = Math.max(0, 60 * 60 - (currenttime - user.user.claninfo.getclanmoneyat));
		
		date = null;
		user = null;
		claninfo = null;	
		users = null;
		return allinfo;
	}
	
	public CreateClanResult createClan(String title){
		CreateClanResult result = new CreateClanResult();
		result.error = ClanError.OTHER;
		Connection _sqlconnection = null;
		PreparedStatement selectclan = null;
		ResultSet selectclanRes = null;
		PreparedStatement insertclan = null;
		PreparedStatement updateClanInfo = null;		
		PreparedStatement findclan = null;
		ResultSet findclanRes = null;
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(user != null && user.conection != null){		
			if (user.user.level >= Config.createClanNeedLevel()){	
				try {
					_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
					findclan = _sqlconnection.prepareStatement("SELECT * FROM clan WHERE title=?");
					findclan.setString(1, title);
					findclanRes = findclan.executeQuery();    	
		    		if(findclanRes.next()){    			
		    			result.error = ClanError.CLAN_EXIST;
		    		}else{
		    			if (user.user.money >= Config.createClanPrice()){
		    				
		    				if(user.user.claninfo.clanid > 0 && user.user.claninfo.clanrole > ClanRole.INVITED){
		    					result.error = ClanError.INOTHERCLAN;
		    					user = null;
		    					return result;
		    				}
		    				
		    				insertclan = _sqlconnection.prepareStatement("INSERT INTO clan (title, ownerid, money, experience) VALUES(?,?,?,?)");
		    				insertclan.setString(1, title);
		    				insertclan.setInt(2, user.user.id);
		    				insertclan.setInt(3, 0);
		    				insertclan.setInt(4, 0);				
		    				
		    				logger.log("Create clan " + title + " from user: " + user.user.id);
		    				
			    			if (insertclan.executeUpdate() > 0){			    				
			    				selectclan = _sqlconnection.prepareStatement("SELECT * FROM clan WHERE ownerid=?");
			    				selectclan.setInt(1, user.user.id);
			    				selectclanRes = selectclan.executeQuery();  
			    				selectclanRes.next();		    				
			    				result.clanid = selectclanRes.getInt("id");
			    				
			    				user.user.updateMoney(user.user.money - Config.createClanPrice());
			    				user.user.claninfo.clanid = result.clanid;
			    				user.user.claninfo.clantitle = selectclanRes.getString("title");
			    				user.user.claninfo.clandeposite = 0;
			    				user.user.claninfo.clandepositm = 0;	    				
			    				user.user.claninfo.clanrole = ClanRole.OWNER;
			    				
			    				result.clantitle = user.user.claninfo.clantitle;
			    				result.clandeposite = user.user.claninfo.clandeposite;
			    				result.clandepositm = user.user.claninfo.clandepositm;
			    				result.clanrole = user.user.claninfo.clanrole;
			    				result.money = user.user.money;
			    				
			    				updateClanInfo = _sqlconnection.prepareStatement("UPDATE user SET clanid=?,clanrole=? WHERE id=?");			    				
			    				updateClanInfo.setInt(1, result.clanid);			    				
			    				updateClanInfo.setInt(2, (int)ClanRole.OWNER);
			    				updateClanInfo.setInt(3, user.user.id);
			    				
			            		if (updateClanInfo.executeUpdate() > 0){
			            		}else{
			            			result.error = ClanError.OTHER;
			            			user = null;
			            			return result;
			            		}		    				
			    				result.error = ClanError.OK;
			    			}else{    	    				
			    				result.error = ClanError.OTHER;
			    				user = null;
		            			return result;
			    			}
		    			}else{
		    				result.error = ClanError.NOT_ENOUGHT_MONEY;
		    			}
		    		}
				} catch (SQLException e) {
					logger.error(e.toString());
				}
				finally
				{
				    try{
				    	if (_sqlconnection != null) _sqlconnection.close();
				    	if (insertclan != null) insertclan.close(); 
				    	if (updateClanInfo != null) updateClanInfo.close();			    	
				    	if (findclan != null) findclan.close();
				    	if (findclanRes != null) findclanRes.close();			    	
				    	if (selectclanRes != null) selectclanRes.close();
				    	if (selectclan != null) selectclan.close();
				    	_sqlconnection = null;
				    	insertclan = null;
				    	updateClanInfo = null;
				    	findclan = null;
				    	findclanRes = null;
				    	selectclanRes = null;
				    	selectclan = null;
				    }
				    catch (SQLException sqlx) {		     
				    }
				}
			}else{
				result.error = ClanError.LOWLEVEL;
			}
		}
		user = null;
		return result;
	}
	
	public int inviteUser(String userID){
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		PreparedStatement updateuser = null;
		
		int error = ClanError.OTHER;
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		UserConnection inviteduser = ServerMouseApplication.application.commonroom.users.get(userID);
		if(inviteduser != null){
			if(inviteduser.user.claninfo.clanrole <= ClanRole.INVITED){				
				if(user != null && user.user.claninfo.clanrole == ClanRole.OWNER){					
					try {
						_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
						select = _sqlconnection.prepareStatement("SELECT * FROM clan INNER JOIN user ON clan.ownerid=? AND user.id=?");
						select.setInt(1, user.user.id);
						select.setInt(2, user.user.id);
						selectRes = select.executeQuery();						
			    		if(selectRes.next()){
			    			updateuser = _sqlconnection.prepareStatement("UPDATE user SET clanid=?,clanrole=? WHERE id=?");
			    			updateuser.setInt(1, selectRes.getInt("clan.id"));
			    			updateuser.setInt(2, (int)ClanRole.INVITED);
			    			updateuser.setInt(3, inviteduser.user.id);
		    				
		            		if (updateuser.executeUpdate() > 0){		            			
		            			inviteduser.user.claninfo.clanid = selectRes.getInt("clan.id");
		            			inviteduser.user.claninfo.clanrole = ClanRole.INVITED;
		            			
		            			ClanInfo claninfo = new ClanInfo(selectRes.getInt("clan.id"), selectRes.getString("clan.title"), selectRes.getString("user.title"), selectRes.getInt("clan.ownerid"), selectRes.getInt("user.money"), selectRes.getInt("clan.experience"));
		            			MessageClanInvite message = new MessageClanInvite(MessageType.CLAN_INVITE, ServerMouseApplication.application.commonroom.id, claninfo);
		            			IConnection conn = inviteduser.conection;
		        				if (conn instanceof IServiceCapableConnection) { 
		        		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
		        		    	}
		        				
		        				conn = null;
		        				message = null;
		        				claninfo = null;
		            			error = ClanError.OK;
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
					    	if (updateuser != null) updateuser.close();
					    	_sqlconnection = null;
					    	select = null;
					    	selectRes = null;
					    	updateuser = null;
					    }
					    catch (SQLException sqlx) {		     
					    }
					}
				}else{
					error = ClanError.YOUNOTOWNER;
				}
			}else{
				error = ClanError.INOTHERCLAN;
			}
		}else{
			error = ClanError.USEROFFLINE;
		}
		user = null;
		inviteduser = null;
		return error;
	}
	
	public void inviteReject(){
		Connection _sqlconnection = null;
		PreparedStatement updateuser = null;	
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			updateuser = _sqlconnection.prepareStatement("UPDATE user SET clanid=?,clanrole=? WHERE id=?");
			updateuser.setInt(1, 0);
			updateuser.setInt(2, (int)ClanRole.NO_ROLE);
			updateuser.setInt(3, user.user.id);
			if (updateuser.executeUpdate() > 0){
				user.user.claninfo.clanid = 0;
				user.user.claninfo.clanrole = ClanRole.NO_ROLE;
			}
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (updateuser != null) updateuser.close();
		    	_sqlconnection = null;
		    	updateuser = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		user = null;
	}
	
	public int inviteAccept(){
		int clanid = 0;
		Connection _sqlconnection = null;
		PreparedStatement updateuser = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		Date date = new Date();
		int currenttime = (int)(date.getTime() / 1000);
		date = null;
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		if(user != null && user.user.claninfo.clanrole == ClanRole.INVITED && user.user.claninfo.clanid > 0){
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				
				select = _sqlconnection.prepareStatement("SELECT * FROM clan WHERE id=?");
				select.setInt(1, user.user.claninfo.clanid);
				selectRes = select.executeQuery();
	    		if(selectRes.next()){				
					updateuser = _sqlconnection.prepareStatement("UPDATE user SET clanrole=?,clandeposite=?,clandepositm=?,getclanmoneyat=? WHERE id=?");
					updateuser.setInt(1, (int)ClanRole.ROLE1);
					updateuser.setInt(2, 0);
					updateuser.setInt(3, 0);
					updateuser.setInt(4, currenttime);
					updateuser.setInt(5, user.user.id);
					if (updateuser.executeUpdate() > 0){
						user.user.claninfo.clanrole = ClanRole.ROLE1;
						user.user.claninfo.clandeposite = 0;
						user.user.claninfo.clandepositm = 0;
						user.user.claninfo.getclanmoneyat = currenttime;
						user.user.claninfo.clantitle = selectRes.getString("title");
						clanid = user.user.claninfo.clanid;
					}
	    		}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close();
			    	if (updateuser != null) updateuser.close();
			    	if (select != null) select.close();
			    	if (selectRes != null) selectRes.close();
			    	_sqlconnection = null;
			    	updateuser = null;
			    	select = null;
			    	selectRes = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			}
		}
		user = null;
		return clanid;
	}
	
	public int kick(String userID){
		Connection _sqlconnection = null;
		PreparedStatement updateuser = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		int error = ClanError.OTHER;
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		UserConnection kickeduser = ServerMouseApplication.application.commonroom.users.get(userID);
		
		if(user != null && user.user.claninfo.clanrole == ClanRole.OWNER){
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				select = _sqlconnection.prepareStatement("SELECT * FROM user WHERE id=?");
				select.setInt(1, new Integer(userID));
				selectRes = select.executeQuery();
	    		if(selectRes.next()){
	    			if(selectRes.getInt("clanid") == user.user.claninfo.clanid){				
						updateuser = _sqlconnection.prepareStatement("UPDATE user SET clanid=?,clandepositm=?,clandeposite=?,clanrole=? WHERE id=?");
						updateuser.setInt(1, 0);
						updateuser.setInt(2, 0);
						updateuser.setInt(3, 0);
						updateuser.setInt(4, (int)ClanRole.NO_ROLE);
						updateuser.setInt(5, new Integer(userID));
						if (updateuser.executeUpdate() > 0){							
							if(kickeduser != null){								
								kickeduser.user.claninfo.clanid = 0;
								kickeduser.user.claninfo.clandeposite = 0;
								kickeduser.user.claninfo.clandepositm = 0;
								kickeduser.user.claninfo.clanrole = ClanRole.NO_ROLE;
								kickeduser.user.claninfo.clantitle = null;
								
								Message message = new Message(MessageType.CLAN_KICK, ServerMouseApplication.application.commonroom.id);
								IConnection conn = kickeduser.conection;
		        				if (conn instanceof IServiceCapableConnection) { 
		        		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
		        		    	}
		        				
		        				message = null;
		        				conn = null;
							}
							error = ClanError.OK;
						}
	    			}else{
	    				error = ClanError.INOTHERCLAN;
	    			}
	    		}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close();
			    	if (updateuser != null) updateuser.close();
			    	if (select != null) select.close();
			    	if (selectRes != null) selectRes.close();
			    	_sqlconnection = null;
			    	updateuser = null;
			    	select = null;
			    	selectRes = null;
			    }
			    catch (SQLException sqlx) {	     
			    }
			}
		}else{
			error = ClanError.YOUNOTOWNER;
		}
		kickeduser = null;
		user = null;
		return error;
	}
	
	public int setRole(String userID, int role){
		Connection _sqlconnection = null;
		PreparedStatement updateuser = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		int error = ClanError.OTHER;
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		UserConnection roler = ServerMouseApplication.application.commonroom.users.get(userID);
		if(role == ClanRole.ROLE1 || role == ClanRole.ROLE2 || role == ClanRole.ROLE3 || role == ClanRole.ROLE4 || role == ClanRole.ROLE5){
			if(user != null && user.user.claninfo.clanrole == ClanRole.OWNER){
				try {
					_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
					select = _sqlconnection.prepareStatement("SELECT * FROM user WHERE id=?");
					select.setInt(1, new Integer(userID));
					selectRes = select.executeQuery();
		    		if(selectRes.next()){		    			
		    			if(selectRes.getInt("clanid") == user.user.claninfo.clanid && selectRes.getInt("clanrole") > ClanRole.INVITED){
							updateuser = _sqlconnection.prepareStatement("UPDATE user SET clanrole=? WHERE id=?");
							updateuser.setInt(1, role);
							updateuser.setInt(2, new Integer(userID));
							if (updateuser.executeUpdate() > 0){
								error = ClanError.OK;
								if(roler != null){
									roler.user.claninfo.clanrole = (byte)role;
								}
							}
		    			}else{
		    				error = ClanError.INOTHERCLAN;
		    			}
		    		}
				} catch (SQLException e) {
					logger.error(e.toString());
				}
				finally
				{
				    try{
				    	if (_sqlconnection != null) _sqlconnection.close();
				    	if (updateuser != null) updateuser.close();
				    	if (select != null) select.close();
				    	if (selectRes != null) selectRes.close();
				    	_sqlconnection = null;
				    	updateuser = null;
				    	select = null;
				    	selectRes = null;
				    }
				    catch (SQLException sqlx) {	     
				    }
				}
			}else{
				error = ClanError.YOUNOTOWNER;
			}
		}else{
			error = ClanError.NOROLE;
		}
		roler = null;
		user = null;
		return error;
	}
	
	public int leave(){
		Connection _sqlconnection = null;
		PreparedStatement updateuser = null;
		
		int error = ClanError.OTHER;		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());		
		
		if(user != null && user.user.claninfo.clanid > 0 && (user.user.claninfo.clanrole == ClanRole.ROLE1 || 
				user.user.claninfo.clanrole == ClanRole.ROLE2 || user.user.claninfo.clanrole == ClanRole.ROLE3 || 
				user.user.claninfo.clanrole == ClanRole.ROLE4 || user.user.claninfo.clanrole == ClanRole.ROLE5)){
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				updateuser = _sqlconnection.prepareStatement("UPDATE user SET clanid=?,clanrole=?,clandeposite=?,clandepositm=? WHERE id=?");
				updateuser.setInt(1, 0);
				updateuser.setInt(2, (int)ClanRole.NO_ROLE);
				updateuser.setInt(3, 0);
				updateuser.setInt(4, 0);
				updateuser.setInt(5, user.user.id);
				if (updateuser.executeUpdate() > 0){
					error = ClanError.OK;
					user.user.claninfo.clanid = 0;
					user.user.claninfo.clanrole = ClanRole.NO_ROLE;;
					user.user.claninfo.clandeposite = 0;
					user.user.claninfo.clandepositm = 0;
					user.user.claninfo.clantitle = null;
				}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close();
			    	if (updateuser != null) updateuser.close();
			    	_sqlconnection = null;
			    	updateuser = null;
			    }
			    catch (SQLException sqlx) {    
			    }
			}
		}		
		user = null;
		return error;
	}
	
	public int reset(){
		Connection _sqlconnection = null;
		PreparedStatement updateuser = null;
		
		int error = ClanError.OTHER;		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());		
		
		if(user != null && user.user.claninfo.clanrole == ClanRole.OWNER){
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				updateuser = _sqlconnection.prepareStatement("UPDATE user SET clandeposite=?,clandepositm=? WHERE clanid=?");
				updateuser.setInt(1, 0);
				updateuser.setInt(2, 0);
				updateuser.setInt(3, user.user.claninfo.clanid);
				if (updateuser.executeUpdate() > 0){
					error = ClanError.OK;
					
					Set<Entry<String, UserConnection>> set = ServerMouseApplication.application.commonroom.users.entrySet();
					for (Map.Entry<String, UserConnection> cruser:set){
						if(cruser.getValue().user.claninfo.clanid == user.user.claninfo.clanid){
							cruser.getValue().user.claninfo.clandeposite = 0;
							cruser.getValue().user.claninfo.clandepositm = 0;
						}					
					}
				}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close();
			    	if (updateuser != null) updateuser.close();
			    	_sqlconnection = null;
			    	updateuser = null;
			    }
			    catch (SQLException sqlx) {    
			    }
			}
		}else{
			error = ClanError.YOUNOTOWNER;
		}
		user = null;
		return error;
	}
	
	public int destroy(){
		Connection _sqlconnection = null;
		PreparedStatement updateuser = null;
		PreparedStatement deleteclan = null;
		
		int error = ClanError.OTHER;		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());		
		int clanID = user.user.claninfo.clanid;
		
		
		if(user != null && user.user.claninfo.clanrole == ClanRole.OWNER){
			ServerMouseApplication.application.logger.log("DESTROY CLAN " + user.user.claninfo.clanid + " USER: " + user.user.id);
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				updateuser = _sqlconnection.prepareStatement("UPDATE user SET clanid=?,clanrole=?,clandeposite=?,clandepositm=? WHERE clanid=?");
				updateuser.setInt(1, 0);
				updateuser.setInt(2, (int)ClanRole.NO_ROLE);
				updateuser.setInt(3, 0);
				updateuser.setInt(4, 0);
				updateuser.setInt(5, clanID);
				if (updateuser.executeUpdate() > 0){
					error = ClanError.OK;
					
					Set<Entry<String, UserConnection>> set = ServerMouseApplication.application.commonroom.users.entrySet();
					for (Map.Entry<String, UserConnection> cruser:set){
						if(cruser.getValue().user.claninfo.clanid == clanID){
							cruser.getValue().user.claninfo.clanid = 0;
							cruser.getValue().user.claninfo.clanrole = ClanRole.NO_ROLE;
							cruser.getValue().user.claninfo.clandeposite = 0;
							cruser.getValue().user.claninfo.clandepositm = 0;
							cruser.getValue().user.claninfo.clantitle = null;
						}					
					}
					
					deleteclan = _sqlconnection.prepareStatement("DELETE FROM clan WHERE id=?");
					deleteclan.setInt(1, clanID);					
            		if (deleteclan.executeUpdate() > 0){            			
            		}
				}				
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close();
			    	if (deleteclan != null) deleteclan.close();
			    	if (updateuser != null) updateuser.close();
			    	_sqlconnection = null;
			    	deleteclan = null;
			    	updateuser = null;
			    }
			    catch (SQLException sqlx) {    
			    }
			}
		}else{
			error = ClanError.YOUNOTOWNER;
		}
		user = null;
		return error;
	}
	
	public void updateClanDeposits(Map<Integer, ClanDeposit> clandeposits){		
		Connection _sqlconnection = null;
		PreparedStatement updateclan = null;		
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			Set<Entry<Integer, ClanDeposit>> set = clandeposits.entrySet();
			for (Map.Entry<Integer, ClanDeposit> deposit:set){
				updateclan = _sqlconnection.prepareStatement("UPDATE clan SET money=money+?,experience=experience+? WHERE id=?");
				updateclan.setInt(1, Math.max(0, deposit.getValue().depositm));
				updateclan.setInt(2, Math.max(0, deposit.getValue().deposite));
				updateclan.setInt(3, deposit.getKey());
				updateclan.executeUpdate();				
			}			
		} catch (SQLException e) {
			logger.error(e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (updateclan != null) updateclan.close();
		    	_sqlconnection = null;
		    	updateclan = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}		
	}
	
	public int getMoney(){	
		Connection _sqlconnection = null;
		PreparedStatement selectclan = null;
		ResultSet selectclanRes = null;
		PreparedStatement selectowner = null;
		ResultSet selectownerRes = null;				
		int error = ClanError.OTHER;
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		if(user != null && user.conection != null){
			Date date = new Date();
			int currenttime = (int)(date.getTime() / 1000);
			int time = Math.max(0, 60 * 60 - (currenttime - user.user.claninfo.getclanmoneyat));
			date = null;
			
			if(user.user.claninfo.clanid > 0 && user.user.claninfo.clanrole > ClanRole.INVITED){
				if(time <= 0){
					int needmoney = 0;
					if(user.user.claninfo.clanrole == ClanRole.ROLE1){
						needmoney = 10;
					}else if(user.user.claninfo.clanrole == ClanRole.ROLE2){
						needmoney = 20;
					}else if(user.user.claninfo.clanrole == ClanRole.ROLE3){
						needmoney = 50;
					}else if(user.user.claninfo.clanrole == ClanRole.ROLE4){
						needmoney = 100;
					}else if(user.user.claninfo.clanrole == ClanRole.ROLE5){
						needmoney = 300;
					}
					
					try {
						_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
						selectclan = _sqlconnection.prepareStatement("SELECT * FROM clan WHERE id=?");
						selectclan.setInt(1, user.user.claninfo.clanid);
						selectclanRes = selectclan.executeQuery();
			    		if(selectclanRes.next()){
			    			int ownerid = selectclanRes.getInt("ownerid");
			    			UserConnection owner = ServerMouseApplication.application.commonroom.users.get(Integer.toString(ownerid));
			    			
			    			selectowner = _sqlconnection.prepareStatement("SELECT * FROM user WHERE id=?");
			    			selectowner.setInt(1, ownerid);
			    			selectownerRes = selectowner.executeQuery();
				    		if(selectownerRes.next()){
				    			int money = selectownerRes.getInt("money");
				    			if(owner != null && owner.conection != null) money = owner.user.money;				    			
				    			
				    			needmoney = Math.min(needmoney, money);
				    			
				    			user.user.updateMoney(user.user.money + needmoney);
				    			ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
				    			
								user.user.claninfo.clandepositm -= needmoney;
								user.user.claninfo.getclanmoneyat = currenttime;
				    			
								ServerMouseApplication.application.userinfomanager.addMoney(ownerid, -needmoney, owner);
				    			error = ClanError.OK;
				    		}			    		
				    		owner = null;
			    		}
					} catch (SQLException e) {
						logger.error(e.toString());
					}
					finally
					{
					    try{
					    	if (_sqlconnection != null) _sqlconnection.close();
					    	if (selectclan != null) selectclan.close();
					    	if (selectclanRes != null) selectclanRes.close();
					    	if (selectowner != null) selectowner.close();
					    	if (selectownerRes != null) selectownerRes.close();					    					    
					    	_sqlconnection = null;
					    	selectclan = null;
					    	selectclanRes = null;
					    	selectowner = null;
					    	selectownerRes = null;					    						    	
					    }
					    catch (SQLException sqlx) {		     
					    }
					}				
				}else{
					error = ClanError.TIMEERROR;
				}
				user = null;
			}else{
				error = ClanError.INOTHERCLAN;
			}
		}
		return error;
	}
	
	public BuyResult buyExperience(int experience){
		BuyResult result = new BuyResult();	
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		Connection _sqlconnection = null;		
		PreparedStatement updateclan = null;
		
		if (user.user.money >= Math.floor(experience * Config.buyexperiencek())){
			if(user.user.claninfo.clanid > 0){
				try
		        {
					_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
					updateclan = _sqlconnection.prepareStatement("UPDATE clan SET experience=experience+? WHERE id=?");
					updateclan.setInt(1, experience);
					updateclan.setInt(2, user.user.claninfo.clanid);
					
		    		if (updateclan.executeUpdate() > 0){	    			
		    			user.user.updateMoney(user.user.money - (int)Math.floor(experience * Config.buyexperiencek()));
		    			ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
		    			
		    			result.error = BuyErrorCode.OK;
		    		}else{
		    			result.error = BuyErrorCode.OTHER;
		    			return result;
		    		}
		        }catch (Exception e){
		         	logger.error(e.toString());		         	
		        }
		        finally
				{
				    try{			    	    	
				    	if (updateclan != null) updateclan.close();
				    	if (_sqlconnection != null) _sqlconnection.close();
				    	updateclan = null;
				    	_sqlconnection = null;
				    }
				    catch (SQLException sqlx) {		     
				    }
				}
			}else{
				result.error = BuyErrorCode.OTHER;
			}
		}else{
			result.error = BuyErrorCode.NOT_ENOUGH_MONEY;
		}
		user = null;
		
		return result;
	}
}
