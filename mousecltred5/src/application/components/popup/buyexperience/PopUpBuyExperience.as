package application.components.popup.buyexperience
{
	import application.components.popup.PopUp;
	import application.components.popup.PopUpTitled;
	
	public class PopUpBuyExperience extends PopUpTitled
	{
		private var _content:BuyExperience = new BuyExperience();
		
		public function PopUpBuyExperience()
		{
			super();
			title = "Черный рынок";
		}
		
		override protected function createChildren():void{
			super.createChildren();
			addElement(_content);
		}
	}
}