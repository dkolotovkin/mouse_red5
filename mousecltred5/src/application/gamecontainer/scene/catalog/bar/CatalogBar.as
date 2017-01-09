package application.gamecontainer.scene.catalog.bar
{
	import application.gamecontainer.scene.catalog.bar.tab.CatalogTab;
	import application.gamecontainer.scene.catalog.bar.tab.CatalogTabEvent;
	
	import spark.components.HGroup;
	import spark.layouts.VerticalAlign;
	
	import utils.selector.Selector;
	import utils.selector.SelectorEvent;
	import utils.shop.CategoryType;
	import utils.shop.category.ShopCategory;
	
	public class CatalogBar extends HGroup
	{	
		public var hash:Object = new Object ();
		private var _tabs:Array = new Array();
		private var _selector:Selector = new Selector ();
		
		public function CatalogBar() {
			verticalAlign = VerticalAlign.BOTTOM;
			
			_selector.addEventListener(SelectorEvent.SELECTED, onSelected, false, 0, true);
			_selector.addEventListener(SelectorEvent.UNSELECTED, onUnselected, false, 0, true);
		}
		
		private function onUnselected(event : SelectorEvent) : void {
		}
		
		private function onSelected(event : SelectorEvent) : void {					
		}
		
		public function showGroups(list : Array, bag:Boolean) : void {			
			for each (var category : ShopCategory in list) {				
				if ((category.id == CategoryType.PRESENTS && bag) ||
					(category.id == CategoryType.ARTEFACTS && bag) ||
					((category.id == CategoryType.MOUSES ||
					category.id == CategoryType.ACCESSORIES) && !bag)){
					var tab:CatalogTab = new CatalogTab();
					tab.init(category);
					addTab(tab);
				}
			}			
		}
		
		public function addTab (tab:CatalogTab):void {
			if (!hash[tab.category.id]){
				hash[tab.category.id] = tab;
				_tabs.push(tab);
				tab.addEventListener(CatalogTabEvent.SELECTED, onTabSelected, false, 0, true);
				tab.addEventListener(CatalogTabEvent.UNSELECTED, onTabUnselected, false, 0, true);
				addElement(tab);
			}
		}
		
		private function onTabSelected(event : CatalogTabEvent) : void {
			_selector.selected(event.tab);
			dispatchEvent(event.clone());
		}
		
		private function onTabUnselected(event : CatalogTabEvent) : void {
			_selector.unselected(event.tab);
			dispatchEvent(event.clone());
		}
		
		public function removeTab (id:int):void {
			var tab:CatalogTab = hash[id];
			if (tab){
				for (var i : uint = 0, len:uint = _tabs.length; i <  len; i++) {
					if ((_tabs[i] as CatalogTab).category.id == id){
						_tabs.splice(i, 1);
						break;
					}
				}
				tab.removeEventListener(CatalogTabEvent.SELECTED, onTabSelected);
				tab.removeEventListener(CatalogTabEvent.UNSELECTED, onTabUnselected);
				removeElement(tab);
			}
		}
	}
}