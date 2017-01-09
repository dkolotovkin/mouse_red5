package application.components.popup.bet.request
{
	import application.components.popup.PopUpTitled;
	
	import utils.game.betroominfo.BetResult;
	
	public class PopUpBetsGameRequest extends PopUpTitled
	{
		private var _content:BetsGameRequestContent = new BetsGameRequestContent();
		
		public function PopUpBetsGameRequest(userid:int, roomID:int){
			super();	
			_content.closefunction = closepopup;
			_content.userid = userid;
			_content.roomID = roomID;
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