package application.components.popup.bet
{
	import application.components.popup.PopUpTitled;

	public class PopUpBet extends PopUpTitled
	{
		private var _content:BetContent = new BetContent();
		
		public function PopUpBet(roomid:int, userid:int)
		{
			super();
			_content.init(roomid, userid);
			_content.closefunction = closepopup;
		}
		
		private function closepopup():void{
			onHide();
		}
		
		override protected function createChildren():void{
			super.createChildren();
			addElement(_content);
		}
	}
}