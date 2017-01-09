package utils.managers.useitem
{
	import application.GameApplication;
	
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.net.Responder;
	
	import utils.shop.BuyResultCode;
	import utils.shop.CategoryType;
	import utils.shop.UseResultCode;
	import utils.shop.item.Item;
	
	public class UseItemManager extends EventDispatcher
	{
		public function UseItemManager(target:IEventDispatcher=null)
		{
			super(target);
		}
	
		public function useItem(item:Item):void{			
			if (item.categoryid == CategoryType.GAME_INVENTORY){
				GameApplication.app.popuper.showInfoPopUp("Этот предмет вы можете использовать только во время игры.");
			}else{
				GameApplication.app.connection.call("shopmanager.useItem", new Responder(onUseItem, onErrorUseItem), item.id);
			}			
		}
		
		public function userGameItem(item:Item, itemx:Number, itemy:Number):void{
			if (item.categoryid == CategoryType.GAME_INVENTORY){
				if (GameApplication.app.gamemanager.gameMode != true){
					GameApplication.app.popuper.showInfoPopUp("Этот предмет вы можете использовать только во время игры.");
				}else{
					GameApplication.app.connection.call("shopmanager.useGameItem", new Responder(onUseItem, onErrorUseItem), item.id, GameApplication.app.gamemanager.gameworld.roomID, itemx, itemy);
				}
			}else{
				GameApplication.app.connection.call("shopmanager.useItem", new Responder(onUseItem, onErrorUseItem), item.id);
			}
		}		
	
		private function onUseItem(result:Object):void{
			if (result["error"] == UseResultCode.HEALING_OK){
				GameApplication.app.popuper.showInfoPopUp("Вы успешно восстановили энергию!");
			}else if (result["error"] == UseResultCode.GAMEACTION_OK){				
			}else{
				GameApplication.app.popuper.showInfoPopUp("Ошибка 567 при использовании предмета. Сообщите об ошибке разработчикам.");
			}			
			dispatchEvent(new UseItemManagerEvent(UseItemManagerEvent.CHANGE_COUNT, result["itemid"], result["count"]));			
		}
		
		private function onErrorUseItem(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при использовании предмета. Сообщите об ошибке разработчикам.");
		}
		
		public function useShopGameItem(prototypeid:int, gwitemid:int, itemx:Number, itemy:Number):void{
			var roomID:int = 0;
			if (GameApplication.app.gamemanager.gameworld){
				roomID = GameApplication.app.gamemanager.gameworld.roomID;
			}
			GameApplication.app.connection.call("shopmanager.buyUseShopItem", new Responder(onUseShopItem, onErrorUseShopItem), roomID, prototypeid, gwitemid, itemx, itemy);
		}
		
		private function onUseShopItem(result:Object):void{
			if (result["error"] == BuyResultCode.NOT_ENOUGH_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У Вас недостаточно денег!");
			}else if (result["error"] == BuyResultCode.OK){				
			}else{
				GameApplication.app.popuper.showInfoPopUp("Ошибка при использовании предмета.");
			}	
		}
		
		private function onErrorUseShopItem(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при использовании предмета. Сообщите об ошибке разработчикам.");
		}
		
		
		public function saleItem(item:Item):void{
			GameApplication.app.connection.call("shopmanager.saleItem", new Responder(onSaleItem, onSaleItemError), item.id);
		}		
		
		private function onSaleItem(result:Object):void{
			if (result["error"] == UseResultCode.GAMEACTION_OK){				
			}else{
				GameApplication.app.popuper.showInfoPopUp("Невозможно продать вещь");
			}			
			dispatchEvent(new UseItemManagerEvent(UseItemManagerEvent.CHANGE_COUNT, result["itemid"], result["count"]));			
		}
		
		private function onSaleItemError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Невозможно продать вещь");
		}
	}
}