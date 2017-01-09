package mouseapp.clan;

public class ClanInfo {
	public int id;
	public String title;
	public String ownertitle;
	public int ownerid;
	public int money;
	public int experience;
	
	public ClanInfo(int id, String title, String ownertitle, int ownerid, int money, int experience){
		this.id = id;
		this.title = title;
		this.ownertitle = ownertitle;
		this.ownerid = ownerid;
		this.money = money;
		this.experience = experience;
	}
}
