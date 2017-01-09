package application.components.popup.extraction.bets
{
	import application.components.popup.PopUpTitled;
	
	public class PopUpExtractionBets extends PopUpTitled
	{
		private var _content:ExtractionBetsContent = new ExtractionBetsContent();
		
		public function PopUpExtractionBets(returnmoney:int, winnermoney:int, prizemoney:int)
		{
			super();
			_content.returnmoney = returnmoney;
			_content.winnermoney = winnermoney;
			_content.prizemoney = prizemoney;
			
			_content.descr.text = "Забег окончен!";
			if(returnmoney == 0 && winnermoney == 0 && prizemoney == 0){
				_content.descr.text = "Забег окончен! К сожалению вы ничего не выйграли.";
			}
		}
		
		override protected function createChildren():void{
			super.createChildren();
			addElement(_content);
		}
	}
}