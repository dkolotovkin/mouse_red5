package mouseapp.utils.sort;

import java.util.Comparator;

import mouseapp.shop.itemprototype.ItemPrototype;

public class SortShopItems implements Comparator<ItemPrototype> {
	public int compare(ItemPrototype item1, ItemPrototype item2){
		if(item1.id > item2.id){
			return 1;
		}else if(item1.id < item2.id){
			return -1;
		}
		return 0;
	}
}
