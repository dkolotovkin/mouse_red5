package application
{
	import application.components.errorlabel.ErrorLabel;
	import application.components.preloader.LinePreLoader;
	import application.components.preloader.PreLoaderCircle;
	import application.gamecontainer.GameContainer;
	import application.gamecontainer.scene.game.GameWorld;
	import application.gamecontainer.scene.home.HomePage;
	
	import flash.debugger.enterDebugger;
	import flash.events.Event;
	import flash.events.NetStatusEvent;
	import flash.events.ProgressEvent;
	import flash.geom.Rectangle;
	import flash.net.NetConnection;
	import flash.net.ObjectEncoding;
	import flash.net.Responder;
	import flash.net.SharedObject;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.system.Security;
	import flash.utils.clearInterval;
	import flash.utils.setInterval;
	
	import mx.controls.Alert;
	import mx.core.UIComponent;
	import mx.managers.ToolTipManager;
	
	import spark.components.Application;
	
	import utils.chat.ChatManager;
	import utils.chat.actionshower.ActionsShowerMenu;
	import utils.client.ClientGameApplication;
	import utils.connection.ConnectionStatus;
	import utils.game.GameManager;
	import utils.getparams.GetParams;
	import utils.jackpot.JackPotManager;
	import utils.managers.admin.AdministratorManager;
	import utils.managers.apimanager.ApiManager;
	import utils.managers.apimanager.ApiManagerMM;
	import utils.managers.apimanager.ApiManagerOD;
	import utils.managers.apimanager.ApiManagerSite;
	import utils.managers.apimanager.ApiManagerVK;
	import utils.managers.ban.BanManager;
	import utils.managers.clan.ClanManager;
	import utils.managers.navigation.NavigationManager;
	import utils.managers.persinfo.UserInfoManager;
	import utils.managers.pet.PetManager;
	import utils.managers.tooltip.GameToolTipManager;
	import utils.managers.tooltip.types.siimple.ToolTip;
	import utils.managers.useitem.UseItemManager;
	import utils.managers.useitemingame.UseItemInGameManager;
	import utils.popup.PopUpManager;
	import utils.servers.ServersManager;
	import utils.shop.ShopManager;
	import utils.user.Sex;
	import utils.user.User;
	
	public class GameApplication extends Application
	{
		[Bindable]
		public static var app:GameApplication;
		
		public var config:GameApplicationConfig;
		public var gameContainer:GameContainer = new GameContainer();		
		public var navigator:NavigationManager = new NavigationManager();
		public var actionShowerMenu : ActionsShowerMenu = new  ActionsShowerMenu();
		[Bindable]
		public var userinfomanager:UserInfoManager = new UserInfoManager();
		public var tooltiper:GameToolTipManager = new GameToolTipManager();
		public var chatmanager:ChatManager = new ChatManager();
		[Bindable]
		public var gamemanager:GameManager = new GameManager();
		public var shopmanager:ShopManager = new ShopManager();
		[Bindable]
		public var serversmanager:ServersManager = new ServersManager();
		public var useitemmanager:UseItemManager = new UseItemManager();
		public var useitemingamemanager:UseItemInGameManager = new UseItemInGameManager();
		public var popuper:PopUpManager;
		[Bindable]
		public var jackpotmanager:JackPotManager = new JackPotManager();
		public var apimanager:ApiManager;
		
		[Bindable]
		public var banmanager:BanManager = new BanManager();
		public var clanmanager:ClanManager = new ClanManager();
		public var petmanager:PetManager = new PetManager();
		public var adminmanager:AdministratorManager = new AdministratorManager();
		
		public var connectPreLoader:PreLoaderCircle = new PreLoaderCircle();
		private var _errorLabel:ErrorLabel = new ErrorLabel();
		
		private var _connectionInterval:int = -1;
		private var _addtostageIntervall:int = -1;
		private var _tryconnected:Boolean;
		
		private var _needConnected:Boolean = true;
		public var connection:NetConnection;
		public var clientconnection:ClientGameApplication;
		
		private var _checkInterval:int = -1;
		private var _lasttime:Number;
		public var so:SharedObject;
		
		public function GameApplication()
		{
			super();
			app = this;
			config = new GameApplicationConfig(app);
			Security.allowDomain("*");
			clientconnection = new ClientGameApplication();
			so = SharedObject.getLocal("mouserun");
			
			try{
				connection = new NetConnection();
				connection.client = clientconnection;
				connection.objectEncoding = ObjectEncoding.AMF3;
				connection.addEventListener(NetStatusEvent.NET_STATUS, onnetStatus, false, 0, true);
			}catch(e:*){
				
			}
			
			popuper = new PopUpManager();
			
			actionShowerMenu.percentHeight = 100;
			actionShowerMenu.percentWidth = 100;
			
			ToolTipManager.showDelay = 400;
			ToolTipManager.scrubDelay = 0;
			ToolTipManager.toolTipClass = ToolTip;
			tooltiper.append(this);
			
			connectPreLoader.text = "Подключение к серверу...";
			_errorLabel.text = "В настоящее время сервер не доступен. Повторите попытку чуть позже...";			
			
			addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);		
			
			if(_addtostageIntervall != -1){
				clearInterval(_addtostageIntervall);
				_addtostageIntervall = -1;
			}
			_addtostageIntervall = setInterval(createApiManager, 5000);
			
			var _date:Date = new Date();
			_lasttime = _date.getTime();
			_checkInterval = setInterval(checkSpeed, 10000);			
		}
		
		private function checkSpeed():void{
			var _date:Date = new Date();
			var _delta:Number = _date.getTime() - _lasttime;
			_lasttime = _date.getTime();
			
			if(_delta < 9700 && connection && connection.connected){
				userinfomanager.isBadPlayer();	
				_needConnected = false;
			}
		}
		
		private function onAddedToStage(event : Event) : void {
			createApiManager();
		}
		
		private function createApiManager():void{			
			removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
			if(_addtostageIntervall != -1){
				clearInterval(_addtostageIntervall);
				_addtostageIntervall = -1;
			}
			
			if(GameApplication.app.config.mode == GameMode.DEBUG){
				apimanager = new ApiManager();
			}else if (GameApplication.app.config.mode == GameMode.VK){
				apimanager = new ApiManagerVK();
			}else if (GameApplication.app.config.mode == GameMode.MM){				
				apimanager = new ApiManagerMM();
			}else if (GameApplication.app.config.mode == GameMode.OD){				
				apimanager = new ApiManagerOD();
			}else if (GameApplication.app.config.mode == GameMode.SITE){				
				apimanager = new ApiManagerSite();
			}
			
			apimanager.init();
		}
		
		public function reconnect():void{			
			if(connection != null && connection.connected){				
				connection.close();
			}
//			connect();
		}
		
		public function connect():void{
			if(_connectionInterval != -1){
				clearInterval(_connectionInterval);
				_connectionInterval = -1;
			}
			_connectionInterval = setInterval(updateConnecting, config.connetionInterval);			
			updateConnecting();
		}
		
		private function updateConnecting():void{			
			if (!connection.connected && _needConnected){
				connection.connect(serversmanager.servers[serversmanager.currentServerIndex].url);
				_tryconnected = true;
				updateUIByConnection();
			}
		}
		
		private function updateUIByConnection():void{
			if (_tryconnected){
				if(!contains(connectPreLoader)) addElement(connectPreLoader);
				contains(_errorLabel) && removeElement(_errorLabel);
			}else{
				//connection.close();				
				
				addElement(_errorLabel);
				contains(connectPreLoader) && removeElement(connectPreLoader);
			}
			if (!connection.connected){	
				contains(gameContainer) && removeElement(gameContainer);
				contains(actionShowerMenu) && removeElement(actionShowerMenu);
				contains(popuper.popupzone) && removeElement(popuper.popupzone);
			}else{
				contains(connectPreLoader) && removeElement(connectPreLoader);
				contains(_errorLabel) && removeElement(_errorLabel);
				addElement(gameContainer);
				addElement(popuper.popupzone);
				addElement(actionShowerMenu);
			}
		}
		
		private function onnetStatus ( event:NetStatusEvent ):void
		{	
			_tryconnected = false;			
			if (event.info.code == ConnectionStatus.FAILED || event.info.code == ConnectionStatus.REJECT || event.info.code == ConnectionStatus.INVALID){
				gamemanager.exitGame();
				_errorLabel.text = "В настоящее время сервер не доступен. Повторите попытку чуть позже...";
				updateUIByConnection();
				return;
			}else if(event.info.code == ConnectionStatus.CLOSED){
				gamemanager.exitGame();
				chatmanager.exitGame();
				if(so && so.data["redirecing"] == true){
					_errorLabel.text = "Подождите. Уже скоро вы окажетесь в другом городе...";					
				}else{
					_errorLabel.text = "В настоящее время сервер не доступен. \nПроверьте не открыта ли игра в другом месте и попробуйте обновить страничку(F5) ...";
				}
				
				gameContainer.chat.closeRooms();
				updateUIByConnection();
			}else if(event.info.code == ConnectionStatus.SUCCESS){	
				if(GameApplication.app.config.mode == GameMode.SITE){
					if(GameApplication.app.apimanager is ApiManagerSite){
						var code:String = GetParams.getParamByName(GameApplication.app.config.getparams, "code");
						var lastconnectedmode:int;
						if(GameApplication.app.so){
							lastconnectedmode = GameApplication.app.so.data["lastconnectedmode"];
							
							if(so.data["lastconnectedmode"] == GameMode.VK && code){
								connection.call("loginSiteVK", new Responder(onLogIn, onLogInError), code, config.hardcodeSiteVkId, config.currentVersion);
							}else if(so.data["lastconnectedmode"] == GameMode.MM && code){
								connection.call("loginSiteMM", new Responder(onLogIn, onLogInError), code, config.hardcodeSiteMMId, config.currentVersion);
							}else if(so.data["lastconnectedmode"] == GameMode.OD && code){
								connection.call("loginSiteOD", new Responder(onLogIn, onLogInError), code, config.hardcodeSiteODId, config.currentVersion);
							}						
						}
					}
				}else{
					connection.call("logIn", new Responder(onLogIn, onLogInError), apimanager.idsocail, null, apimanager.autukey, apimanager.vid, config.mode, apimanager.appid, config.currentVersion);
				}
				return;
			}else{
				//popuper.showInfoPopUp("Необработанная ситуация: " + event.info.code + "\nСообщите об этом разработчикам игры. Заранее спасибо.");
			}			
			
			if (apimanager is ApiManagerSite){
				if(so && so.data["redirecing"] == true){
					(apimanager as ApiManagerSite).redirect();
				}else{
					if(GameApplication.app.config.mode == GameMode.SITE){					
						var request:URLRequest = new URLRequest(GameApplication.app.config.oficalSiteUrl);					
						navigateToURL(request, "_self");
					}
				}
			}
		}
		
		private function onLogIn(isgood:Boolean):void{
			_tryconnected = false;
			updateUIByConnection();
			
			if (isgood) {
				if(GameApplication.app.so){
					GameApplication.app.so.data["redirecing"] = false;
					GameApplication.app.so.flush();
				}
				
				serversmanager.currentServerName = serversmanager.servers[serversmanager.currentServerIndex].name;
				navigator.goHome();	
				shopmanager.init();
				petmanager.init();
				apimanager.getFriendsBonus();
			}
		}
		
		private function onLogInError(e:Object):void{			
			updateUIByConnection();
		}
		
		override protected function createChildren():void{
			this.percentWidth = this.percentHeight = 100;
			super.createChildren();
		}
	}
}