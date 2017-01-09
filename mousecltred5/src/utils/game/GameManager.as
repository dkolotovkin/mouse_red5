package utils.game
{
	import application.GameApplication;
	import application.components.popup.addtobetgame.PopUpAddToBetGame;
	import application.components.popup.bet.retrn.PopUpReturnMoney;
	import application.components.popup.extraction.PopUpExtraction;
	import application.components.popup.extraction.bets.PopUpExtractionBets;
	import application.components.popup.help.startbonus.PopUpStartBonus;
	import application.gamecontainer.scene.bag.article.BagInGameSmallArticle;
	import application.gamecontainer.scene.betspage.BetsRoom;
	import application.gamecontainer.scene.game.GameWorld;
	
	import flash.events.EventDispatcher;
	import flash.events.TimerEvent;
	import flash.net.Responder;
	import flash.utils.Timer;
	
	import org.hamcrest.object.strictlyEqualTo;
	
	import utils.game.action.GameActionSubType;
	import utils.game.action.GameActionType;
	import utils.game.betroominfo.BetResult;
	import utils.game.betroominfo.GameBetRoomInfo;
	import utils.shop.item.ItemType;

	public class GameManager extends EventDispatcher
	{
		public var timer:Timer;
		public var timeround:int;
		public var roomID:int;		
		public var gameworld:GameWorld;
		[Bindable]
		public var gameMode:Boolean = false;
		
		private var _callBackBetGames:Function;
		private var _callBackBetsInfo:Function;
		
		public var sendRequest:Boolean = false;
		
		public function GameManager(){
		}
		
		/***************************ИГРА НА СТАВКИ*****************************/
		//получить информацию о конкретной игре на ставки(участники и ставки)
		public function getBetsInfo(roomID:int, callback:Function):void{
			_callBackBetsInfo = callback;
			GameApplication.app.connection.call("gamemanager.getBetsInfo", new Responder(ongetBetsInfo, ongetBetGamesInfoError), roomID);
		}		
		private function ongetBetsInfo(result:Object):void{
			_callBackBetsInfo && _callBackBetsInfo(result);			
			_callBackBetsInfo = null;
		}
		
		//войти в комнату с игрой на ставки(до начала забега)
		public function addToBetsGame(roomID:int):void{
			if(!sendRequest){
				var startResponder:Responder = new Responder(onBetsEnter, onStartError);		
				GameApplication.app.connection.call("gamemanager.addToBetsGame", startResponder, roomID);
				sendRequest = true;
			}
		}
		
		//создать игру на ставки
		public function createBetsGame():void{
			if(!sendRequest){
				var startResponder:Responder = new Responder(onBetsEnter, onStartError);		
				GameApplication.app.connection.call("gamemanager.createBetsGame", startResponder);
				sendRequest = true;
			}
		}
		private function onBetsEnter(result:Object):void{			
			if (result["type"] == GameActionType.BAD_PARAMS){				
				GameApplication.app.popuper.showInfoPopUp("Вы не можете создавать забеги на ставки. Минимальные требования: популярность не ниже 50(" + GameApplication.app.userinfomanager.getPopularTitle(50).toLocaleLowerCase() + "), уровень не ниже 3(200 опыта).");
			}else if (result["added"] && !gameMode){
				GameApplication.app.popuper.hidePopUp();
				GameApplication.app.navigator.goBetsRoom(result["roomID"], result["betsinfo"]);
			}
			sendRequest = false;
		}
		
		//получить информацию о доступных ирах на ставки
		public function getBetsGamesInfo(callback:Function):void{
			_callBackBetGames = callback;
			GameApplication.app.connection.call("gamemanager.getBetsGamesInfo", new Responder(ongetBetGamesInfo, ongetBetGamesInfoError));
		}
		
		//сделать ставку
		public function bet(roomid:int, userid:int, bet:int):void{
			GameApplication.app.connection.call("gamemanager.bet", new Responder(onBet, ongetBetGamesInfoError), roomid, userid, bet);
		}
		private function onBet(result:int):void{
			if(result == BetResult.OK){
				GameApplication.app.popuper.hidePopUp();
			}else if(result == BetResult.NO_USER){
				GameApplication.app.popuper.showInfoPopUp("Невозможно сделать ставку на этого пользователя. Возможно он вышел из игры.");
			}else if(result == BetResult.NO_ROOM){
				GameApplication.app.popuper.showInfoPopUp("Невозможно сделать ставку. Возможно игра уже началась или организатор вышел из игры.");
			}else if(result == BetResult.NO_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У вас недостаточно денег для ставки.");
			}else{
				GameApplication.app.popuper.showInfoPopUp("Невозможно сделать ставку.");
			}
		}
		
		//послать заявку на участие в игре на ставки
		public function sendRequestBetsGame(roomid:int):void{
			GameApplication.app.connection.call("gamemanager.sendRequestBetsGame", null, roomid);
		}
		
		//добавить пользователя в игру на ставки(для организатора забега)
		public function addUserToBetsGame(roomid:int, userid:int):void{
			GameApplication.app.connection.call("gamemanager.addUserToBetsGame", null, roomid, userid);
		}
		
		//начать игру на ставки(для организатора забега)
		public function startBetsGame(roomid:int):void{
			var startResponder:Responder = new Responder(onstartBetsGame, null);
			GameApplication.app.connection.call("gamemanager.startBetsGame", startResponder, roomid);
		}
		private function onstartBetsGame(result:Boolean):void{
			if(!result){
				GameApplication.app.popuper.showInfoPopUp("Невозможно начать забег. Проверьте, возможно недостаточно участников или их количество превышено.");
			}
		}
		//выйти из игры на ставки
		public function exitBetsGame(roomid:int):void{
			var startResponder:Responder = new Responder(onstartBetsGame, null);
			GameApplication.app.connection.call("gamemanager.exitBetsGame", null, roomid);
		}		
		/******************************ИГРА НА ДЕНЬГИ*********************************/
		//получить информацию о доступных играх на деньги
		public function getBetGamesInfo(callback:Function):void{
			_callBackBetGames = callback;
			GameApplication.app.connection.call("gamemanager.getBetGamesInfo", new Responder(ongetBetGamesInfo, ongetBetGamesInfoError));
		}		
		private function ongetBetGamesInfo(rooms:Array):void{
			var list:Array = new Array();
			for(var i:uint = 0; i < rooms.length; i++){
				if(rooms[i]){
					var room:GameBetRoomInfo = new GameBetRoomInfo(rooms[i]["id"], rooms[i]["bet"], rooms[i]["time"], rooms[i]["isseats"], Boolean(rooms[i]["rlocked"]), rooms[i]["users"], rooms[i]["creatorid"]);				
					list.push(room);
				}
			}
			_callBackBetGames && _callBackBetGames(list);			
			_callBackBetGames = null;
		}
		private function ongetBetGamesInfoError(error:Object):void{
			_callBackBetGames = null;
			GameApplication.app.popuper.showInfoPopUp("Ошибка при получении списка игр");
		}
		
		//создать игру на деньги
		public function createBetGame(bet:int, pass:String):void{
			if(!sendRequest){
				var startResponder:Responder = new Responder(onStart, onStartError);		
				GameApplication.app.connection.call("gamemanager.createBetGame", startResponder, bet, pass);
				sendRequest = true;
			}
		}
		//войти в игру на деньги
		public function addToBetGame(roomID:int, pass:String):void{
			if(!sendRequest){
				var startResponder:Responder = new Responder(onStart, onStartError);		
				GameApplication.app.connection.call("gamemanager.addToBetGame", startResponder, roomID, pass);
				sendRequest = true;
			}
		}
		
		
		
		/*******************************ОБЫЧНАЯ ИГРА***********************************/
		//войти в игру(в забег)
		public function sendStartRequest():void{
			if(!sendRequest){
				sendRequest = true;
				var startResponder:Responder = new Responder(onStart, onStartError);			
				GameApplication.app.connection.call("gamemanager.startRequest", startResponder);
			}
		}
		private function onStart(result:Object):void{
			if (result["added"] && !gameMode){
				GameApplication.app.popuper.hidePopUp();
				GameApplication.app.navigator.goFindUsersScreen(result["waittime"]);
			}
			sendRequest = false;
		}
		private function onStartError(err:Object):void{
			GameApplication.app.navigator.goHome();
			GameApplication.app.popuper.showInfoPopUp("Произошла ошибка при обращении к серверу. Код ошибки 111. Сообщите об этом разработчикам!");
			sendRequest = false;
		}
		
		
		
		
		
		public function processGameAction(action:Object):void{			
			if(action){					
				if (action["type"] == GameActionType.NOT_ENOUGH_USERS){
					GameApplication.app.navigator.goHome();
					GameApplication.app.popuper.showInfoPopUp("Нет желающих поиграть прямо сейчас. Повторите попытку немного позже.");
				}else if (action["type"] == GameActionType.NOT_ENOUGH_ENERGY){					
					GameApplication.app.popuper.showInfoPopUp("У вас недостаточно энергии для игры (необходимо " + action["needEnergy"]+ "). Вы можете немного подождать (энергия восстановится со временем) или купить в магазине что-нибудь для восстановления.");
				}else if (action["type"] == GameActionType.NOT_ENOUGH_MONEY){					
					GameApplication.app.popuper.showInfoPopUp("У вас недостаточно денег для игры.");
				}else if (action["type"] == GameActionType.NO_ROOM){					
					GameApplication.app.popuper.showInfoPopUp("Игровая комната не найдена, возможно игра уже началась");
				}else if (action["type"] == GameActionType.NO_SEATS){					
					GameApplication.app.popuper.showInfoPopUp("Нет свободных мест");
				}else if (action["type"] == GameActionType.BAD_PASSWARD){					
					GameApplication.app.popuper.show(new PopUpAddToBetGame(action["roomID"]));
				}else if(action["type"] == GameActionType.START){					
					GameApplication.app.gameContainer.chat.finishedpanel.clearPanel();
					//GameApplication.app.gameContainer.chat.bagingame.updatePanel();
					gameMode = true;
					for(var i:int; i < GameApplication.app.shopmanager.gameinventory.length; i++){
						var art:BagInGameSmallArticle = GameApplication.app.shopmanager.gameinventory[i];
						art.visible = art.includeInLayout = true;
						if(action["gametype"] != 0){
							if(art.itemp.id == ItemType.BALL || art.itemp.id == ItemType.BOX ||
								art.itemp.id == ItemType.HEAVYBOX ||
								art.itemp.id == ItemType.KERNELLEFT || art.itemp.id == ItemType.KERNELRIGHT){
								art.visible = art.includeInLayout = false;
							}
						}
					}
					
					gameworld = GameApplication.app.navigator.goGameWorld(action["roomID"], XML(action["locationXML"]), (action["users"] as Array), action["gametype"]);
					
					timeround = action["time"];
					timer = new Timer(1000, timeround);
					timer.start();
					timer.addEventListener(TimerEvent.TIMER, timerHandler);
					dispatchEvent(new GameManagerTimerEvent(GameManagerTimerEvent.TIMER_UPDATE, timeround));
				}else if(action["type"] == GameActionType.ACTION){
					if (gameworld != null && gameworld.roomID == int(action["roomID"])){
						if (action["subtype"] == GameActionSubType.GOTOLEFT){
							gameworld.userGotoLeft(action["initiatorID"], action["down"], action["userx"], action["usery"], action["lvx"], action["lvy"]);
						}else if (action["subtype"] == GameActionSubType.GOTORIGHT){
							gameworld.userGotoRight(action["initiatorID"], action["down"], action["userx"], action["usery"], action["lvx"], action["lvy"]);
						}else if (action["subtype"] == GameActionSubType.JUMP){
							gameworld.userJump(action["initiatorID"], action["down"], action["userx"], action["usery"], action["lvx"], action["lvy"]);
						}else if (action["subtype"] == GameActionSubType.FINDSOURCE){
							gameworld.userFindSource(action["initiatorID"], action["userx"], action["usery"]);
						}else if (action["subtype"] == GameActionSubType.FINDEXIT){
							gameworld.userFindExit(action["initiatorID"], action["userx"], action["usery"]);
							GameApplication.app.gameContainer.chat.finishedpanel.addFinishedUser(action["position"], action["initiatorTitle"]);
						}
					}
				}else if(action["type"] == GameActionType.USE_GAMEITEM){
					if(gameworld != null && gameworld.roomID == int(action["roomID"])){
						GameApplication.app.gameContainer.chat.useGameItem(gameworld.roomID, action["initiatorID"],  action["itemtype"])
						gameworld.createGameItem(action["itemtype"], action["gwitemID"], action["itemx"], action["itemy"], action["initiatorID"]);					
					}
				}else if (action["type"] == GameActionType.FINISH){		
					trace("FINISH ");
					//GameApplication.app.gameContainer.chat.bagingame.clearPanel();
					if(gameworld != null && gameworld.roomID == int(action["roomID"])){
						exitGame();
						var popUp:PopUpExtraction = new PopUpExtraction(action["extraction"], action["position"]);			
						GameApplication.app.popuper.show(popUp);
					}
				}else if (action["type"] == GameActionType.WAIT_START){					
					if (!gameMode){
						GameApplication.app.popuper.hidePopUp();
						GameApplication.app.navigator.goFindUsersScreen(action["waittime"]);
					}
				}else if (action["type"] == GameActionType.RETURN_MONEY){					
					GameApplication.app.popuper.show(new PopUpReturnMoney(action["userid"], action["money"]));
				}else if (action["type"] == GameActionType.ORGANIZER_EXIT){					
					GameApplication.app.navigator.goHome();
					GameApplication.app.popuper.showInfoPopUp("Организатор забега вышел из игры.");
				}else if (action["type"] == GameActionType.BETS_CONTENT){					
					if(GameApplication.app.navigator.currentSceneContent != null &&
						GameApplication.app.navigator.currentSceneContent is BetsRoom){
						(GameApplication.app.navigator.currentSceneContent as BetsRoom).updateBetsInfo(action["betsinfo"]);
					}					
				}else if (action["type"] == GameActionType.FINISH_BETS){					
					if(gameworld != null && gameworld.roomID == int(action["roomID"])){
						exitGame();
					}
					var pp:PopUpExtractionBets = new PopUpExtractionBets(action["returnmoney"], action["winnermoney"], action["prizemoney"]);			
					GameApplication.app.popuper.show(pp);
				}/*else if(action["type"] == GameActionType.GET_GWITEMS){
				if(gameworld){						
				GameApplication.app.connection.call("gamemanager.updateitems", null, gameworld.roomID, gameworld.getGameWorldObjects());
				}
				}else if(action["type"] == GameActionType.UPDATE_GWITEMS){
				gameworld && gameworld.updateGameWorldObjects(action["items"]);
				}*/
			}else{
				GameApplication.app.navigator.goHome();
				GameApplication.app.popuper.showInfoPopUp("Произошла ошибка. Код ошибки 222. Сообщите об этом разработчикам!");
			}
		}
		
		public function exitGame():void{
			if(gameworld){
				GameApplication.app.gameContainer.chat.finishedpanel.clearPanel();
				GameApplication.app.actionShowerMenu.hideMenu();
				GameApplication.app.useitemingamemanager.clear();
				
				timer.removeEventListener(TimerEvent.TIMER, timerHandler);
				timer.stop();
				
				gameworld.destroyWorld();
				GameApplication.app.navigator.goHome();
				gameworld = null;
				
				gameMode = false;
			}
		}
		
		private function timerHandler(e:TimerEvent):void{
			dispatchEvent(new GameManagerTimerEvent(GameManagerTimerEvent.TIMER_UPDATE, (timeround * 1000 - (e.target as Timer).currentCount * (e.target as Timer).delay) / 1000));
		}
		
		public function goToLeft(down:Boolean, _x:Number, _y:Number, _lvx:Number, _lvy:Number):void{			
			GameApplication.app.connection.call("gamemanager.gotoleft", null, gameworld.roomID, down, _x, _y, _lvx, _lvy);
		}
		public function goToRight(down:Boolean, _x:Number, _y:Number, _lvx:Number, _lvy:Number):void{			
			GameApplication.app.connection.call("gamemanager.gotoright", null, gameworld.roomID, down, _x, _y, _lvx, _lvy);
		}
		public function jump(down:Boolean, _x:Number, _y:Number, _lvx:Number, _lvy:Number):void{
			GameApplication.app.connection.call("gamemanager.jump", null, gameworld.roomID, down, _x, _y, _lvx, _lvy);
		}
		public function findsource(_x:Number, _y:Number):void{			
			GameApplication.app.connection.call("gamemanager.findsource", null, gameworld.roomID, _x, _y);
		}
		public function findexit(_x:Number, _y:Number):void{			
			GameApplication.app.connection.call("gamemanager.findexit", null, gameworld.roomID, _x, _y);
		}
		public function userout():void{	
			GameApplication.app.connection.call("gamemanager.userout", null, gameworld.roomID);
		}
		public function userexit():void{	
			GameApplication.app.connection.call("gamemanager.userexit", null, gameworld.roomID);
		}
		
		public function endTestGame():void{		
			GameApplication.app.navigator.goHome();
			GameApplication.app.connection.call("startbonus.getStartBonus", new Responder(onGetStartBonus, null));			
		}
		private function onGetStartBonus(e:*):void{
			GameApplication.app.navigator.goHome();
			GameApplication.app.popuper.show(new PopUpStartBonus());
		}
	}
}