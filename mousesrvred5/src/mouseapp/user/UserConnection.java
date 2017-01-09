package mouseapp.user;

import mouseapp.clan.ClanUserInfo;
import mouseapp.user.pet.PetInfo;

import org.red5.server.api.IConnection;

public class UserConnection {
	public User user;
	public IConnection conection;	
	
	public UserConnection(int id, String idSocial, String ip, int sex, String title, int popular, int experience, int exphour, int expday, int lastlvl, int money, byte role, byte bantype, int setbanat, int changebanat, byte colortype, int setcolorat, byte accessorytype, int setaccessoryat, String url, ClanUserInfo claninfo, PetInfo pet, IConnection connection){
		this.conection = connection;		
		user = new User(id, idSocial, ip, sex, title, popular, experience, exphour, expday, lastlvl, money, role, bantype, setbanat, changebanat, colortype, setcolorat, accessorytype, setaccessoryat, url, claninfo, pet);
	}
	
	public void update(int level, int experience, int energy){
		user.update(level, experience, energy);
	}
}
