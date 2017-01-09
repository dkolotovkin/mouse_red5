package utils.access
{
	import application.GameApplication;
	import application.GameMode;
	
	import utils.shop.item.ItemType;

	public class AccessManager
	{
		public function AccessManager()
		{
		}
		
		public static function checkAccessPresent(pid:int):Boolean{
			if(GameApplication.app.config.mode == GameMode.OD){
				if(pid == ItemType.BEER || pid == ItemType.COCKTAIL){
					return false;
				}
			}
			return true;
		}
	}
}