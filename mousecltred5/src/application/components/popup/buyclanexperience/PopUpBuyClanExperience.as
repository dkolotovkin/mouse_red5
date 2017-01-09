package application.components.popup.buyclanexperience
{
	import application.components.popup.PopUp;
	import application.components.popup.PopUpTitled;
	import application.components.popup.buyclanexperience.BuyClanExperience;
	
	public class PopUpBuyClanExperience extends PopUpTitled
	{
		private var _content:BuyClanExperience = new BuyClanExperience();
		
		public function PopUpBuyClanExperience()
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

