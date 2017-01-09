package utils.managers.useitem
{
	import flash.events.Event;
	
	public class UseItemManagerEvent extends Event
	{
		public static var CHANGE_COUNT:String = "CHANGE_COUNT";
		
		public var itemid:int;
		public var count:int;
		
		public function UseItemManagerEvent(type:String, itemid:int, count:int)
		{
			super(type, false, false);
			this.itemid = itemid;
			this.count = count;
		}
		
		override public function clone() : Event {
			return new UseItemManagerEvent(type, itemid, count);
		}
	}
}