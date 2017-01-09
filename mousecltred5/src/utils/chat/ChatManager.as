package utils.chat
{
	import application.GameApplication;
	import application.components.popup.bet.request.PopUpBetsGameRequest;
	import application.components.popup.claninviteme.PopUpClanInviteMe;
	import application.components.popup.fortuna.PopUpMoneyPrize;
	import application.components.popup.help.changeinfo.PopUpHelpChangeInfo;
	import application.components.popup.newlevel.PopUpNewLevel;
	import application.components.popup.winjp.PopUpWinJackPot;
	import application.gamecontainer.scene.minigames.auction.Auction;
	import application.gamecontainer.scene.minigames.victorina.VictorinaEvent;
	
	import flash.events.EventDispatcher;
	import flash.utils.clearInterval;
	
	import mx.controls.Alert;
	
	import utils.chat.message.MessageType;
	import utils.chat.room.Room;
	import utils.jackpot.JackPotEvent;
	import utils.user.ClanUserRole;

	public class ChatManager extends EventDispatcher
	{		
		public var commonroom:Room;
		
		public function ChatManager()
		{
		}
		
		public function exitGame():void{
			commonroom = null;
		}
		
		public function processMassage(message:Object):void{			
			var room:Room = GameApplication.app.gameContainer.chat.getRoom(message["roomId"]);			
			if (message["type"] == MessageType.USER_IN){				
				if (message["initiator"]["id"] == GameApplication.app.userinfomanager.myuser.id){					
					room = new Room(message["roomId"], message["roomTitle"], message["users"], message["messages"]);					
					GameApplication.app.gameContainer.chat.addRoom(room);
					GameApplication.app.gameContainer.chat.activeRoom = room;
					if(commonroom == null && message["roomId"] == 1) commonroom = room;
				}else{					
					if (room) {
						room.addUser(message["initiator"]);
					} else {
						//Alert.show("Пользователь вошел в несуществующую комнату!", "Ошибка");
					}					
				}
			}else if (message["type"] == MessageType.USER_OUT){
				if (message["initiatorId"] == GameApplication.app.userinfomanager.myuser.id){
					if(room){						
						GameApplication.app.gameContainer.chat.removeRoom(room);					
					}
				}else{
					if (room) {
						room.removeUser(message["initiatorId"]);
					} else {
						//Alert.show("Пользователь вышел из несуществующей комнаты!", "Ошибка");
					}					
				}
			}else if (message["type"] == MessageType.MESSAGE || message["type"] == MessageType.BAN || message["type"] == MessageType.BAN_OUT || message["type"] == MessageType.SYSTEM || message["type"] == MessageType.PRESENT || message["type"] == MessageType.VICTORINA) {
				if (room) {
					if(message["type"] == MessageType.VICTORINA){
						dispatchEvent(new VictorinaEvent(VictorinaEvent.TASK_UPDATE, message["text"]));
					}
					room.addMessage(message, true);
				} else {					
					var proom : Room = GameApplication.app.gameContainer.chat.getRoom(message["roomId"]);
					if (proom) {
						proom.addMessage(message, true);
					} else {
						//Alert.show("Нет комнаты " + message["roomId"] + ". Cообщиете об ошибке разработчикам!", "Ошибка");
					}
				}
			}else if (message["type"] == MessageType.PRIVATE) {				
				GameApplication.app.gameContainer.chat.addPrivateMessage(message);
			}else if (message["type"] == MessageType.CHANGEINFO) {					
				GameApplication.app.userinfomanager.changeMyParams(message);
			}else if (message["type"] == MessageType.JACK_POT) {
				GameApplication.app.jackpotmanager.updateJackPot(int(message["money"]));
			}else if (message["type"] == MessageType.JACK_POT_WIN) {
				var msg:Object = new Object();				
				msg["type"] = MessageType.JACK_POT_WIN;
				msg["text"] = "Джек пот разыгран! Побетитель: " + message["winner"] + ". Размер выйгрыша: " + int(message["money"]) + " евро!";
				room.addMessage(msg);			
				//GameApplication.app.popuper.show(new PopUpWinJackPot(int(message["money"]), message["winner"]));				
			}else if (message["type"] == MessageType.CLAN_INVITE){
				GameApplication.app.popuper.show(new PopUpClanInviteMe(message["claninfo"]["ownertitle"], message["claninfo"]["title"]));
			}else if (message["type"] == MessageType.CLAN_KICK){
				GameApplication.app.userinfomanager.myuser.claninfo.clanid = 0;
				GameApplication.app.userinfomanager.myuser.claninfo.clandeposite = 0;
				GameApplication.app.userinfomanager.myuser.claninfo.clandepositm = 0;
				GameApplication.app.userinfomanager.myuser.claninfo.clanrole = ClanUserRole.NO_ROLE;
				GameApplication.app.popuper.showInfoPopUp("Вас выгнали из клана.");
			}else if (message["type"] == MessageType.NEW_LEVEL) {				
				GameApplication.app.popuper.show(new PopUpNewLevel(message["level"], message["prize"]));
			}else if (message["type"] == MessageType.BET_GAME_REQUEST) {				
				GameApplication.app.popuper.show(new PopUpBetsGameRequest(message["userid"], message["roomId"]));
			}else if (message["type"] == MessageType.AUCTION_STATE) {				
				if(GameApplication.app.navigator.currentSceneContent && 
					(GameApplication.app.navigator.currentSceneContent is Auction)){
					(GameApplication.app.navigator.currentSceneContent as Auction).update(message["bank"], message["passtime"], message["minbet"], message["winnerid"]);
				}
			}else if (message["type"] == MessageType.AUCTION_PRIZE) {				
				GameApplication.app.popuper.show(new PopUpMoneyPrize(message["prize"]));
			}else if (message["type"] == MessageType.BEST_HOUR) {				
				GameApplication.app.popuper.show(new PopUpMoneyPrize(message["prize"], "Вы лучший игрок за час!"));
			}else if (message["type"] == MessageType.BEST_DAY) {				
				GameApplication.app.popuper.show(new PopUpMoneyPrize(message["prize"], "Вы лучший игрок за день!"));
			}else if (message["type"] == MessageType.START_INFO) {			
				GameApplication.app.popuper.show(new PopUpHelpChangeInfo());
			}
		}
	}
}