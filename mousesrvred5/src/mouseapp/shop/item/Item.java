package mouseapp.shop.item;

public class Item {
	public int id;	
	public int prototypeid;
	public int price;
	public String title;
	public String description;	
	public int count;
	public int categoryid;
	
	public Item(int id, int prototypeid, int price, String title, String description, int count, int categoryid){
		this.id = id;
		this.prototypeid = prototypeid;
		this.price = price;
		this.title = title;
		this.description = description;
		this.count = count;		
		this.categoryid = categoryid;
	}
}
