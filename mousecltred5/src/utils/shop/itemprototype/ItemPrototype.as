package utils.shop.itemprototype
{
	public class ItemPrototype
	{
		public var id:int;
		public var title:String;
		public var description:String;
		public var count:int;
		public var price:int;
		public var categoryid:int;
		public var showed:int;
		
		public function ItemPrototype(id:int, title:String, description:String, count:int, price:int, categoryid:int, showed:int){
			this.id = id;
			this.title = title;
			this.description = description;
			this.count = count;
			this.price = price;
			this.categoryid = categoryid;
			this.showed = showed;
		}
	}
}