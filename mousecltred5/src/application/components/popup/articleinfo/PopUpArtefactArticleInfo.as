package application.components.popup.articleinfo
{
	import application.GameApplication;
	import application.components.popup.PopUpTitled;
	
	import flash.events.MouseEvent;
	
	import utils.shop.artefact.Artefact;
	
	public class PopUpArtefactArticleInfo extends PopUpTitled
	{
		private var _articleInfo:ArtefactArticleInfo;
		private var _art:Artefact;
		
		public function PopUpArtefactArticleInfo(art:Artefact)
		{
			super();
			_art = art;
			_articleInfo = new ArtefactArticleInfo();
			_articleInfo.changebtn.addEventListener(MouseEvent.CLICK, onChange, false, 0, true);
		}
		
		private function onChange(e:MouseEvent):void{
			onHide();
			GameApplication.app.petmanager.changeArtefact(_art.id);
		}
		
		override protected function createChildren():void{
			super.createChildren();
			_articleInfo.title.text = _art.title;
			_articleInfo.description.text = _art.description;
			_articleInfo.article.init(_art);
			addElement(_articleInfo);
		}
	}
}