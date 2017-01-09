package utils.managers.navigation
{
	import application.GameApplication;
	import application.components.preloader.PreLoaderCircle;
	import application.gamecontainer.scene.admin.AdminPanel;
	import application.gamecontainer.scene.bag.Bag;
	import application.gamecontainer.scene.betpage.BetPage;
	import application.gamecontainer.scene.betspage.BetsPage;
	import application.gamecontainer.scene.betspage.BetsRoom;
	import application.gamecontainer.scene.catalog.Catalog;
	import application.gamecontainer.scene.clans.ClanRoom;
	import application.gamecontainer.scene.clans.ClansRoom;
	import application.gamecontainer.scene.game.GameWorld;
	import application.gamecontainer.scene.game.GameWorldTest;
	import application.gamecontainer.scene.home.HomePage;
	import application.gamecontainer.scene.minigames.MiniGames;
	import application.gamecontainer.scene.minigames.auction.Auction;
	import application.gamecontainer.scene.minigames.fortuna.Fortuna;
	import application.gamecontainer.scene.minigames.simplerule.SimpleRule;
	import application.gamecontainer.scene.minigames.victorina.Victorina;
	import application.gamecontainer.scene.pet.PetPage;
	import application.gamecontainer.scene.top.Top;
	
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.geom.Rectangle;
	
	import spark.components.Group;
	
	import utils.interfaces.ISceneContent;
	import utils.shop.CategoryType;
	
	public class NavigationManager extends EventDispatcher
	{
		public var currentSceneContent:ISceneContent;
		
		public function NavigationManager(target:IEventDispatcher=null)
		{
			super(target);
		}
		
		public function clear():void{
			if(currentSceneContent){
				currentSceneContent.onHide();
				GameApplication.app.gameContainer.scene.removeElement(currentSceneContent as Group);
			} 
		}
		
		public function goHome():void{			
			clear();
			
			var hp:HomePage = new HomePage();			
			GameApplication.app.gameContainer.scene.addElement(hp);
			currentSceneContent = hp;
		}
		
		public function goPetPage():void{			
			clear();
			
			var ppege:PetPage = new PetPage();			
			GameApplication.app.gameContainer.scene.addElement(ppege);
			currentSceneContent = ppege;
		}
		
		public function goBetPage():void{
			clear();
			
			var hp:BetPage = new BetPage();			
			GameApplication.app.gameContainer.scene.addElement(hp);
			currentSceneContent = hp;
		}
		
		public function goBetsPage():void{			
			clear();
			
			var hp:BetsPage = new BetsPage();			
			GameApplication.app.gameContainer.scene.addElement(hp);
			currentSceneContent = hp;
		}
		
		public function goBetsRoom(roomID:int, betsinfo:Object):void{			
			clear();
			
			var hp:BetsRoom = new BetsRoom();
			hp.roomID = roomID;
			hp.updateBetsInfo(betsinfo);
			GameApplication.app.gameContainer.scene.addElement(hp);
			currentSceneContent = hp;
		}
		
		public function goGameWorld(roomID:int, locationXML:XML, users:Array, gt:int):GameWorld{			
			clear();
			
			var r:Rectangle = new Rectangle(0, 0, GameApplication.app.gameContainer.scene.width, GameApplication.app.gameContainer.scene.height);
			var gw:GameWorld = new GameWorld(roomID, r, locationXML, users, gt);
			GameApplication.app.gameContainer.scene.addElement(gw);
			currentSceneContent = gw;
			return gw;
		}
		
		public function goGameWorldTest():GameWorldTest{			
			clear();
			
			var r:Rectangle = new Rectangle(0, 0, GameApplication.app.gameContainer.scene.width, GameApplication.app.gameContainer.scene.height);
			var gw:GameWorldTest = new GameWorldTest(r);
			GameApplication.app.gameContainer.scene.addElement(gw);
			currentSceneContent = gw;
			return gw;
		}
		
		public function goFindUsersScreen(wt:int):void{
			clear();
			
			var fu:PreLoaderCircle = new PreLoaderCircle();
			fu.text = "Идет поиск соперников...";
			fu.time = wt;
			GameApplication.app.gameContainer.scene.addElement(fu);
			currentSceneContent = fu;
		}
		
		public function goShop(selectedCategory:int = 3):void{
			clear();			
			
			var catalog:Catalog = new Catalog();
			catalog.selectedCategory = selectedCategory;
			GameApplication.app.gameContainer.scene.addElement(catalog);
			currentSceneContent = catalog;		
		}
		
		public function goBag(selectCategory:int = 5):void{
			clear();			
			
			var bag:Bag = new Bag();
			bag.selectedCategory = selectCategory;
			GameApplication.app.gameContainer.scene.addElement(bag);
			currentSceneContent = bag;		
		}
		
		public function goTop():void{
			clear();			
			
			var top:Top = new Top();
			GameApplication.app.gameContainer.scene.addElement(top);
			currentSceneContent = top;		
		}
		
		public function goMiniGames():void{
			clear();			
			
			var minigames:MiniGames = new MiniGames();
			GameApplication.app.gameContainer.scene.addElement(minigames);
			currentSceneContent = minigames;		
		}
		
		public function goAdminPanel():void{
			clear();			
			
			var admin:AdminPanel = new AdminPanel();
			GameApplication.app.gameContainer.scene.addElement(admin);
			currentSceneContent = admin;		
		}
		
		public function goFortuna():void{
			clear();			
			
			var fortuna:Fortuna = new Fortuna();
			GameApplication.app.gameContainer.scene.addElement(fortuna);
			currentSceneContent = fortuna;		
		}
		
		public function goSimpleRule(type:int):void{
			clear();			
			
			var simplerule:SimpleRule = new SimpleRule();
			simplerule.roomtype = type;
			GameApplication.app.gameContainer.scene.addElement(simplerule);
			currentSceneContent = simplerule;		
		}
		
		public function goAuction():void{
			clear();			
			
			var auction:Auction = new Auction();
			GameApplication.app.gameContainer.scene.addElement(auction);
			currentSceneContent = auction;		
		}
		
		public function goVictorina():void{
			clear();			
			
			var victorina:Victorina = new Victorina();
			GameApplication.app.gameContainer.scene.addElement(victorina);
			currentSceneContent = victorina;		
		}
		
		public function goClansRoom():void{
			clear();			
			
			var clans:ClansRoom = new ClansRoom();
			GameApplication.app.gameContainer.scene.addElement(clans);
			currentSceneContent = clans;		
		}
		public function goClanRoom(idclan:int):void{
			clear();			
			
			var clan:ClanRoom = new ClanRoom();
			clan.idclan = idclan;
			GameApplication.app.gameContainer.scene.addElement(clan);
			currentSceneContent = clan;		
		}
	}
}