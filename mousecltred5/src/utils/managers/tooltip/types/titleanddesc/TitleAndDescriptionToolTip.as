package utils.managers.tooltip.types.titleanddesc
{
	import application.gamecontainer.scene.bag.article.BagArticle;
	import application.gamecontainer.scene.catalog.article.ArticleArtefact;
	import application.gamecontainer.scene.catalog.article.ArticleBagArtefact;
	
	import utils.managers.tooltip.IToolTiped;
	import utils.managers.tooltip.types.siimple.ToolTip;
	
	public class TitleAndDescriptionToolTip extends ToolTip
	{
		private var _title:String;
		private var _description:String;
		
		public function TitleAndDescriptionToolTip()
		{
			super();
			setStyle("skinClass", TitleAndDescriptionToolTipSkin);
		}
		
		override public function updateState() : void {
			if (initialized) {
				(skin as TitleAndDescriptionToolTipSkin).title.text = _title;
				(skin as TitleAndDescriptionToolTipSkin).description.text = _description;
			}
		}
		
		override public function set target(value : IToolTiped) : void {			
			if (value is BagArticle){				
				_title = BagArticle(value).tooltiptitle;
				_description = BagArticle(value).tooltipdescription;
				updateState();
			}
			if (value is ArticleArtefact){				
				_title = ArticleArtefact(value).tooltiptitle;
				_description = ArticleArtefact(value).tooltipdescription;
				updateState();
			}
			if (value is ArticleBagArtefact){				
				_title = ArticleBagArtefact(value).tooltiptitle;
				_description = ArticleBagArtefact(value).tooltipdescription;
				updateState();
			}
		}
	}
}