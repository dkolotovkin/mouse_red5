package application.components.popup.extraction
{
	import application.GameApplication;
	import application.components.popup.PopUpTitled;
	import application.gamecontainer.scene.catalog.article.ArticleArtefact;
	
	public class PopUpExtraction extends PopUpTitled
	{
		private var _extractionInfo:ExtractionInfo;
		
		public function PopUpExtraction(extraction:Object, position:int)
		{
			super();
			var experience:int = 0;
			var cexperience:int = 0;
			var money:int = 0;
			var experienceBonus:int = 0;
			var cexperienceBonus:int = 0;
			var moneyBonus:int = 0;
			
			_extractionInfo = new ExtractionInfo();
			if (extraction){
				experience = extraction["experience"];
				cexperience = extraction["cexperience"];
				money = extraction["money"];
				experienceBonus = extraction["experiencebonus"];
				cexperienceBonus = extraction["cexperiencebonus"];
				moneyBonus = extraction["moneybonus"];
				_extractionInfo.artefactID = extraction["artefactID"];
			}			
			
			title = "Окончание забега";
			if (experience > 0){
				_extractionInfo.description = "Забег окончен и Вы заняли " + position + " место!";
			}else{
				_extractionInfo.description = "Забег окончен! К сожалению Вы не заняли призовое место и не получили награды. Попробуйте еще.";
			}
			_extractionInfo.experience = experience;
			_extractionInfo.cexperience = cexperience;
			_extractionInfo.money = money;
			_extractionInfo.experienceBonus = experienceBonus;
			_extractionInfo.cexperienceBonus = cexperienceBonus;
			_extractionInfo.moneyBonus = moneyBonus;
		}
		
		override protected function createChildren():void{
			super.createChildren();
			addElement(_extractionInfo);
		}
	}
}