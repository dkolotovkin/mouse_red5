package mouseapp.utils.authorization;

import mouseapp.Config;
import mouseapp.ServerMouseApplication;
import mouseapp.utils.md5.MD5;

public class Authorization {
	public static boolean check(String authkey, String vid, int mode, String appID){
		if (mode == GameMode.DEBUG){
			if(Config.mode() == Config.TEST){return true;}else{return false;}
    	}else if (mode == GameMode.VK){
    		String hashMD5 = MD5.getMD5(appID + '_' +  vid + '_' +  Config.protectedSecretVK());    		
    		if (hashMD5.toString().equals(authkey)){					
    			return true;
			}else{ServerMouseApplication.application.logger.log("AuthorizationVK bad, user: " + vid);}
    	}else if (mode == GameMode.MM){
    		String hashMD5 = MD5.getMD5(appID + '_' +  vid + '_' +  Config.protectedSecretMM());    		
    		if (hashMD5.toString().equals(authkey)){					
    			return true;
			}else{ServerMouseApplication.application.logger.log("AuthorizationMM bad, user: " + vid);}    		   		
    	}else if (mode == GameMode.OD){	   	
    		//� appID ��� �������������� ���������� �������� session_key, �.�. �� ��������� ��� ����������� � appID �� ���������� � flashvars
			String hashMD5 = MD5.getMD5(vid + appID +  Config.protectedSecretOD());			 
			if (hashMD5.toString().equals(authkey)){					
				return true;
			}else{ServerMouseApplication.application.logger.log("AuthorizationOD bad, user: " + vid);}			  		
		}
		return false;
	}
}