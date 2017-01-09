package mouseapp.pet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mouseapp.ServerMouseApplication;
import mouseapp.logger.MyLogger;
import mouseapp.pet.artefact.ArtefactInfo;
import mouseapp.shop.buyresult.BuyErrorCode;
import mouseapp.shop.buyresult.BuyResult;
import mouseapp.user.UserConnection;
import mouseapp.user.pet.PetInfo;
import mouseapp.utils.changeinfo.ChangeInfoParams;

import org.red5.server.api.Red5;

public class PetManager {
	public MyLogger logger = new MyLogger(PetManager.class.getName());

	public PetManagerParams params = new PetManagerParams();
	public List<List<Integer>> petsartefacts = new ArrayList<List<Integer>>();
	
	public PetManager(){
		petsartefacts.add(params.pet1artefacts);
		petsartefacts.add(params.pet2artefacts);
		petsartefacts.add(params.pet3artefacts);
		petsartefacts.add(params.pet4artefacts);
		petsartefacts.add(params.pet5artefacts);
	}
	
	public PetManagerParams getparams(){
		return params;
	}
	
	public PetInfo getPetInfo(){
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		if(user != null){
			return user.user.pet;
		}
		return null;
	}
	
	public PetInfo buyPet(){
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		PreparedStatement insertpet = null;
		PreparedStatement updatepet = null;		
		if(user != null){			
			Date date = new Date();
			int currenttime = (int)(date.getTime() / 1000);
			
			try {				
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				select = _sqlconnection.prepareStatement("SELECT * FROM pets WHERE id=?");
				select.setInt(1, user.user.id);				
				selectRes = select.executeQuery(); 	
				if(selectRes.next()){
					if(user.user.pet.level > 0 && user.user.pet.level < 5){
						int needmoney = params.petprices.get(user.user.pet.level);
						
						if(user.user.money >= needmoney){							
							user.user.updateMoney(user.user.money - needmoney);
							ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
							
							updatepet = _sqlconnection.prepareStatement("UPDATE pets SET level=?  WHERE id=?");
							updatepet.setInt(1, user.user.pet.level + 1);
							updatepet.setInt(2, user.user.id);
							
							user.user.pet.level =  user.user.pet.level + 1;
							if (updatepet.executeUpdate() > 0){								
								 return user.user.pet;
							}
						}
					}
				}else{
					if(user.user.pet.level == 0){
						int needmoney = params.petprices.get(0);
						if(user.user.money >= needmoney){
							user.user.updateMoney(user.user.money - needmoney);
							ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY, user.user.money, 0);
							
							insertpet = _sqlconnection.prepareStatement("INSERT INTO pets (id, level, experience, energy, changeenergyat) VALUES(?,?,?,?,?)");
	    	    			insertpet.setInt(1, user.user.id);
	    	    			insertpet.setInt(2, 1);
	    	    			insertpet.setInt(3, 0);
	    	    			insertpet.setInt(4, 100);
	    	    			insertpet.setInt(5, currenttime);
	    	    			if (insertpet.executeUpdate() > 0){
	    	    				updatepet = _sqlconnection.prepareStatement("UPDATE user SET petid=?  WHERE id=?");
								updatepet.setInt(1, user.user.id);
								updatepet.setInt(2, user.user.id);						
								if (updatepet.executeUpdate() > 0){
									user.user.pet.level = 1;
		    	    				user.user.pet.energy = 100;
		    	    				user.user.pet.changeenergyat = currenttime;
		    	    				return user.user.pet;
								}    	    				
	    	    			}
						}
					}
				}				
			}catch (SQLException e) {
				logger.error("PM1" + e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close(); 
			    	if (select != null) select.close(); 
			    	if (selectRes != null) selectRes.close();
			    	if (insertpet != null) insertpet.close();
			    	if (updatepet != null) updatepet.close();
			    	_sqlconnection = null;
			    	select = null;
			    	selectRes = null;
			    	insertpet = null;
			    	updatepet = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			}
		}
		return null;
	}
	
	public int changeArtefact(int artefactID){
		Connection _sqlconnection = null;
		PreparedStatement select = null;
		ResultSet selectRes = null;
		PreparedStatement deleteart = null;
		
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
				
		try {
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			select = _sqlconnection.prepareStatement("SELECT * FROM artefacts WHERE id=? AND uid=?");
			select.setInt(1, artefactID);
			select.setInt(2, user.user.id);
			selectRes = select.executeQuery();    		
    		if(selectRes.next()){
    			ArtefactInfo artefact = ServerMouseApplication.application.petmanager.params.artefacts.get(selectRes.getInt("aid"));
    			
    			deleteart = _sqlconnection.prepareStatement("DELETE FROM artefacts WHERE id=? AND uid=?");
    			deleteart.setInt(1, artefactID);
    			deleteart.setInt(2, user.user.id);			
        		if (deleteart.executeUpdate() > 0){
        			user.user.updateExpAndPopul(user.user.experience + artefact.experience, user.user.popular + artefact.popular);
        			ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_EXPERIENCE_AND_POPULAR, user.user.experience, user.user.popular);
        		}
        		
        		artefact = null;
    		}
		} catch (SQLException e) {
			logger.error("PM2" + e.toString());
		}
		finally
		{
		    try{
		    	if (_sqlconnection != null) _sqlconnection.close();
		    	if (select != null) select.close(); 
		    	if (selectRes != null) selectRes.close();
		    	if (deleteart != null) deleteart.close();
		    	_sqlconnection = null;
		    	select = null;
		    	selectRes = null;
		    	deleteart = null;
		    }
		    catch (SQLException sqlx) {		     
		    }
		}
		return 0;
	}
	
	public BuyResult feedPet(){
		BuyResult result = new BuyResult();
		UserConnection user = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		Connection _sqlconnection = null;		
		PreparedStatement updatepet = null;
		
		if(user.user.pet.level > 0 && params.petfeedprices.size() >= user.user.pet.level){
			int price = params.petfeedprices.get(user.user.pet.level - 1);
			if(user.user.money >= price){
				try {
					_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
					updatepet = _sqlconnection.prepareStatement("UPDATE pets SET energy=? WHERE id=?");
					updatepet.setInt(1, 100);
					updatepet.setInt(2, user.user.id);						
					if (updatepet.executeUpdate() > 0){
						 user.user.pet.energy = 100;
						 user.user.updateMoney(user.user.money - price);
						 result.error = BuyErrorCode.OK;
						 
						 ServerMouseApplication.application.commonroom.changeUserInfoByID(user.user.id, ChangeInfoParams.USER_MONEY_AND_PET_ENERGY, user.user.money, user.user.pet.energy);
					}  		
				} catch (SQLException e) {
					logger.error("PM3" + e.toString());
				}
				finally
				{
				    try{
				    	if (_sqlconnection != null) _sqlconnection.close();
				    	if (updatepet != null) updatepet.close();	    	
				    	_sqlconnection = null;
				    	updatepet = null;				    	
				    }
				    catch (SQLException sqlx) {		     
				    }
				}
			}else{
				result.error = BuyErrorCode.NOT_ENOUGH_MONEY;
			}
		}else{
			result.error = BuyErrorCode.OTHER;
		}
		
		user = null;
		return result;
	}
}
