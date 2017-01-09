package application.components.popup.buy
{
	import application.components.popup.PopUpTitled;
	
	import utils.shop.item.Item;
	import utils.shop.itemprototype.ItemPrototype;
	
	public class PopUpBuy extends PopUpTitled
	{
		private var _buyInfo:BuyInfo;
		private var _prototype:ItemPrototype;
		
		public function PopUpBuy(prototype:ItemPrototype, description:String = "Поздравляем с успешной покупкой!"){
			super();
			priority = 31;
			_prototype = prototype;
			_buyInfo = new BuyInfo();
			_buyInfo.description.text = description;
		}
		
		override protected function createChildren():void{
			super.createChildren();
			_buyInfo.article.init(Item.createFromItemPrototype(_prototype));
			addElement(_buyInfo);
		}
	}
}