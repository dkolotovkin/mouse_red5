package mouseapp.shop;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.logger.MyLogger;
import mouseapp.pet.artefact.ArtefactUser;
import mouseapp.shop.buyMoneyResult.BuyMoneyErrorCode;
import mouseapp.shop.buyMoneyResult.BuyMoneyResult;
import mouseapp.shop.buyresult.BuyErrorCode;
import mouseapp.shop.buyresult.BuyResult;
import mouseapp.shop.catagory.CategoryType;
import mouseapp.shop.catagory.ShopCategory;
import mouseapp.shop.item.Item;
import mouseapp.shop.item.ItemPresent;
import mouseapp.shop.item.ItemType;
import mouseapp.shop.itemprototype.ItemPrototype;
import mouseapp.shop.useresult.UseErrorCode;
import mouseapp.shop.useresult.UseResult;
import mouseapp.user.AccessoryType;
import mouseapp.user.ColorType;
import mouseapp.user.UserConnection;
import mouseapp.user.UserRole;
import mouseapp.utils.ban.BanType;
import mouseapp.utils.changeinfo.ChangeInfoParams;
import mouseapp.utils.sort.SortPresents;
import mouseapp.utils.sort.SortShopItems;

import org.red5.server.api.Red5;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ShopManager {
	public MyLogger logger = new MyLogger(ShopManager.class.getName());
	public ArrayList<ShopCategory> categories = new ArrayList<ShopCategory>();	
	public Map<Integer, ItemPrototype> itemprototypes = new HashMap<Integer, ItemPrototype>();
	
	public ShopManager(){
		initializeShopCategories();
		initializeItemPrototypes();
	}
	
	public ArrayList<ShopCategory> getShopCategories(){		
		return categories;
	}
	
	public ArrayList<ItemPrototype> getItemPrototypesByCategoryID(int categoryID){
		ArrayList<ItemPrototype> items = new ArrayList<ItemPrototype>();
		Set<Entry<Integer, ItemPrototype>> set = itemprototypes.entrySet();
		for (Map.Entry<Integer, ItemPrototype> item:set){
			if(item.getValue().categoryid == categoryID){
				items.add(item.getValue());
			}
		}			
		set = null;
		
		SortShopItems comparator = new SortShopItems();
		SortPresents comparatorPresent = new SortPresents();
		if(categoryID != CategoryType.PRESENTS){
			Collections.sort(items, comparator);
		}else{
			Collections.sort(items, comparatorPresent);
		}
		
		comparator = null;
		comparatorPresent = null;
		return items;
	}
	
	public void initializeShopCategories(){		
		
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			select = _sqlconnection.prepareStatement("SELECT * FROM categories");
			selectRes = select.executeQuery();   		
    		while(selectRes.next()){
    			ShopCategory category = new ShopCategory(selectRes.getInt("id"), selectRes.getString("title"));    			
    			categories.add(category);
    		}
		} catch (SQLException e) {
			logger.error("SM1" + e.toString());
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
	}
	
	public void initializeItemPrototypes(){		
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			select = _sqlconnection.prepareStatement("SELECT * FROM itemprototype");
			
			selectRes = select.executeQuery();    		
			while(selectRes.next()){
    			ItemPrototype item = new ItemPrototype(selectRes.getInt("id"), selectRes.getString("title"), selectRes.getString("description"), selectRes.getInt("count"), selectRes.getInt("price"), selectRes.getInt("type"), selectRes.getInt("showed"));
    			itemprototypes.put(item.id, item);
    		}    			
		} catch (SQLException e) {
			logger.error("SM2" + e.toString());
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
	}
	
	public ArrayList<Item> getUserItemsByCategoryID(int categoryID){	
		ArrayList<Item> items = new ArrayList<Item>();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		if(user != null){
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				select = _sqlconnection.prepareStatement("SELECT * FROM usersitems INNER JOIN itemprototype ON usersitems.idprototype=itemprototype.id AND usersitems.iduser=? AND itemprototype.type=? INNER JOIN user ON usersitems.idpresenter=user.id");
				select.setInt(1, user.user.id);
				select.setInt(2, categoryID);
				selectRes = select.executeQuery(); 		
				while(selectRes.next()){
	    			ItemPresent item = new ItemPresent(selectRes.getInt("usersitems.id"), selectRes.getInt("itemprototype.id"), selectRes.getInt("itemprototype.price"), selectRes.getString("itemprototype.title"), selectRes.getString("itemprototype.description"), selectRes.getInt("usersitems.count"), selectRes.getInt("itemprototype.type"), selectRes.getString("user.title"));
	    			items.add(item);
	    		}
			} catch (SQLException e) {
				logger.error("SM3" + e.toString());
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
		}
		
		user = null;
		return items;
	}
	
	public BuyResult buyItem(int ipid){
		BuyResult buyresult = new BuyResult();
		
		Connection _sqlconnection = null;		
		PreparedStatement finditem = null;
		ResultSet finditemRes = null;
		PreparedStatement updateitem = null;
		PreparedStatement insertitem = null;		
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			ItemPrototype prototype = itemprototypes.get(ipid);
			 		
			if(prototype != null){
    			UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    			if (user.user.money >= prototype.price){    				
    				finditem = _sqlconnection.prepareStatement("SELECT * FROM usersitems WHERE idprototype=? AND iduser=?");    				
    				finditem.setInt(1, prototype.id);
    				finditem.setInt(2, user.user.id);
    				finditemRes = finditem.executeQuery(); 		
    				if(finditemRes.next()){    					
    					updateitem = _sqlconnection.prepareStatement("UPDATE usersitems SET count=? WHERE idprototype=? AND iduser=?");
    					updateitem.setInt(1, finditemRes.getInt("count") + prototype.count);        				
    					updateitem.setInt(2, prototype.id);
    					updateitem.setInt(3, user.user.id);
                		if (updateitem.executeUpdate() > 0){                			
                		}else{           			
                			buyresult.error = BuyErrorCode.OTHER;
                			return buyresult;
                		}                		
    	    		}else{
    	    			insertitem = _sqlconnection.prepareStatement("INSERT INTO usersitems (iduser, idprototype, count) VALUES(?,?,?)");
    	    			insertitem.setInt(1, user.user.id);
    	    			insertitem.setInt(2, prototype.id);
    	    			insertitem.setInt(3, prototype.count);
    	    			if (insertitem.executeUpdate() > 0){
    	    			}else{    	    				
    	    				buyresult.error = BuyErrorCode.OTHER;
                			return buyresult;
    	    			}    	    			
    	    		}
    				user.user.updateMoney(user.user.money - prototype.price);            		
    				ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
    				
    				buyresult.itemprototype = prototype;
    				buyresult.error = BuyErrorCode.OK;
    				
    				prototype = null;
    				user = null;
    			}else{
    				buyresult.error = BuyErrorCode.NOT_ENOUGH_MONEY;
    			}
    		}else{
    			buyresult.error = BuyErrorCode.NOT_PROTOTYPE;
    		}		
		} catch (SQLException e) {
			buyresult.error = BuyErrorCode.SQL_ERROR;
			logger.error("SM14" + e.toString());
		}
		finally
		{
		    try{
		    	if (finditem != null) finditem.close();
		    	if (finditemRes != null) finditemRes.close();
		    	if (updateitem != null) updateitem.close();
		    	if (insertitem != null) insertitem.close();		    	
		    	if (_sqlconnection != null) _sqlconnection.close();		    	
		    	finditem = null;
		    	finditemRes = null;
		    	updateitem = null;
		    	insertitem = null;		    	
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		return buyresult;
	}
	
	public BuyResult buyPresent(int ipid, int userid){
		BuyResult buyresult = new BuyResult();
		
		Connection _sqlconnection = null;		
		PreparedStatement insertitem = null;		
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			ItemPrototype prototype = itemprototypes.get(ipid);
			
			if(prototype != null){   			
    			UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    			if(user != null && user.conection != null){
	    			if (user.user.money >= prototype.price){    				
	    				insertitem = _sqlconnection.prepareStatement("INSERT INTO usersitems (iduser, idpresenter, idprototype, count) VALUES(?,?,?,?)");
		    			insertitem.setInt(1, userid);
		    			insertitem.setInt(2, user.user.id);
		    			insertitem.setInt(3, prototype.id);
		    			insertitem.setInt(4, prototype.count);
		    			if (insertitem.executeUpdate() > 0){
		    			}else{
		    				buyresult.error = BuyErrorCode.OTHER;
	            			return buyresult;
		    			}
		    			
	    				user.user.updateMoney(user.user.money - prototype.price);
	    				user.user.popular = user.user.popular + (int) Math.floor((double) prototype.price / 10);	    				
	    				ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY_POPULAR, user.user.money, user.user.popular);
	    				
	    				ServerMouseApplication.application.commonroom.sendMessagePresent(prototype.id, user.conection.getClient().getId(), Integer.toString(userid));	            		
	            		
	    				buyresult.itemprototype = prototype;
	    				buyresult.error = BuyErrorCode.OK;
	    				
	    				prototype = null;
	    				user = null;
	    			}else{
	    				buyresult.error = BuyErrorCode.NOT_ENOUGH_MONEY;
	    			}
    			}else{
    				buyresult.error = BuyErrorCode.OTHER;
        			return buyresult;
    			}
    		}else{
    			buyresult.error = BuyErrorCode.NOT_PROTOTYPE;
    		}		
		} catch (SQLException e) {
			buyresult.error = BuyErrorCode.SQL_ERROR;
			logger.error("SM4" + e.toString());
		}
		finally
		{
		    try{		    	
		    	if (insertitem != null) insertitem.close();		    	
		    	if (_sqlconnection != null) _sqlconnection.close();	    
		    	insertitem = null;		    	
		    	_sqlconnection = null;	    			    	
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		return buyresult;
	}
	
	public UseResult saleItem(int itemid){
		Boolean sqlok = false;
		UseResult result = new UseResult();
		result.itemid = itemid;		
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		if(user != null && user.conection != null){		
			Connection _sqlconnection = null;
			PreparedStatement finditem = null;
			ResultSet finditemRes = null;
			PreparedStatement deleteitem = null;
			PreparedStatement updatemoney = null;
			
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				finditem = _sqlconnection.prepareStatement("SELECT * FROM usersitems WHERE id=? AND iduser=?");    				
				finditem.setInt(1, itemid);
				finditem.setInt(2, user.user.id);
				finditemRes = finditem.executeQuery(); 		
				if(finditemRes.next()){
					deleteitem = _sqlconnection.prepareStatement("DELETE FROM usersitems WHERE id=?");
					deleteitem.setInt(1, itemid);			
	        		if (deleteitem.executeUpdate() > 0){
	        			sqlok = true;
	        			result.count = 0;
	        		}else{        			
	        			result.error = UseErrorCode.ERROR;
	        		}   
					
					if (sqlok){
						ItemPrototype prototype = itemprototypes.get(finditemRes.getInt("idprototype"));						
						if(prototype != null){
							int price = (int)Math.floor((float)prototype.price * 0.5);
							
							user.user.updateMoney(user.user.money + price);
							ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
		    				
		            		result.error = UseErrorCode.GAMEACTION_OK;
						}
					}
	    		}else{
	    			result.error = UseErrorCode.ERROR;
	    		}			
			} catch (SQLException e) {
				logger.error("SM5" + e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close(); 
			    	if (finditem != null) finditem.close(); 
			    	if (finditemRes != null) finditemRes.close();		    	
			    	if (deleteitem != null) deleteitem.close();	 
			    	if (updatemoney != null) updatemoney.close();
			    	_sqlconnection = null;
			    	finditem = null;
			    	finditemRes = null;		    	
			    	deleteitem = null;
			    	updatemoney = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			    user = null;
			}
		}
		
		return result;
	}
	
	public UseResult useItem(int itemid){
		Boolean sqlok = false;
		UseResult result = new UseResult();
		result.itemid = itemid;		
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		Connection _sqlconnection = null;
		PreparedStatement finditem = null;
		ResultSet finditemRes = null;
		PreparedStatement updatecount = null;
		PreparedStatement deleteitem = null;
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			finditem = _sqlconnection.prepareStatement("SELECT * FROM usersitems WHERE id=? AND iduser=?");    				
			finditem.setInt(1, itemid);
			finditem.setInt(2, user.user.id);
			finditemRes = finditem.executeQuery(); 		
			if(finditemRes.next()){
				int becount = finditemRes.getInt("count") - 1;
				if (becount > 0){
					updatecount = _sqlconnection.prepareStatement("UPDATE usersitems SET count=? WHERE id=?");
					updatecount.setInt(1, becount);
					updatecount.setInt(2, itemid);
            		if (updatecount.executeUpdate() > 0){
            			sqlok = true;
            			result.count = becount;
            		}else{
            			result.count = becount + 1;
            			result.error = UseErrorCode.ERROR;
            		}            		
				}else{
					deleteitem = _sqlconnection.prepareStatement("DELETE FROM usersitems WHERE id=?");
					deleteitem.setInt(1, itemid);					
            		if (deleteitem.executeUpdate() > 0){
            			sqlok = true;
            			result.count = becount;
            		}else{
            			result.count = becount + 1;
            			result.error = UseErrorCode.ERROR;
            		}            		
				}
				
				if (sqlok){
					//он рхос опедлерю янбепьюел деиярбхъ
					int itemtype = finditemRes.getInt("idprototype");
					result.itemtype = itemtype;
					if(itemtype == ItemType.FOOD){												
						result.error = UseErrorCode.HEALING_OK;
					}else if(itemtype == ItemType.ENERGYC){
						result.error = UseErrorCode.HEALING_OK;
					}else  if(itemtype == ItemType.ELIXIR){
						result.error = UseErrorCode.HEALING_OK;
					}else if(itemtype == ItemType.BALL || itemtype == ItemType.BOX || itemtype == ItemType.HEAVYBOX ||
							itemtype == ItemType.SPRINGBROAD || itemtype == ItemType.STATIC || itemtype == ItemType.STICK ||
							itemtype == ItemType.KERNELRIGHT || itemtype == ItemType.KERNELLEFT){						
						result.error = UseErrorCode.GAMEACTION_OK;
					}
				}
    		}else{
    			result.error = UseErrorCode.ERROR;
    		}			
		} catch (SQLException e) {
			logger.error("SM6" + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close(); 
		    	if (finditem != null) finditem.close(); 
		    	if (finditemRes != null) finditemRes.close(); 
		    	if (updatecount != null) updatecount.close();
		    	if (deleteitem != null) deleteitem.close();	 
		    	_sqlconnection = null;
		    	finditem = null;
		    	finditemRes = null;
		    	updatecount = null;
		    	deleteitem = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		    user = null;
		}
		return result;
	}
	
	public UseResult useGameItem(int itemid, int roomID, Float itemx, Float itemy){
		UseResult result = useItem(itemid);
		ServerMouseApplication.application.gamemanager.useitem(roomID, result.itemid, result.itemtype, 0, Red5.getConnectionLocal().getClient(), itemx, itemy);		
		return result;
	}
	
	public BuyResult buyUseItem(int ipid){
		BuyResult buyresult = new BuyResult();
		
		Connection _sqlconnection = null;
		PreparedStatement findprototype = null;
		ResultSet findprototypeRes = null;
	
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			ItemPrototype prototype = itemprototypes.get(ipid);		
			if(prototype != null){
    			UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    			if(user != null && user.conection != null){
	    			if (user.user.money >= prototype.price){
	    				user.user.updateMoney(user.user.money - prototype.price);
	    				user.user.popular = user.user.popular + (int) Math.floor((double) prototype.price / 10);
	    				
	    				
	    				//он рхос опедлерю янбепьюел деиярбхъ
    					if(prototype.id == ItemType.FOOD){    						
    					}else if(prototype.id == ItemType.ENERGYC){    						
    					}else  if(prototype.id == ItemType.ELIXIR){    						
    					}else  if(prototype.id == ItemType.MOUSE_BLACK1) user.user.updateColor(ColorType.BLACK1);
    					else if(prototype.id == ItemType.MOUSE_BLACK3) user.user.updateColor(ColorType.BLACK3);
    					else if(prototype.id == ItemType.MOUSE_BLACK10) user.user.updateColor(ColorType.BLACK10);
    					else if(prototype.id == ItemType.MOUSE_WHITE1) user.user.updateColor(ColorType.WHITE1);
    					else if(prototype.id == ItemType.MOUSE_WHITE3) user.user.updateColor(ColorType.WHITE3);
    					else if(prototype.id == ItemType.MOUSE_WHITE10) user.user.updateColor(ColorType.WHITE10);
    					else if(prototype.id == ItemType.MOUSE_ORANGE1) user.user.updateColor(ColorType.ORANGE1);
    					else if(prototype.id == ItemType.MOUSE_ORANGE3) user.user.updateColor(ColorType.ORANGE3);
    					else if(prototype.id == ItemType.MOUSE_ORANGE10) user.user.updateColor(ColorType.ORANGE10);
    					else if(prototype.id == ItemType.MOUSE_BLUE1) user.user.updateColor(ColorType.BLUE1);
    					else if(prototype.id == ItemType.MOUSE_BLUE3) user.user.updateColor(ColorType.BLUE3);
    					else if(prototype.id == ItemType.MOUSE_BLUE10) user.user.updateColor(ColorType.BLUE10);
    					else if(prototype.id == ItemType.MOUSE_FIOLET1) user.user.updateColor(ColorType.FIOLET1);
    					else if(prototype.id == ItemType.MOUSE_FIOLET3) user.user.updateColor(ColorType.FIOLET3);
    					else if(prototype.id == ItemType.MOUSE_FIOLET10) user.user.updateColor(ColorType.FIOLET10);
    					
    					else if(prototype.id == ItemType.PEN1) user.user.updateAccessory(AccessoryType.PEN1);
    					else if(prototype.id == ItemType.PEN3) user.user.updateAccessory(AccessoryType.PEN3);
    					else if(prototype.id == ItemType.PEN10) user.user.updateAccessory(AccessoryType.PEN10);
    					else if(prototype.id == ItemType.BANDAGE1) user.user.updateAccessory(AccessoryType.BANDAGE1);
    					else if(prototype.id == ItemType.BANDAGE3) user.user.updateAccessory(AccessoryType.BANDAGE3);
    					else if(prototype.id == ItemType.BANDAGE10) user.user.updateAccessory(AccessoryType.BANDAGE10);
    					else if(prototype.id == ItemType.CRONE1) user.user.updateAccessory(AccessoryType.CRONE1);
    					else if(prototype.id == ItemType.CRONE3) user.user.updateAccessory(AccessoryType.CRONE3);
    					else if(prototype.id == ItemType.CRONE10) user.user.updateAccessory(AccessoryType.CRONE10);
    					else if(prototype.id == ItemType.CYLINDER1) user.user.updateAccessory(AccessoryType.CYLINDER1);
    					else if(prototype.id == ItemType.CYLINDER3) user.user.updateAccessory(AccessoryType.CYLINDER3);
    					else if(prototype.id == ItemType.CYLINDER10) user.user.updateAccessory(AccessoryType.CYLINDER10);
    					else if(prototype.id == ItemType.COOKHAT1) user.user.updateAccessory(AccessoryType.COOKHAT1);
    					else if(prototype.id == ItemType.COOKHAT3) user.user.updateAccessory(AccessoryType.COOKHAT3);
    					else if(prototype.id == ItemType.COOKHAT10) user.user.updateAccessory(AccessoryType.COOKHAT10);
    					else if(prototype.id == ItemType.KOVBOYHAT1) user.user.updateAccessory(AccessoryType.KOVBOYHAT1);
    					else if(prototype.id == ItemType.KOVBOYHAT3) user.user.updateAccessory(AccessoryType.KOVBOYHAT3);
    					else if(prototype.id == ItemType.KOVBOYHAT10) user.user.updateAccessory(AccessoryType.KOVBOYHAT10);
    					else if(prototype.id == ItemType.FLASHHAIR1) user.user.updateAccessory(AccessoryType.FLASHHAIR1);
    					else if(prototype.id == ItemType.FLASHHAIR3) user.user.updateAccessory(AccessoryType.FLASHHAIR3);
    					else if(prototype.id == ItemType.FLASHHAIR10) user.user.updateAccessory(AccessoryType.FLASHHAIR10);
    					else if(prototype.id == ItemType.HIPHOP1) user.user.updateAccessory(AccessoryType.HIPHOP1);
    					else if(prototype.id == ItemType.HIPHOP3) user.user.updateAccessory(AccessoryType.HIPHOP3);
    					else if(prototype.id == ItemType.HIPHOP10) user.user.updateAccessory(AccessoryType.HIPHOP10);
    					else if(prototype.id == ItemType.GLAMUR1) user.user.updateAccessory(AccessoryType.GLAMUR1);
    					else if(prototype.id == ItemType.GLAMUR3) user.user.updateAccessory(AccessoryType.GLAMUR3);
    					else if(prototype.id == ItemType.GLAMUR10) user.user.updateAccessory(AccessoryType.GLAMUR10);
    					else if(prototype.id == ItemType.PUMPKIN1) user.user.updateAccessory(AccessoryType.PUMPKIN1);
    					else if(prototype.id == ItemType.PUMPKIN3) user.user.updateAccessory(AccessoryType.PUMPKIN3);
    					else if(prototype.id == ItemType.PUMPKIN10) user.user.updateAccessory(AccessoryType.PUMPKIN10);
    					else if(prototype.id == ItemType.NYHAT1) user.user.updateAccessory(AccessoryType.NYHAT1);
    					else if(prototype.id == ItemType.NYHAT3) user.user.updateAccessory(AccessoryType.NYHAT3);
    					else if(prototype.id == ItemType.NYHAT10) user.user.updateAccessory(AccessoryType.NYHAT10);
    					else if(prototype.id == ItemType.NY1) user.user.updateAccessory(AccessoryType.NY1);
    					else if(prototype.id == ItemType.NY3) user.user.updateAccessory(AccessoryType.NY3);
    					else if(prototype.id == ItemType.NY10) user.user.updateAccessory(AccessoryType.NY10);  
    					else if(prototype.id == ItemType.DOCTOR1) user.user.updateAccessory(AccessoryType.DOCTOR1);  
    					else if(prototype.id == ItemType.DOCTOR3) user.user.updateAccessory(AccessoryType.DOCTOR3);  
    					else if(prototype.id == ItemType.DOCTOR10) user.user.updateAccessory(AccessoryType.DOCTOR10);
    					else if(prototype.id == ItemType.ANGEL1) user.user.updateAccessory(AccessoryType.ANGEL1);
    					else if(prototype.id == ItemType.ANGEL3) user.user.updateAccessory(AccessoryType.ANGEL3);  
    					else if(prototype.id == ItemType.ANGEL10) user.user.updateAccessory(AccessoryType.ANGEL10);  
    					else if(prototype.id == ItemType.DEMON1) user.user.updateAccessory(AccessoryType.DEMON1);  
    					else if(prototype.id == ItemType.DEMON3) user.user.updateAccessory(AccessoryType.DEMON3);  
    					else if(prototype.id == ItemType.DEMON10) user.user.updateAccessory(AccessoryType.DEMON10);  
    					else if(prototype.id == ItemType.HELMET1) user.user.updateAccessory(AccessoryType.HELMET1);
    					else if(prototype.id == ItemType.HELMET3) user.user.updateAccessory(AccessoryType.HELMET3);
    					else if(prototype.id == ItemType.HELMET10) user.user.updateAccessory(AccessoryType.HELMET10);
    					else if(prototype.id == ItemType.POLICEHAT1) user.user.updateAccessory(AccessoryType.POLICEHAT1);
    					else if(prototype.id == ItemType.POLICEHAT3) user.user.updateAccessory(AccessoryType.POLICEHAT3);
    					else if(prototype.id == ItemType.POLICEHAT10) user.user.updateAccessory(AccessoryType.POLICEHAT10);
    					
    					ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY_POPULAR, user.user.money, user.user.popular);
    					ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_COLOR_ACCESSORY, user.user.colortype, user.user.accessorytype);

    					buyresult.error = BuyErrorCode.OK;
	    				buyresult.itemprototype = prototype;
	    				
	    				prototype = null;
	    				user = null;    				
	    			}else{
	    				buyresult.error = BuyErrorCode.NOT_ENOUGH_MONEY;
	    			}
    			}else{
    				buyresult.error = BuyErrorCode.OTHER;
        			return buyresult;
    			}
    		}else{
    			buyresult.error = BuyErrorCode.NOT_PROTOTYPE;
    		}		
		} catch (SQLException e) {
			buyresult.error = BuyErrorCode.SQL_ERROR;
			logger.error("SM7" + e.toString());
		}
		finally
		{
		    try{
		    	if (findprototype != null) findprototype.close();
		    	if (findprototypeRes != null) findprototypeRes.close();
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	findprototype = null;
		    	findprototypeRes = null;		    	
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		return buyresult;
	}
	
	public BuyResult buyUseShopItem(int roomID, int ipid, int gwitemID, Float itemx, Float itemy){
		BuyResult buyresult = new BuyResult();
		
		Connection _sqlconnection = null;
		PreparedStatement findprototype = null;
		ResultSet findprototypeRes = null;
	
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			ItemPrototype prototype = itemprototypes.get(ipid);	 		
			
			if(prototype != null){			
    			
    			UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
    			if(user != null && user.conection != null){
	    			if (user.user.money >= prototype.price){
	    				user.user.updateMoney(user.user.money - prototype.price);
	    				user.user.popular = user.user.popular + (int) Math.floor((double) prototype.price / 10);
	    				
	    				ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY_POPULAR, user.user.money, user.user.popular);
	    				ServerMouseApplication.application.gamemanager.useitem(roomID, 0, prototype.id, gwitemID, Red5.getConnectionLocal().getClient(), itemx, itemy);
	            		
	    				buyresult.error = BuyErrorCode.OK;
	    				
	    				prototype = null;
	    				user = null;
	    			}else{
	    				buyresult.error = BuyErrorCode.NOT_ENOUGH_MONEY;
	    			}
    			}else{
    				buyresult.error = BuyErrorCode.OTHER;
        			return buyresult;
    			}
    		}else{
    			buyresult.error = BuyErrorCode.NOT_PROTOTYPE;
    		}		
		} catch (SQLException e) {
			buyresult.error = BuyErrorCode.SQL_ERROR;
			logger.error("SM8" + e.toString());
		}
		finally
		{
		    try{
		    	if (findprototype != null) findprototype.close();
		    	if (findprototypeRes != null) findprototypeRes.close();
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	findprototype = null;
		    	findprototypeRes = null;		    	
		    	_sqlconnection = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		return buyresult;
	}
	
	
	public BuyMoneyResult withdrawVotesVK(int uid, int votes, String api_url, int appID, int addMoneyUserID){
		BuyMoneyResult result = new BuyMoneyResult();
		
		if(votes > 0){
			UserConnection user = ServerMouseApplication.application.commonroom.users.get(Integer.toString(addMoneyUserID));
			int withdrawVotes = 0;
			
			try
			{
				 URL url = new URL(api_url);
				 DataOutputStream wr = null;
				 InputStream is = null;
				 BufferedReader rd = null;
				 InputStream istr = null;
				
				 try{				 
					 long time = (new Date()).getTime();
					 int random = Math.round((float)Math.random() * Integer.MAX_VALUE);
					 MessageDigest md5;				
					 String sigstr = "api_id=" + appID + "method=" + "secure.withdrawVotes" +
		 							"random=" + random + "timestamp=" + time + "uid=" + uid + "v=3.0" + "votes=" + votes + Config.protectedSecretVK();				 
					 String sig = new String();
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
					 } catch (NoSuchAlgorithmException e) {	 
						 logger.log("Error getting MD5 object" + e);
					 }
					 md5 = null;
					 String urlparams = "api_id=" + appID + "&method=" + "secure.withdrawVotes" +
						 				"&random=" + random + "&timestamp=" + time +
						 				"&uid=" + uid + "&v=3.0" + "&votes=" + votes + "&sig=" + sig;
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
			        		         
			         is = urlConnection.getInputStream();
			        
			         rd = new BufferedReader(new InputStreamReader(is));			         
			         String line;
			         StringBuffer response = new StringBuffer();
			         int counter = 0;
			         while((line = rd.readLine()) != null && counter < 250) {
						response.append(line);
						response.append('\r');
						counter++;				
			         }
			        
			         rd.close();
			         String answer = response.toString();	       
			       
			         Document document = null;
			         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			         factory.setValidating(false);
			         factory.setNamespaceAware(true);
			         
			         try
			         {
			        	 if(answer.indexOf("transferred") == -1){
			        		 ServerMouseApplication.application.logger.log("BAD ANSWER: " + answer);
			        		 answer = "bad answer";
			        	 }
			        	 			        	 
			        	 istr = new ByteArrayInputStream(answer.getBytes());		        	 
			        	 DocumentBuilder builder = factory.newDocumentBuilder();						
						
						try{							
							document = builder.parse(istr);
						}catch (Throwable e) {
							ServerMouseApplication.application.logger.log("not parse " + e.toString());
						}			
						
						document.getDocumentElement().normalize();						
						NodeList nodelist = document.getDocumentElement().getElementsByTagName("transferred");
						Element fstNmElmnt = (Element) nodelist.item(0);		             
						withdrawVotes = new Integer(fstNmElmnt.getTextContent());
						
			            if (withdrawVotes > 0){
			            	 int addmoney = Math.round((float)((float) withdrawVotes / 100) * Config.moneyToVote());		            	 
			            	 if (ServerMouseApplication.application.userinfomanager.addMoney(addMoneyUserID, addmoney, user)){			            		 
			            		 result.money = addmoney;
			            		 result.error = BuyMoneyErrorCode.OK;
			            	 }
			             }		            
						builder = null;
						nodelist = null;
						fstNmElmnt = null;
			         }
			         catch (Exception e){
			         	logger.error("Error Parse VK withdrawVotes answer: " + answer);		         	
			         }			         
			         factory = null;
			         document = null;			         
			         urlConnection = null;
				 }				
				 catch(IOException e){
					 logger.log("SM9" + e.toString());
				 }
				 finally
					{
					    try{
					    	if (wr != null) wr.close();
					    	if (is != null) is.close();
					    	if (rd != null) rd.close();
					    	if (istr != null) istr.close();
					    	istr = null;
					    	wr = null;
					    	is = null;
					    	rd = null;					    	
					    }
					    catch (Throwable e) {
					    	logger.log("SM15" + e.getMessage());
					    }
					}
			}catch(MalformedURLException e){
				logger.log("SM10" + e.toString());
			}
			user = null;
		}
		
		return result;
	}
	
	public BuyResult buyExperience(int experience){
		BuyResult result = new BuyResult();	
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(user != null && user.conection != null){		
			if (user.user.money >= Math.floor(experience * Config.buyexperiencek())){
				user.user.updateMoney(user.user.money - (int)Math.floor(experience * Config.buyexperiencek()));
				user.user.updateExperience(user.user.experience + experience);
				
    			ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY_EXPERIENCE, user.user.money, user.user.experience);
    			result.error = BuyErrorCode.OK;
			}else{
				result.error = BuyErrorCode.NOT_ENOUGH_MONEY;
			}
		}else{
			result.error = BuyErrorCode.OTHER;
			return result;
		}
		user = null;		
		return result;
	}
	
	public BuyResult buyLink(){
		BuyResult result = new BuyResult();	
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());		
		
		boolean needbuy = true;
		if (user.user.role == UserRole.MODERATOR || user.user.role == UserRole.ADMINISTRATOR || user.user.role == UserRole.ADMINISTRATOR_MAIN){
			needbuy = false;
		}
		
		if(user != null && user.conection != null){
		
			if (user.user.money >= Config.showLinkPrice() || !needbuy){
				if(needbuy){
					user.user.updateMoney(user.user.money - Config.showLinkPrice());
					ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
				}    			
    			result.error = BuyErrorCode.OK;
			}else{
				result.error = BuyErrorCode.NOT_ENOUGH_MONEY;
			}
		}else{
			result.error = BuyErrorCode.OTHER;
			return result;
		}
		user = null;		
		return result;
	}
	
	public int getPriceBanOff(){
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		int allduration = 0;
		if (user.user.bantype == BanType.MINUT5){
			allduration = 5 * 60;
		}else if (user.user.bantype == BanType.MINUT15){
			allduration = 15 * 60;
		}else if (user.user.bantype == BanType.MINUT30){
			allduration = 30 * 60;
		}else if (user.user.bantype == BanType.HOUR1){
			allduration = 60 * 60;
		}else if (user.user.bantype == BanType.DAY1){
			allduration = 60 * 60 * 24;
		}
		
		int price = (int) Math.ceil((float)(Math.max(0, user.user.setbanat + allduration - user.user.changebanat)) / 60) * Config.banminutePrice();
		user = null;		
		return price;
	}
	
	public BuyResult buyBanOff(){
		BuyResult result = new BuyResult();	
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(user != null && user.conection != null){
			int allduration = 0;
			if (user.user.bantype == BanType.MINUT5){
				allduration = 5 * 60;
			}else if (user.user.bantype == BanType.MINUT15){
				allduration = 15 * 60;
			}else if (user.user.bantype == BanType.MINUT30){
				allduration = 30 * 60;
			}else if (user.user.bantype == BanType.HOUR1){
				allduration = 60 * 60;
			}else if (user.user.bantype == BanType.DAY1){
				allduration = 60 * 60 * 24;
			}
			
			int price = (int) Math.ceil((float)(Math.max(0, user.user.setbanat + allduration - user.user.changebanat)) / 60) * Config.banminutePrice();
			
			if (user.user.role == UserRole.MODERATOR || user.user.role == UserRole.ADMINISTRATOR){
				price = (int) Math.floor((float) price / 10);
			}
			if (user.user.role == UserRole.ADMINISTRATOR_MAIN){
				price = 0;
			}
			
			if (user.user.money >= price){
				user.user.updateMoney(user.user.money - Math.abs(price));
				
				ServerMouseApplication.application.userinfomanager.banoff(user.user.id, user.user.ip);
    			result.error = BuyErrorCode.OK;
    			
    			user.user.bantype = BanType.NO_BAN;
				
				ServerMouseApplication.application.commonroom.sendBanOutMessage(user.user.id);
    			ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY_BANTYPE, user.user.money, user.user.bantype);
			}else{
				result.error = BuyErrorCode.NOT_ENOUGH_MONEY;
			}
		}else{
			result.error = BuyErrorCode.OTHER;
			return result;
		}
		user = null;
		
		return result;
	}
	
	public ArrayList<ArtefactUser> getArtefact(){	
		ArrayList<ArtefactUser> artefacts = new ArrayList<ArtefactUser>();
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			select = _sqlconnection.prepareStatement("SELECT * FROM artefacts WHERE uid=?");
			select.setInt(1, user.user.id);
			selectRes = select.executeQuery();    		
    		while(selectRes.next()){
    			ArtefactUser category = new ArtefactUser(selectRes.getInt("id"), selectRes.getInt("aid"));    			
    			artefacts.add(category);
    		}
		} catch (SQLException e) {
			logger.error("SM14" + e.toString());
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
		user = null;
		return artefacts;
	}
}