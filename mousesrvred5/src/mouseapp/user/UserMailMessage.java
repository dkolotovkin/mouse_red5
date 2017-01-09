package mouseapp.user;

import mouseapp.ServerMouseApplication;

public class UserMailMessage {
	public int id;	
	public String title;
	public int level;
	public int popular;
	public String url;
	public boolean isonline;
	public String message;
	public int messageid;
	
	public UserMailMessage(int id, String title, int experience, int popular, String url){
		this.id = id;
		this.popular = popular;
		this.title = title;
		this.url = url;
		
		setParamsByExperience(experience);
	}
	
	public void setParamsByExperience(int value){
		int level = ServerMouseApplication.application.userinfomanager.getLevelByExperience(value);
		this.level = level;		
	}
}
