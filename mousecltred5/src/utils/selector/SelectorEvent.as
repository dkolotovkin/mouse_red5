package utils.selector {
	import flash.events.Event;

	/**
	 * @author ivaskov
	 */
	public class SelectorEvent extends Event {
		
		
		public static const SELECTED:String = "selected";
		
		
		public var item:ISelected;
		
		public function SelectorEvent(type : String,item:ISelected) {
			super(type, false,false);
			this.item = item;
		}
	}
}