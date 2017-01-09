package utils.game
{
	import flash.events.Event;
	
	public class GameContactListenerEvent extends Event
	{
		public static var CHEESE:String = "CHEESE";
		public static var MINK:String = "MINK";
		public static var CONTACTGROUND:String = "CONTACTGROUND";
		
		public function GameContactListenerEvent(type:String)
		{
			super(type, false, false);
		}
	}
}