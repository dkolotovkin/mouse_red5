package application.gamecontainer.chat.actionmenu.bagarticle
{
	import application.GameApplication;
	import application.components.popup.articleinfo.PopUpArtefactArticleInfo;
	import application.components.popup.articleinfo.PopUpArticleInfo;
	import application.gamecontainer.chat.actionmenu.ActionMenu;
	import application.gamecontainer.chat.actionmenu.Actions;
	import application.gamecontainer.chat.actionmenu.title.ArticleTitle;
	import application.gamecontainer.chat.actionmenu.title.TargetTitle;
	
	import utils.shop.artefact.Artefact;
	
	public class ActionMenuBagArtefactArticle extends ActionMenu
	{
		private var _art : Artefact;
		
		public function ActionMenuBagArtefactArticle(art : Artefact) {
			super();
			_art = art;			
			title = _art.title;
		}
		
		override protected function createChildren() : void {
			super.createChildren();
			_titleComponent.setTitle("", true, 0);
			addAndCreateControl(Actions.INFO, "Информация и обмен");
		}
		
		override protected function getTitleComponent ():TargetTitle{
			return new ArticleTitle();
		}
		
		override protected function onAction(id : String) : void {
			super.onAction(id);			
			if (id == Actions.INFO){
				GameApplication.app.popuper.show(new PopUpArtefactArticleInfo(_art));
			}
		}
	}
}