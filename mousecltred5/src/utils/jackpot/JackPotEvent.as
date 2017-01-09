package utils.jackpot
{
	import flash.events.Event;
	
	public class JackPotEvent extends Event
	{
		public static var CHANGE:String = "CHANGE";
		
		public var money:int;
		
		public function JackPotEvent(type:String, money:int)
		{
			super(type, false, false);
			this.money = money;
		}
	}
}