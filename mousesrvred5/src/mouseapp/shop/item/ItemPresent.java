package mouseapp.shop.item;

public class ItemPresent extends Item {
	public String presenter;
	
	public ItemPresent(int id, int prototypeid, int price, String title, String description, int count, int categoryid, String presenter){
		super(id, prototypeid, price, title, description, count, categoryid);
		this.presenter = presenter;
	}
}
