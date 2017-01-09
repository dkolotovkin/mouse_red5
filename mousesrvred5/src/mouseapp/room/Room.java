package mouseapp.room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import mouseapp.Config;
import mouseapp.logger.MyLogger;
import mouseapp.message.Message;
import mouseapp.message.MessageType;
import mouseapp.message.ban.BanMessage;
import mouseapp.message.changeinfo.MessageChangeInfo;
import mouseapp.message.changeinfo.MessageChangeInfo3;
import mouseapp.message.jp.JackPotMessage;
import mouseapp.message.jp.JackPotWinMessage;
import mouseapp.message.present.MessagePresent;
import mouseapp.message.simple.MessageSimple;
import mouseapp.message.status.MessageOutStatus;
import mouseapp.message.status.MessageStatus;
import mouseapp.user.ChatInfo;
import mouseapp.user.User;
import mouseapp.user.UserConnection;
import mouseapp.utils.ban.BanType;

import org.red5.server.api.IConnection;
import org.red5.server.api.service.IServiceCapableConnection;

public class Room {
	public MyLogger logger = new MyLogger(Room.class.getName());
	
	public int id;
	public String title;
	public Map<String, UserConnection> users;
	public Map<String, String> userConnectionIDtoID;
	public Map<String, String> userSocialIDtoID;
	public List<Message> messages;
	
	public Room(int id, String title){		
		this.id = id;
		this.title = title;
		
		users = new ConcurrentHashMap<String, UserConnection>();
		userConnectionIDtoID = new ConcurrentHashMap<String, String>();
		userSocialIDtoID = new ConcurrentHashMap<String, String>();
		messages = new ArrayList<Message>();
	}
	
	public void addUser(UserConnection u){		
		if (u.conection != null && u.conection.getClient() != null){
			
			users.put(Integer.toString(u.user.id), u);	
			userConnectionIDtoID.put(u.conection.getClient().getId(), Integer.toString(u.user.id));			
			userSocialIDtoID.put(u.user.idSocial, Integer.toString(u.user.id));
			
			MessageStatus message = new MessageStatus(MessageType.USER_IN, this, ChatInfo.createFromUser(u.user));		
			List<ChatInfo> usersinroom = new ArrayList<ChatInfo>();
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				usersinroom.add(ChatInfo.createFromUser(user.getValue().user));
				if(user.getValue().user.id != u.user.id){
					conn = user.getValue().conection;
					if (conn instanceof IServiceCapableConnection) { 
			            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
			    	}
				}
			}			
			set = null;
			
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
	}
	
	public void removeUserByConnectionID(String connID){
		String userID = userConnectionIDtoID.get(connID);
		if (userID != null && userID != "null"){
			MessageOutStatus message = new MessageOutStatus(MessageType.USER_OUT, this, users.get(userID).user.id);	
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});            
		    	}
			}
			set = null;
			
			if(users.get(userID) != null) userSocialIDtoID.remove(users.get(userID).user.idSocial);
			userConnectionIDtoID.remove(connID);
			users.remove(userID);
			
			conn = null;
			message = null;
		}
	}
	
	public UserConnection getUserByConnectionID(String cID){
		String userID = userConnectionIDtoID.get(cID);
		if (userID != null && userID != "null"){
			return users.get(userID);
		}
		return null;
	}
	
	public UserConnection getUserBySocialID(String socialID){
		String userID = userSocialIDtoID.get(socialID);
		if (userID != null && userID != "null"){
			return users.get(userID);
		}
		return null;
	}
	
	public void sendMessage(String mtext, String initiatorConnID, String receiverID){
		String userID = userConnectionIDtoID.get(initiatorConnID);		
		if (userID != null && userID != "null" && users.get(userID).user.bantype == BanType.NO_BAN){
			
			User receiverUser = null;
			UserConnection receiverUserConnetion = users.get(receiverID);			
			if (receiverUserConnetion != null){
				receiverUser = receiverUserConnetion.user;
			}
			
			int fromId = 0;
			if(users.get(userID).user != null) fromId = users.get(userID).user.id;
			int toId = 0;
			if(receiverUser != null) toId = receiverUser.id;
			
			MessageSimple message = new MessageSimple(MessageType.MESSAGE, this.id, mtext, fromId, toId);	
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				if(fromId != user.getValue().user.id)
				{
					conn = user.getValue().conection;
					if (conn instanceof IServiceCapableConnection) {
			            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
			    	}
				}
			}
			set = null;
			
			messages.add(message);
			if (messages.size() > Config.maxMessageCountInRoom()){
				messages.remove(0);
			}
			
			conn = null;
			receiverUserConnetion = null;
			receiverUser = null;
			message = null;
		}
	}
	
	public void sendMessagePresent(int prototypeID, String initiatorConnID, String receiverID){
		String userID = userConnectionIDtoID.get(initiatorConnID);		
		if (userID != null && userID != "null" && users.get(userID).user.bantype == BanType.NO_BAN){
			
			User receiverUser = null;
			UserConnection receiverUserConnetion = users.get(receiverID);			
			if (receiverUserConnetion != null){
				receiverUser = receiverUserConnetion.user;
			}
			
			int fromId = 0;
			if(users.get(userID).user != null) fromId = users.get(userID).user.id;
			int toId = 0;
			if(receiverUser != null) toId = receiverUser.id;
			
			MessagePresent message = new MessagePresent(MessageType.PRESENT, this.id, prototypeID, fromId, toId);	
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
		    	}
			}
			set = null;
			
			messages.add(message);
			if (messages.size() > Config.maxMessageCountInRoom()){
				messages.remove(0);
			}
			
			conn = null;
			receiverUserConnetion = null;
			receiverUser = null;
			message = null;
		}
	}
	
	public void sendBanMessage(int bannedId, String initiatorConnId, byte type){
		UserConnection initiator = getUserByConnectionID(initiatorConnId);
		UserConnection banned = users.get(Integer.toString(bannedId));
		if (initiator != null){		
			
			BanMessage message = null;
			
			int fromId = 0;
			if(initiator.user != null) fromId = initiator.user.id;
			int toId = 0;
			if(banned.user != null) toId = banned.user.id;
			
			message = new BanMessage(MessageType.BAN, this.id, fromId, toId, type);			
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
		    	}
			}
			set = null;
			
			messages.add(message);
			if (messages.size() > Config.maxMessageCountInRoom()){
				messages.remove(0);
			}
			conn = null;
			message = null;
		}
		initiator = null;
		banned = null;
	}
	
	public void sendJPMessage(int jp){		
		JackPotMessage message = null;
		message = new JackPotMessage(MessageType.JACK_POT, this.id, jp);
		
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			conn = user.getValue().conection;
			if (conn instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
	    	}
		}
		conn = null;
		set = null;
		message = null;
	}
	
	public void sendJPWinMessage(int jp, String winner){		
		JackPotWinMessage message = new JackPotWinMessage(MessageType.JACK_POT_WIN, this.id, jp, winner);
		
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			conn = user.getValue().conection;
			if (conn instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
	    	}
		}	
		conn = null;
		set = null;
		message = null;
	}
	
	public void sendSystemMessage(String text){		
		MessageSimple message = new MessageSimple(MessageType.SYSTEM, this.id, text, 0, 0);
		
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			conn = user.getValue().conection;
			if (conn instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
	    	}
		}	
		
		messages.add(message);
		if (messages.size() > Config.maxMessageCountInRoom()){
			messages.remove(0);
		}
		
		conn = null;
		set = null;
		message = null;
	}
	
	public void sendBanOutMessage(int userID){
		UserConnection initiator = users.get(Integer.toString(userID));
		if (initiator != null){
			
			int fromId = 0;
			if(initiator.user != null) fromId = initiator.user.id;
			int toId = 0;			
			
			BanMessage message = new BanMessage(MessageType.BAN_OUT, this.id, fromId, toId, (byte)0);
			
			IConnection conn = null;
			Set<Entry<String, UserConnection>> set = users.entrySet();
			for (Map.Entry<String, UserConnection> user:set){
				conn = user.getValue().conection;
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
		    	}
			}
			set = null;
			
			messages.add(message);
			if (messages.size() > Config.maxMessageCountInRoom()){
				messages.remove(0);
			}
			conn = null;
			message = null;
		}
		initiator = null;		
	}
	
	public void sendPrivateMessage(String mtext, String initiatorConnID, String receiverID, int proomID){
		String userID = userConnectionIDtoID.get(initiatorConnID);		
		if (userID != null && userID != "null" && users.get(userID).user.bantype == BanType.NO_BAN){			
			User receiverUser = null;
			UserConnection receiverUserConnetion = users.get(receiverID);			
			if (receiverUserConnetion != null){
				receiverUser = receiverUserConnetion.user;				
			}
			
			int fromId = 0;
			if(users.get(userID).user != null) fromId = users.get(userID).user.id;
			int toId = 0;
			if(receiverUser != null) toId = receiverUser.id;
			
			MessageSimple message = new MessageSimple(MessageType.PRIVATE, proomID, mtext, fromId, toId);
			
			IConnection conn;		
//			UserConnection u = users.get(userID);
//			if (u != null && u.conection != null){
//				conn = u.conection;
//				if (conn instanceof IServiceCapableConnection) {
//		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
//		    	}
//			}
			
			if(receiverUserConnetion != null && receiverUserConnetion.conection != null){
				conn = receiverUserConnetion.conection;
				if (conn instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
		    	}
			}
			receiverUserConnetion = null;
			receiverUser = null;
			message = null;
			conn = null;
//			u = null;
		}
	}
	
	public void changeUserInfoByID(int userID, byte param, int value1, int value2){
		if (users.get(Integer.toString(userID)) != null){
			MessageChangeInfo message = new MessageChangeInfo(MessageType.CHANGEINFO, this.id, param, value1, value2);
			
			try{
				IConnection connection = users.get(Integer.toString(userID)).conection;
				if (connection != null && connection instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) connection).invoke("processMassage", new Object[]{message});
		    	}
				connection = null;
			}catch(Throwable e){
				logger.log(e.toString());
			}
			message = null;
		}
	}
	
	public void changeUserInfoByID(int userID, byte param, int value1, int value2, int value3){
		if (users.get(Integer.toString(userID)) != null){
			MessageChangeInfo3 message = new MessageChangeInfo3(MessageType.CHANGEINFO, this.id, param, value1, value2, value3);
			
			try{
				IConnection connection = users.get(Integer.toString(userID)).conection;
				if (connection instanceof IServiceCapableConnection) {
		            ((IServiceCapableConnection) connection).invoke("processMassage", new Object[]{message});
		    	}
				connection = null;
			}catch(Throwable e){
				logger.log(e.toString());
			}
			message = null;
		}
	}	
	
	public void roomClear(){
		IConnection conn = null;
		Set<Entry<String, UserConnection>> set = users.entrySet();
		for (Map.Entry<String, UserConnection> user:set){
			MessageOutStatus message = new MessageOutStatus(MessageType.USER_OUT, this, user.getValue().user.id);
			conn = user.getValue().conection;
			if (conn instanceof IServiceCapableConnection) {
	            ((IServiceCapableConnection) conn).invoke("processMassage", new Object[]{message});
	    	}
		}
		set = null;
		conn = null;
		
		users.clear();
		userConnectionIDtoID.clear();
		userSocialIDtoID.clear();
		messages.clear();
		logger = null;
	}
}