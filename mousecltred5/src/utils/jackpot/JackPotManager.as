package utils.jackpot
{
	import flash.events.EventDispatcher;

	public class JackPotManager extends EventDispatcher
	{
		[Bindable]
		public var jackPot:int;
		public function JackPotManager()
		{
		}
		
		public function updateJackPot(money:int):void{			
			jackPot = money;
		}
	}
}