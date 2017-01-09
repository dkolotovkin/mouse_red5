package application.gamecontainer.scene.minigames.victorina
{
	import flash.events.Event;
	
	public class VictorinaEvent extends Event
	{
		public static var TASK_UPDATE:String = "TASK_UPDATE";
		
		public var task:String;
		
		public function VictorinaEvent(type:String, t:String)
		{
			super(type, false, false);
			this.task = t;
		}
	}
}