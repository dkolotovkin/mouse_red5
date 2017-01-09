package mouseapp.db;

import mouseapp.Config;

public class DataBaseConfig {
	
	public static String connectionUrl(){
		return "jdbc:mysql://" + mysqlServerHost() +":" + mysqlServerPort() + "/" + dbname() + "?characterEncoding=" + characterEncoding();
	}	
	public static String dbname(){
		return "mouse";
	}
	public static String dblogin(){
		return "root";
	}
	public static String dbpassward(){
		return "130874";
	}
	
	private static String mysqlServerHost(){
		if(Config.mode() == Config.TEST){
			return "localhost";			
		}else{
			return "109.234.154.125";			
		}
	}
	private static String mysqlServerPort(){
		return "3306";
	}	
	private static String characterEncoding(){
		return "utf8";
	}
}
