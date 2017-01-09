package utils.managers.apimanager
{
	import application.GameApplication;
	import application.components.popup.PopUp;
	import application.components.popup.buymoney.PopUpBuyMoney;
	import application.components.popup.friendsBonus.PopUpFriendsBonus;
	import application.gamecontainer.StartSitePage;
	
	import flash.net.Responder;
	import flash.utils.clearInterval;
	
	import mx.controls.Alert;
	
	import utils.shop.BuyMoneyResultCode;
	import utils.user.Sex;
	import utils.vk.api.MD5;

	public class ApiManager
	{
		public var idsocail:String;
		public var title:String;
		public var sex:String;
		public var autukey:String;
		public var vid:String;
		public var appid:String;
		public var url:String = null;
		public var addMoneyUserID:int;
		
		protected var _sid:int = -1;
		
		public function ApiManager(){
		}
		
		public function init():void{
			idsocail = "test2";
			title = "Дмитрий";
			url = "www.ya.ru"
			sex = String(Sex.MALE);
//			GameApplication.app.serversmanager.checkServers();
			
			GameApplication.app.addElement(new StartSitePage());
		}
		public function inviteFriends():void{
		}
		public function showBuyMoneyPopUp():void{			
			GameApplication.app.popuper.show(new PopUpBuyMoney("Покупка монет тестовая"));
			//GameApplication.app.connection.call("shopmanager.withdrawVotesVK", null, 1, 1, "http://vkontakte.ru", 11, 11);
		}
		public function buyMoney(money:uint = 0):void{		
		}
		
		public function getFriendsBonus():void{			
		}
		
		public function updateParams():void{
			if(_sid != -1){
				clearInterval(_sid);
				_sid = -1;
			}
			
			if(GameApplication.app.userinfomanager.myuser != null){				
				if(url != null && GameApplication.app.userinfomanager.myuser.url == null || 
					GameApplication.app.userinfomanager.myuser.url.length == 0 ||
					GameApplication.app.userinfomanager.myuser.url.toLowerCase() == "null"){					
					GameApplication.app.connection.call("updateParams", null, url);
				}
			}
		}
	}
}