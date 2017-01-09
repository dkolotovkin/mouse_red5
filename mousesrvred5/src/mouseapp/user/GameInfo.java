package mouseapp.user;

public class GameInfo{	
	public int id;	
	public String title;	
	public int level;
	public int sex;
	
	public byte colortype;
	public byte accessorytype;
	
	public GameInfo(int id, String title, int level, int sex, byte colortype, byte accessorytype){
		this.id = id;
		this.title = title;
		this.level = level;
		this.sex = sex;
		
		this.colortype = colortype;
		this.accessorytype = accessorytype;
	}
	
	public static GameInfo createFromUser(User u){
		if(u != null){
			return new GameInfo(u.id, u.title, u.level, u.sex, u.colortype, u.accessorytype);
		}
		return null;
	}
}