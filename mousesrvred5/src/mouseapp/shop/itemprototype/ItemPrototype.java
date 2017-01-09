package mouseapp.shop.itemprototype;

public class ItemPrototype {
	public int id;
	public String title;
	public String description;
	public int count;
	public int price;
	public int categoryid;
	public int showed;
	
	public ItemPrototype(int id, String title, String description, int count, int price, int categoryid, int showed){
		this.id = id;
		this.title = title;
		this.description = description;
		this.count = count;
		this.price = price;
		this.categoryid = categoryid;
		this.showed = showed;
	}
}
