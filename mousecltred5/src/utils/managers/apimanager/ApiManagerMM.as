package utils.managers.apimanager
{
	import application.GameApplication;
	import application.components.popup.buymoney.mm.PopUpBuyMoneyMM;
	import application.components.popup.friendsBonus.PopUpFriendsBonus;
	import application.components.preloader.PreLoaderCircle;
	
	import flash.events.Event;
	import flash.net.Responder;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	import flash.utils.clearInterval;
	import flash.utils.setInterval;
	
	import mx.controls.Alert;
	
	import org.hamcrest.object.strictlyEqualTo;
	
	import utils.mailru.MailruCall;
	import utils.mailru.MailruCallEvent;
	import utils.md5.Md5;
	import utils.user.Sex;
	import utils.vk.api.MD5;
	import utils.vk.api.serialization.json.JSON;

	public class ApiManagerMM extends ApiManager
	{		
		private var uid:String; 
		private var _preLoader:PreLoaderCircle = new PreLoaderCircle();	
		private var _connectInterval:int = -1;
		
		public function ApiManagerMM()
		{			
			super();
			try{
				MailruCall.init('flash-app', GameApplication.app.config.privatekeymm);			
				MailruCall.addEventListener (Event.COMPLETE, mailruReadyHandler);
			}catch(e:*){
				//Alert.show("Error " + e);
			}
			
			autukey = GameApplication.app.config.authentication_key;
			appid = String(GameApplication.app.config.app_id);
			vid = GameApplication.app.parameters["vid"];
			idsocail = "mm" + vid;
			
			if(_connectInterval != -1){
				clearInterval(_connectInterval);
				_connectInterval = -1;
			}
			_connectInterval = setInterval(checkAddedAndConnect, 3000);
		}
		
		override public function init():void{			
		}		
		
		private function mailruReadyHandler(event : Event) : void {
			checkAddedAndConnect();
		}	
		
		private function checkAddedAndConnect():void{			
			MailruCall.removeEventListener(Event.COMPLETE, mailruReadyHandler);
			if(_connectInterval != -1){
				clearInterval(_connectInterval);
				_connectInterval = -1;
			}
			
			if (GameApplication.app.config.is_app_user != true) {
				//если не установлено - вызываем диалог установки с разрешением на размещение виджета
				try{
					MailruCall.exec('mailru.app.users.requireInstallation', null, []);
					_preLoader.text = "Необходимо установить приложение...";
					GameApplication.app.addElement(_preLoader);
				}catch(e:*){
					GameApplication.app.serversmanager.checkServers();
				}
			}else{
				GameApplication.app.serversmanager.checkServers();			
				uid = MailruCall.exec('mailru.session.vid');	
				try{
					MailruCall.exec('mailru.app.users.requireInstallation', null, []);
				}catch(e:*){
					MailruCall.exec('mailru.common.users.getInfo', getMyUserInfo, String(uid));
				}
			}
		}
	
		private function getMyUserInfo ( users : Object ) : void {
			for each(var u:* in users){
				title = u["last_name"] + " " + u["first_name"];
				url = u["link"];				
				if (String(u["sex"]) == String(0)){
					sex = String(Sex.MALE);
				}else{
					sex = String(Sex.FEMALE);
				}
			}
			
			//idsocail = "mm" + uid;
			//GameApplication.app.connect();
			
			_sid = setInterval(updateParams, 3000);
		}
		
		override public function inviteFriends():void{
			MailruCall.exec('mailru.app.friends.invite', null);
		}
		
		override public function buyMoney(money:uint = 0):void{
			var smsprice:int = 0;
			var otherprice:int = money;
			if(otherprice == 25){
				smsprice = 1;				
			}else if(otherprice == 80){
				smsprice = 3;
			}else if(otherprice == 120){
				smsprice = 5;
			}
			var params:Object = new Object();
			params["service_id"] = addMoneyUserID;
			params["service_name"] = "Покупка евро";
			if(smsprice > 0) params["sms_price"] = smsprice;
			params["other_price"] = otherprice * 100;
			
			MailruCall.exec('mailru.app.payments.showDialog', null, params);
		}	
		
		override public function showBuyMoneyPopUp():void{			
			GameApplication.app.popuper.show(new PopUpBuyMoneyMM());
		}
		
		override public function getFriendsBonus():void{
//			GameApplication.app.connection.call("getFriendsBonus", new Responder(onGetFriendsBonus, onGetFriendsBonusError), GameApplication.app.config.vid, GameApplication.app.config.mode, GameApplication.app.config.app_id, GameApplication.app.config.session_key);
		}
		
		private function onGetFriendsBonus(count:int):void{			
			if(count > 0){
				GameApplication.app.popuper.show(new PopUpFriendsBonus(count));
			}
		}
		private function onGetFriendsBonusError(error:Object):void{			
		}
	}
}