package application.components.popup.bet.retrn
{
	import application.components.popup.PopUpTitled;
	
	public class PopUpReturnMoney extends PopUpTitled
	{
		private var _content:ReturnMoneyContent = new ReturnMoneyContent();
		
		public function PopUpReturnMoney(userid:int, money:int)
		{
			super();
			priority = 20;
			_content.init(userid, money);
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