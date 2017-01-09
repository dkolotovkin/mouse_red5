package mouseapp.startbonus;

import org.red5.server.api.Red5;

import mouseapp.ServerMouseApplication;
import mouseapp.clan.ClanManager;
import mouseapp.logger.MyLogger;
import mouseapp.user.AccessoryType;
import mouseapp.user.ColorType;
import mouseapp.user.UserConnection;
import mouseapp.user.chage.ChangeResult;
import mouseapp.utils.changeinfo.ChangeInfoParams;

public class StartBonusManager {
	public MyLogger logger = new MyLogger(ClanManager.class.getName());
	
	public ChangeResult changeInfo(String title, int sex){
		UserConnection u = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		ChangeResult result = new ChangeResult();
		if(u.user.level < 2 && u.user.colortype == ColorType.GRAY && u.user.setcolorat == 0){
			result = ServerMouseApplication.application.userinfomanager.changeInfo(title, sex, Red5.getConnectionLocal().getClient().getId(), result, true);
		}
    	return result;
    }
	
	public void getStartBonus(){
		UserConnection u = ServerMouseApplication.application.commonroom.getUserByConnectionID(Red5.getConnectionLocal().getClient().getId());
		
		if(u.user.level < 2 && u.user.colortype == ColorType.GRAY && u.user.setcolorat == 0){
			u.user.updateColor(ColorType.BLACK3);
			u.user.updateAccessory(AccessoryType.KOVBOYHAT3);
			
			ServerMouseApplication.application.commonroom.changeUserInfoByID(u.user.id, ChangeInfoParams.USER_COLOR_ACCESSORY, u.user.colortype, u.user.accessorytype);
		}
	}
}
