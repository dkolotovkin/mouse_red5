package application.components.preloader 
{
	import flash.display.GradientType;
	import flash.display.MovieClip;
	import flash.display.SpreadMethod;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.ProgressEvent;
	import flash.filters.GlowFilter;
	import flash.geom.Matrix;
	import flash.text.TextField;
	
	import mx.events.FlexEvent;
	import mx.preloaders.DownloadProgressBar;
	
	public class GamePreLoader extends DownloadProgressBar
	{
		private var _mc:MovieClip;
		private var _sw:Number;
		private var _sh:Number;
		private var _gf:GlowFilter = new GlowFilter(0x000000, 1, 20, 20, 2);		
		private var colors:Array = [0x0F1312, 0x032F38, 0x000308];
		private var alphas:Array = [1, 1, 1];
		private var ratios:Array = [0, 125, 255];
		private var matr:Matrix = new Matrix();
		
		public function GamePreLoader()
		{
			super();
			_mc = new PreLoaderMC();
			addChild(_mc);
			(_mc["percent_txt"] as TextField).filters = [_gf];
			
			addEventListener(Event.ADDED_TO_STAGE, onAddToStage, false, 0, true);
		}	
		
		private function onAddToStage(e:Event):void{			
			_sw = stage.stageWidth;
			_sh = stage.stageHeight;		
			
			matr.createGradientBox(_sw, _sh, -Math.PI / 2, 0, 0);
			this.graphics.clear();
			this.graphics.beginGradientFill(GradientType.LINEAR, colors, alphas, ratios, matr, SpreadMethod.PAD);
			this.graphics.drawRect(0, 0, _sw, _sh);
			this.graphics.endFill();
			
			_mc.x = (_sw - _mc.width) / 2;
			_mc.y = (_sh - _mc.height) / 2;
		}
		
		override public function set preloader( preloader:Sprite ):void 
		{                   
			preloader.addEventListener( ProgressEvent.PROGRESS , SWFDownloadProgress );    
			preloader.addEventListener( Event.COMPLETE , SWFDownloadComplete );
			preloader.addEventListener( FlexEvent.INIT_PROGRESS , FlexInitProgress );
			preloader.addEventListener( FlexEvent.INIT_COMPLETE , FlexInitComplete );
		}
		
		private function SWFDownloadProgress( event:ProgressEvent ):void {
			var k:Number = event.bytesLoaded / event.bytesLoaded;
			_mc["percent_txt"].text = "Загрузка " + Math.round(k * 100) + "%";
			_mc["percent_mc"].width = 186 * k;
		}
		
		private function SWFDownloadComplete( event:Event ):void {			
		}
		
		private function FlexInitProgress( event:Event ):void {			
		}
		
		private function FlexInitComplete( event:Event ):void 
		{
			dispatchEvent( new Event( Event.COMPLETE ) );
		}
	}
}