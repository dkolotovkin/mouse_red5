package utils.shop
{
	import application.GameApplication;
	import application.components.popup.banoff.PopUpBanOff;
	import application.components.popup.buy.PopUpBuy;
	import application.gamecontainer.scene.bag.article.BagInGameSmallArticle;
	
	import flash.net.Responder;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import utils.shop.artefact.Artefact;
	import utils.shop.category.ShopCategory;
	import utils.shop.item.Item;
	import utils.shop.item.ItemPresent;
	import utils.shop.itemprototype.ItemPrototype;

	public class ShopManager
	{
		private var _callBackCategories:Function;
		private var _callBackItemPrototypes:Function;
		private var _callBackItems:Function;
		private var _callBackArtefact:Function;
		
		private var _currentUrl:String;
		
		public var gameinventory:Array;
		
		public function ShopManager(){						
		}
		
		public function init():void{
			GameApplication.app.connection.call("shopmanager.getItemPrototypesByCategoryID", new Responder(onGetGameInventoryItems, onGetGameInventoryItemsError), CategoryType.GAME_INVENTORY);
		}
		
		private function onGetGameInventoryItems(items:Array):void{
			gameinventory = new Array();
			for(var i:int = 0; i < items.length; i++){
				var article:BagInGameSmallArticle = new BagInGameSmallArticle();
				var itemprototype:ItemPrototype = new ItemPrototype(items[i]["id"], items[i]["title"], items[i]["description"], items[i]["count"], items[i]["price"], items[i]["categoryid"], items[i]["showed"]);
				article.init(itemprototype);
				gameinventory.push(article);			
			}
			GameApplication.app.gameContainer.chat.bagingamesmall.setitems();
		}		
		private function onGetGameInventoryItemsError(err:Object):void{			
		}
		
		public function getShopCategories(callback:Function = null):void{
			_callBackCategories = callback;			
			GameApplication.app.connection.call("shopmanager.getShopCategories", new Responder(onGetCategories, onGetCategoriesError));
		}
		
		private function onGetCategories(categories:Array):void{
			var list:Array = new Array();			
			for(var i:uint = 0; i < categories.length; i++){
				var category:ShopCategory = new ShopCategory(categories[i]["id"], categories[i]["title"]);				
				list.push(category);				
			}
			_callBackCategories && _callBackCategories(list);			
			_callBackCategories = null;
		}
		private function onGetCategoriesError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при получении категорий. Сообщите об ошибке разработчикам.");
		}
		
		public function getItemPrototypes(categoryID:uint = 0, callback:Function = null):void{			
			_callBackItemPrototypes = callback;
			GameApplication.app.connection.call("shopmanager.getItemPrototypesByCategoryID", new Responder(ongetItemPrototypes, ongetItemPrototypesError), categoryID);
		}
		private function ongetItemPrototypes(items:Array):void{			
			var itemprototypes:Array = new Array();
			for(var i:uint = 0; i < items.length; i++){
				var itemprototype:ItemPrototype = new ItemPrototype(items[i]["id"], items[i]["title"], items[i]["description"], items[i]["count"], items[i]["price"], items[i]["categoryid"], items[i]["showed"]);
				itemprototypes.push(itemprototype);
			}			
			_callBackItemPrototypes && _callBackItemPrototypes(itemprototypes);
			_callBackItemPrototypes = null;
		}
		private function ongetItemPrototypesError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при получении вещей. Сообщите об ошибке разработчикам.");
		}
		
		public function buyItem(prototypeID:int = -1):void{			
			GameApplication.app.connection.call("shopmanager.buyItem", new Responder(onBuyItem, onBuyItemError), prototypeID);
		}
		
		public function buyUseItem(prototypeID:int = -1):void{			
			GameApplication.app.connection.call("shopmanager.buyUseItem", new Responder(onBuyItem, onBuyItemError), prototypeID);
		}
		
		private function onBuyItem(buyresult:Object):void{			
			trace("buyresult: " + buyresult["error"]);
			if (buyresult["error"] == BuyResultCode.OK){
				var itemprototype:ItemPrototype = new ItemPrototype(buyresult["itemprototype"]["id"], buyresult["itemprototype"]["title"], buyresult["itemprototype"]["description"], buyresult["itemprototype"]["count"], buyresult["itemprototype"]["price"], buyresult["itemprototype"]["categoryid"], buyresult["itemprototype"]["showed"]);
				GameApplication.app.popuper.show(new PopUpBuy(itemprototype));
			}else if (buyresult["error"] == BuyResultCode.NOT_ENOUGH_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У Вас не достаточно денег для этой покупки.");
			}else if (buyresult["error"] == BuyResultCode.NOT_PROTOTYPE){
				GameApplication.app.popuper.showInfoPopUp("Невозможно купить эту вещь.");
			}else if (buyresult["error"] == BuyResultCode.SQL_ERROR){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 756. Сообщите об ошибке разработчикам.");
			}else if (buyresult["error"] == BuyResultCode.OTHER){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 796. Сообщите об ошибке разработчикам.");
			}
		}
		private function onBuyItemError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке вещи. Сообщите об ошибке разработчикам.");
		}
		
		public function buyPresent(prototypeID:int, userID:int):void{			
			GameApplication.app.connection.call("shopmanager.buyPresent", new Responder(onBuyPresent, onBuyPresentError), prototypeID, userID);
		}
		
		private function onBuyPresent(buyresult:Object):void{			
			trace("buyresult: " + buyresult["error"]);
			if (buyresult["error"] == BuyResultCode.OK){
				var itemprototype:ItemPrototype = new ItemPrototype(buyresult["itemprototype"]["id"], buyresult["itemprototype"]["title"], buyresult["itemprototype"]["description"], buyresult["itemprototype"]["count"], buyresult["itemprototype"]["price"], buyresult["itemprototype"]["categoryid"], buyresult["itemprototype"]["showed"]);
				GameApplication.app.popuper.show(new PopUpBuy(itemprototype, "Подарок доставлен!"));
			}else if (buyresult["error"] == BuyResultCode.NOT_ENOUGH_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У Вас не достаточно денег для этой покупки.");
			}else if (buyresult["error"] == BuyResultCode.NOT_PROTOTYPE){
				GameApplication.app.popuper.showInfoPopUp("Невозможно купить эту вещь.");
			}else if (buyresult["error"] == BuyResultCode.SQL_ERROR){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при отправке подарка.");
			}else if (buyresult["error"] == BuyResultCode.OTHER){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при отправке подарка.");
			}
		}
		private function onBuyPresentError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при отправке подарка.");
		}
		
		public function getArtefacts(callBackArtefact:Function = null):void{
			_callBackArtefact = callBackArtefact;
			GameApplication.app.connection.call("shopmanager.getArtefact", new Responder(onGetArtefact, onGetArtefactError));
		}
		private function onGetArtefact(arts:Array):void{
			var artsArr:Array = new Array();
			for(var i:uint = 0; i < arts.length; i++){	
				var currentArtefact:Object = GameApplication.app.petmanager.artefacts[arts[i]["type"]];
				var art:Artefact = new Artefact(arts[i]["id"], currentArtefact["id"], currentArtefact["title"], currentArtefact["description"], currentArtefact["experience"], currentArtefact["popular"]);
				
				artsArr.push(art);
			}
			_callBackArtefact && _callBackArtefact(artsArr);
			_callBackArtefact = null;
		}
		private function onGetArtefactError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при обращению к серверу.");
		}
		
		public function getUserItemsByCategoryID(categoryID:uint = -1, callBackItems:Function = null):void{
			_callBackItems = callBackItems;
			GameApplication.app.connection.call("shopmanager.getUserItemsByCategoryID", new Responder(ongetUserItems, ongetUserItemsError), categoryID);
		}
		private function ongetUserItems(items:Array):void{
			var itemsArr:Array = new Array();
			for(var i:uint = 0; i < items.length; i++){				
				var item:ItemPresent = new ItemPresent(items[i]["id"], items[i]["prototypeid"], items[i]["price"], items[i]["title"], items[i]["description"], items[i]["count"], items[i]["categoryid"], items[i]["presenter"]);
				itemsArr.push(item);
			}			
			_callBackItems && _callBackItems(itemsArr);
			_callBackItems = null;
		}
		private function ongetUserItemsError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при получении вещей пользователя. Сообщите об ошибке разработчикам.");
		}
		
		public function buyexperience(exp:int):void{
			GameApplication.app.connection.call("shopmanager.buyExperience", new Responder(onBuyexperience, onBuyexperienceError), exp);
		}
		
		private function onBuyexperience(buyresult:Object):void{
			if (buyresult["error"] == BuyResultCode.OK){
				GameApplication.app.popuper.showInfoPopUp("Поздравляем с удачной покупкой опыта!");
			}else if (buyresult["error"] == BuyResultCode.NOT_ENOUGH_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У Вас не достаточно денег для этой покупки.");
			}else if (buyresult["error"] == BuyResultCode.SQL_ERROR){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 756. Сообщите об ошибке разработчикам.");
			}else if (buyresult["error"] == BuyResultCode.OTHER){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 791. Сообщите об ошибке разработчикам.");
			}
		}
		private function onBuyexperienceError(result:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке опыта.");
		}
		
		public function buyLink(url:String):void{
			_currentUrl = url;
			GameApplication.app.connection.call("shopmanager.buyLink", new Responder(onBuyLink, onBuyLinkError));
		}
		
		private function onBuyLink(buyresult:Object):void{
			if (buyresult["error"] == BuyResultCode.OK){
				if (_currentUrl && _currentUrl.length){
					var request:URLRequest = new URLRequest(_currentUrl);
					try {
						navigateToURL(request, '_blank');
					} catch (e:Error) {
						trace("Error occurred!");
					}
					_currentUrl = null;
				}				
			}else if (buyresult["error"] == BuyResultCode.NOT_ENOUGH_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У Вас не достаточно денег для этой покупки.");
			}else if (buyresult["error"] == BuyResultCode.SQL_ERROR){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 756. Сообщите об ошибке разработчикам.");
			}else if (buyresult["error"] == BuyResultCode.OTHER){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 791. Сообщите об ошибке разработчикам.");
			}
		}
		private function onBuyLinkError(result:Object):void{
			_currentUrl = null;
			GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке ссылки.");
		}
		
		
		//РАБОТА С БАНОМ
		public function showBanPrice():void{			
			GameApplication.app.connection.call("shopmanager.getPriceBanOff", new Responder(ongetPriceBanOff, ongetPriceBanOffError));
		}		
		private function ongetPriceBanOff(price:int):void{
			GameApplication.app.popuper.show(new PopUpBanOff(price));
		}
		private function ongetPriceBanOffError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при запросе цены на выход из бана.");
		}
		
		public function buyBanOff():void{
			GameApplication.app.connection.call("shopmanager.buyBanOff", new Responder(onbuyBanOff, onbuyBanOffError));
		}
		
		private function onbuyBanOff(buyresult:Object):void{
			if (buyresult["error"] == BuyResultCode.OK){
				GameApplication.app.banmanager.banoff();	
			}else if (buyresult["error"] == BuyResultCode.NOT_ENOUGH_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У Вас не достаточно денег для этой покупки.");
			}else if (buyresult["error"] == BuyResultCode.SQL_ERROR){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 756. Сообщите об ошибке разработчикам.");
			}else if (buyresult["error"] == BuyResultCode.OTHER){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 791. Сообщите об ошибке разработчикам.");
			}
		}
		private function onbuyBanOffError(result:Object):void{			
			GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке выхода из бана.");
		}
	}
}