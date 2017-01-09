package utils.managers.useitemingame
{
	import Box2D.Dynamics.b2Body;
	
	import application.GameApplication;
	import application.gamecontainer.scene.catalog.article.ArticleMovieClass;
	
	import flash.display.MovieClip;
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Rectangle;
	import flash.ui.Keyboard;
	
	import flex.lang.reflect.Field;
	
	import mx.core.UIComponent;
	
	import utils.chat.message.MessageType;
	import utils.chat.room.Room;
	import utils.shop.item.Item;
	import utils.shop.item.ItemType;
	
	public class UseItemInGameManager extends EventDispatcher
	{
		private var _gameitemmc:MovieClip;
		private var _curritem:Item;
		private var _currentpid:int;
		
		public function UseItemInGameManager(target:IEventDispatcher=null)
		{
			super(target);
		}
		
		public function useItem(item:Item):void{			
			_curritem = item;
			
			addListeners();
				
			var classMovie:Class = ArticleMovieClass.getGameItemClass(item.prototypeid);
			_gameitemmc = new classMovie();
			var bounds:Rectangle = ArticleMovieClass.getGameItemBounds(item.prototypeid);
			_gameitemmc.width = bounds.width;
			_gameitemmc.height = bounds.height;
			GameApplication.app.gamemanager.gameworld.gameUI.addChild(_gameitemmc);
			updateXY();
		}
		
		private function addListeners():void{
			GameApplication.app.stage.addEventListener(MouseEvent.CLICK, onClick, false, 0, true);
			GameApplication.app.stage.addEventListener(MouseEvent.MOUSE_MOVE, onMouseMove, false, 0, true);
			GameApplication.app.stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown, false, 0, true);		
		}
		
		public function useshopitem(prototypeid:int):void{
			if(prototypeid != ItemType.FREEZE && prototypeid != ItemType.ANTIFREEZE){
				_currentpid = prototypeid;
				
				addListeners();
				
				var classMovie:Class = ArticleMovieClass.getGameItemClass(prototypeid);
				_gameitemmc = new classMovie();
				var bounds:Rectangle = ArticleMovieClass.getGameItemBounds(prototypeid);
				_gameitemmc.width = bounds.width;
				_gameitemmc.height = bounds.height;
				GameApplication.app.gamemanager.gameworld.gameUI.addChild(_gameitemmc);
				updateXY();
			}else{
				GameApplication.app.useitemmanager.useShopGameItem(prototypeid, 0, 0, 0);
			}
		}
		
		private function onKeyDown(e:KeyboardEvent):void{			
			if (e.keyCode == 27){
				clear();
			}
		}
		
		private function onClick(e:MouseEvent):void{
			if ((_gameitemmc.x - _gameitemmc.width / 2 > 0) && (_gameitemmc.x + _gameitemmc.width / 2 < GameApplication.app.gameContainer.scene.width) &&
				(_gameitemmc.y - _gameitemmc.height / 2 > 0) && (_gameitemmc.y + _gameitemmc.height / 2 < GameApplication.app.gameContainer.scene.height))
			{		
				if (_currentpid == ItemType.MAGIC_HAND){
					var isfind:Boolean = false;
					var gwitemID:int;
					for (var id:String in GameApplication.app.gamemanager.gameworld.createdItems){
						if (_gameitemmc.hitTestObject((GameApplication.app.gamemanager.gameworld.createdItems[int(id)] as b2Body).GetUserData())){
							isfind = true;
							gwitemID = int(id);
						}
					}
					if(!isfind){
						sendWarningMsg("Вы можете убирать только предметы, которые поставили пользователи");						
					}else{
						GameApplication.app.useitemmanager.useShopGameItem(_currentpid, gwitemID, _gameitemmc.x, _gameitemmc.y);
					}
				}else if (_currentpid == ItemType.GUN){					
					var _gwuserID:int = -1;
					for (var iduser:String in GameApplication.app.gamemanager.gameworld.users){
						if (_gameitemmc.hitTestObject((GameApplication.app.gamemanager.gameworld.users[int(iduser)] as b2Body).GetUserData())){							
							_gwuserID = int(iduser);
							break;
						}
					}					
					GameApplication.app.useitemmanager.useShopGameItem(_currentpid, _gwuserID, _gameitemmc.x, _gameitemmc.y);
				}else{	
					var gameUI:UIComponent = GameApplication.app.gamemanager.gameworld.gameUI;
					for(var i:int = 0; i < gameUI.numChildren; i++){					
						if (gameUI.getChildAt(i) is MovieClip){
							if (_gameitemmc.hitTestObject(gameUI.getChildAt(i))){
								if(Object(gameUI.getChildAt(i)).constructor == MinkSkin || Object(gameUI.getChildAt(i)).constructor == CheeseSkin){
									return;
								}
							}
						}						
					}
					if (_curritem != null){
						GameApplication.app.useitemmanager.userGameItem(_curritem, _gameitemmc.x, _gameitemmc.y);					
					}else{
						GameApplication.app.useitemmanager.useShopGameItem(_currentpid, 0, _gameitemmc.x, _gameitemmc.y);
					}
				}
			}
			//clear();				
		}
		
		private function sendWarningMsg(msg:String):void{
			var message:Object = new Object();
			message["from"] = GameApplication.app.userinfomanager.myuser;
			message["type"] = MessageType.USEITEM;
			message["text"] = msg;
			var room:Room = GameApplication.app.gameContainer.chat.getRoom(GameApplication.app.gamemanager.gameworld.roomID);
			room.addMessage(message);
		}
		
		public function clear():void{			
			if(_curritem || _currentpid){
				GameApplication.app.stage.removeEventListener(MouseEvent.CLICK, onClick);
				GameApplication.app.stage.removeEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
				GameApplication.app.stage.removeEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
				
				if (_gameitemmc != null && GameApplication.app.gamemanager.gameworld.gameUI.contains(_gameitemmc)){
					GameApplication.app.gamemanager.gameworld.gameUI.removeChild(_gameitemmc);
				}
				_gameitemmc = null;
				_curritem = null;
			}
		}
		
		private function onMouseMove(e:MouseEvent):void{
			updateXY();
		}
		
		private function updateXY():void{
			if (_gameitemmc){
				_gameitemmc.x = GameApplication.app.gamemanager.gameworld.gameUI.mouseX;
				_gameitemmc.y = GameApplication.app.gamemanager.gameworld.gameUI.mouseY;
			}
		}
	}
}