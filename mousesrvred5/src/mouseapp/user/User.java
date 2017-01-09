package mouseapp.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import mouseapp.ServerMouseApplication;
import mouseapp.clan.ClanUserInfo;
import mouseapp.logger.MyLogger;
import mouseapp.user.pet.PetInfo;
import mouseapp.utils.changeinfo.ChangeInfoParams;

public class User {
	public int id;
	public String idSocial;
	public String ip;
	public int sex;
	public String title;
	public byte role;
	public int level;
	public int lastlvl;
	
	public byte colortype;
	public int setcolorat;
	public byte accessorytype;
	public int setaccessoryat;
	
	public int popular;
	public int experience;
	public int exphour;
	public int expday;
	public int nextLevelExperience;
	public int money;
	public byte bantype;
	public int setbanat;
	public int changebanat;
	public String url;
	
	public ClanUserInfo claninfo;
	public PetInfo pet;
	
	public MyLogger logger = new MyLogger(User.class.getName());
	
	public User(int id, String idSocial, String ip, int sex, String title, int popular, int experience, int exphour, int expday, int lastlvl, int money, byte role, byte bantype, int setbanat, int changebanat, byte colortype, int setcolorat, byte accessorytype, int setaccessoryat, String url, ClanUserInfo claninfo, PetInfo pet){
		this.id = id;
		this.idSocial = idSocial;
		this.ip = ip;
		this.sex = sex;
		this.title = title;
		this.money = money;
		this.role = role;
		this.popular = popular;
		this.experience = experience;
		this.exphour = exphour;
		this.expday = expday;
		this.lastlvl = lastlvl;
		this.bantype = bantype;
		this.setbanat = setbanat;
		this.changebanat = changebanat;
		this.colortype = colortype;
		this.setcolorat = setcolorat;
		
		this.accessorytype = accessorytype;
		this.setaccessoryat = setaccessoryat;
		
		this.url = url;
		this.claninfo = claninfo;
		this.pet = pet;		
		
		this.level = ServerMouseApplication.application.userinfomanager.getLevelByExperience(experience);
		this.nextLevelExperience = ServerMouseApplication.application.userinfomanager.levels.get(this.level + 1);	
	}
	
	public void setParamsByExperience(int value){
		int level = ServerMouseApplication.application.userinfomanager.getLevelByExperience(value);
		if(this.level != level){
			ServerMouseApplication.application.commonroom.changeUserInfoByID(this.id, ChangeInfoParams.USER_EXPERIENCE_MAXEXPERIENCE_LEVEL, this.experience, ServerMouseApplication.application.userinfomanager.levels.get(level + 1), level);
		}
		this.level = level;
		this.nextLevelExperience = ServerMouseApplication.application.userinfomanager.levels.get(level + 1);		
	}
	
	public void updateColor(byte ct){
		Connection _sqlconnection = null;
		PreparedStatement update = null;
		
		try {		
			Date date = new Date();
			int currenttime = (int)(date.getTime() / 1000);
			date = null;  
			
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			update = _sqlconnection.prepareStatement("UPDATE user SET colortype=?,setcolorat=? WHERE id=?");
			update.setByte(1, ct);
			update.setInt(2, currenttime);
			update.setInt(3, id);
			if (update.executeUpdate() > 0){	
				colortype = ct;
				setcolorat = currenttime;
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
	
	public void updateAccessory(byte at){
		Connection _sqlconnection = null;
		PreparedStatement update = null;
		
		try {		
			Date date = new Date();
			int currenttime = (int)(date.getTime() / 1000);
			date = null;  
			
			_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
			update = _sqlconnection.prepareStatement("UPDATE user SET accessorytype=?,setaccessoryat=? WHERE id=?");
			update.setByte(1, at);
			update.setInt(2, currenttime);
			update.setInt(3, id);
			if (update.executeUpdate() > 0){	
				accessorytype = at;
				setaccessoryat = currenttime;
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
	
	public void updateExperience(int value){
		int beexperience = Math.max(value, 0);
		int delta = beexperience - experience;
		if(delta > 0){
			expday += delta;
			exphour += delta;
		}
		experience = beexperience;
		
		checkLevelBonus();
	}
	
	public void checkLevelBonus(){		
		setParamsByExperience(experience);
		
		if(lastlvl < level){
			Connection _sqlconnection = null;				
			PreparedStatement updatelastlvl = null;
			
			try {
				_sqlconnection = ServerMouseApplication.application.sqlpool.getConnection();
				
				updatelastlvl = _sqlconnection.prepareStatement("UPDATE user SET lastlvl=? WHERE id=?");
				updatelastlvl.setInt(1, level);
				updatelastlvl.setInt(2, id);						
				
        		if (updatelastlvl.executeUpdate() > 0){
        			lastlvl = level;
        			ServerMouseApplication.application.userinfomanager.setBonusNewLevel(id);								
        		}				
				
			} catch (SQLException e) {
				logger.error(e.toString());
			}
			finally
			{
			    try{
			    	if (_sqlconnection != null) _sqlconnection.close();		    	
			    	if (updatelastlvl != null) updatelastlvl.close(); 
			    	
			    	_sqlconnection = null;
			    	updatelastlvl = null;
			    }
			    catch (SQLException sqlx) {		     
			    }
			}
		}
	}
	
	public void updateExperience(int value, int changebanat){
		int beexperience = Math.max(value, 0);
		experience = beexperience;
		this.changebanat = changebanat;
		
		checkLevelBonus();
	}
	
	public void updateExpAndPopul(int exp, int pop, int changebanat){
		int beexperience = Math.max(exp, 0);
		int bepopular = Math.max(pop, 0);
		experience = beexperience;
		popular = bepopular;
		this.changebanat = changebanat;
		
		checkLevelBonus();		
	}
	
	public void updateExpAndPopul(int exp, int pop){
		int beexperience = Math.max(exp, 0);
		int bepopular = Math.max(pop, 0);
		experience = beexperience;
		popular = bepopular;
		
		checkLevelBonus();
	}
	
	public void updateMoney(int value){		
		int bemoney = Math.max(value, 0);
		money = bemoney;
	}
	
	public void updateMoney(int value, int addtodepositm, int adddtoeposite){		
		int bemoney = Math.max(value, 0);
		money = bemoney;
		claninfo.clandepositm += addtodepositm;
		claninfo.clandeposite += adddtoeposite;
	}
	
	public void update(int level, int experience, int energy){
		this.level = level;
		this.experience = experience;
	}
}