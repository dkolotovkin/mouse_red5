package mouseapp.user;

import java.util.List;

import mouseapp.clan.ClanUserInfo;
import mouseapp.user.pet.PetInfo;

public class UserForInit extends User {
	public int bantime;
	public int jackpot;
	public int colortime;
	public int accessorytime;
	public List<Integer> popularparts;
	public List<String> populartitles;
	
	public UserForInit(int id, String idSocial, String ip, int sex, String title, int popular, int experience, int exphour, int expday, int lastlvl, int money, byte role, byte bantype, int setbanat, int changebanat, byte colortype, int setcolorat, byte accessorytype, int setaccessoryat, String url, ClanUserInfo claninfo, PetInfo pet){
		super(id, idSocial, ip,  sex, title, popular, experience, exphour, expday, lastlvl, money, role, bantype, setbanat, changebanat, colortype, setcolorat, accessorytype, setaccessoryat, url, claninfo, pet);
	}
	
	public static UserForInit createfromUser(User u){
		if(u != null){
			return new UserForInit(u.id, u.idSocial, u.ip, u.sex, u.title, u.popular, u.experience, u.exphour, u.expday, u.lastlvl, u.money, u.role, u.bantype, u.setbanat, u.changebanat, u.colortype, u.setcolorat, u.accessorytype, u.setaccessoryat, u.url, u.claninfo, u.pet);
		}else{
			return null;
		}
	}
}
