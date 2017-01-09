package utils.shop.item
{
	public class ItemPresent extends Item
	{
		public var presenter:String;
		public function ItemPresent(id:int, prototypeid:int, price:int, title:String, description:String, count:int, categoryid:int, presenter:String)
		{
			super(id, prototypeid, price, title, description, count, categoryid);
			this.presenter = presenter;
		}
	}
}