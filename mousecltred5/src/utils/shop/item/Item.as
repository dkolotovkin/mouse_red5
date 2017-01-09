package utils.shop.item
{
	import utils.shop.itemprototype.ItemPrototype;

	public class Item
	{
		public var id:int;
		public var prototypeid:int;
		public var price:int;
		public var title:String;
		public var description:String;
		[Bindable]
		public var count:int;
		public var categoryid:int;
		
		public function Item(id:int, prototypeid:int, price:int, title:String, description:String, count:int, categoryid:int){
			this.id = id;
			this.prototypeid = prototypeid;
			this.price = price;
			this.title = title;
			this.description = description;
			this.count = count;			
			this.categoryid = categoryid;
		}
		
		public static function createFromItemPrototype(ip:ItemPrototype):Item{
			return new Item(ip.id, ip.id, ip.price, ip.title, ip.description, ip.count, ip.categoryid);
		} 
		
		public function clone():Item{
			return new Item(id, prototypeid, price, title, description, count, categoryid);
		}
	}
}