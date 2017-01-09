package application.components.popup.feedpet
{
	import application.GameApplication;
	import application.components.popup.PopUpTitled;
	
	public class PopUpFeedPet extends PopUpTitled
	{
		private var _content:FeedPetContent = new FeedPetContent();
		
		public function PopUpFeedPet()
		{
			super();	
			title = "Покормить помощника";
			if(GameApplication.app.petmanager.petfeedprices.length >= GameApplication.app.userinfomanager.myuser.pet.level){
				_content.init(GameApplication.app.petmanager.petfeedprices[GameApplication.app.userinfomanager.myuser.pet.level - 1]);
			}
		}
		
		override protected function createChildren():void{
			super.createChildren();
			addElement(_content);
		}
	}
}