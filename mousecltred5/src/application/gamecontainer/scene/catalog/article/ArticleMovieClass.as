package application.gamecontainer.scene.catalog.article
{
	import flash.display.MovieClip;
	import flash.geom.Rectangle;
	
	import utils.shop.artefact.ArtefactType;
	import utils.shop.item.ItemType;

	public class ArticleMovieClass
	{	
		public function ArticleMovieClass(){
		}
		
		public static function getClassByItemPrototypeID(id:int):Class{
			if (id == ItemType.BOX){
				return ShopBox;
			}else if (id == ItemType.BALL){
				return ShopBall;
			}else if (id == ItemType.STATIC){
				return ShopStatic;
			}else if (id == ItemType.HEAVYBOX){
				return ShopHeavyBox;
			}else if (id == ItemType.STICK){
				return ShopStick;
			}else if (id == ItemType.SPRINGBROAD){
				return ShopSpringboard;
			}else if (id == ItemType.KERNELLEFT){
				return ShopKernelLeft;
			}else if (id == ItemType.KERNELRIGHT){
				return ShopKernelRight;
			}else if (id == ItemType.FREEZE){
				return IconFreeze;
			}else if (id == ItemType.ANTIFREEZE){
				return IconAntiFreeze;
			}else if (id == ItemType.FOOD){
				return ShopFood;
			}else if (id == ItemType.ENERGYC){
				return ShopEnergyc;
			}else if (id == ItemType.ELIXIR){
				return ShopElixir;
			}else if (id == ItemType.MAGIC_HAND){
				return ShopHand;
			}else if (id == ItemType.MOUSE_BLACK1 || id == ItemType.MOUSE_BLACK3 || id == ItemType.MOUSE_BLACK10){
				return ShopMouseBlack;
			}else if (id == ItemType.MOUSE_WHITE1 || id == ItemType.MOUSE_WHITE3 || id == ItemType.MOUSE_WHITE10){
				return ShopMouseWhite;
			}else if (id == ItemType.MOUSE_ORANGE1 || id == ItemType.MOUSE_ORANGE3 || id == ItemType.MOUSE_ORANGE10){
				return ShopMouseOrange;
			}else if (id == ItemType.MOUSE_BLUE1 || id == ItemType.MOUSE_BLUE3 || id == ItemType.MOUSE_BLUE10){
				return ShopMouseBlue;
			}else if (id == ItemType.MOUSE_FIOLET1 || id == ItemType.MOUSE_FIOLET3 || id == ItemType.MOUSE_FIOLET10){
				return ShopMouseFiolet;
			}else if (id == ItemType.PEN1 || id == ItemType.PEN3 || id == ItemType.PEN10){
				return Pen;
			}else if (id == ItemType.BANDAGE1 || id == ItemType.BANDAGE3 || id == ItemType.BANDAGE10){
				return Bandage;
			}else if (id == ItemType.CRONE1 || id == ItemType.CRONE3 || id == ItemType.CRONE10){
				return Crone;
			}else if (id == ItemType.CYLINDER1 || id == ItemType.CYLINDER3 || id == ItemType.CYLINDER10){
				return Cylinder;
			}else if (id == ItemType.COOKHAT1 || id == ItemType.COOKHAT3 || id == ItemType.COOKHAT10){
				return CookHat;
			}else if (id == ItemType.KOVBOYHAT1 || id == ItemType.KOVBOYHAT3 || id == ItemType.KOVBOYHAT10){
				return KovboyHat;
			}else if (id == ItemType.FLASHHAIR1 || id == ItemType.FLASHHAIR3 || id == ItemType.FLASHHAIR10){
				return FlashHair;
			}else if (id == ItemType.PUMPKIN1 || id == ItemType.PUMPKIN3 || id == ItemType.PUMPKIN10){
				return PumpKin;
			}else if (id == ItemType.NYHAT1 || id == ItemType.NYHAT3 || id == ItemType.NYHAT10){
				return HatNewYear;
			}else if (id == ItemType.NY1 || id == ItemType.NY3 || id == ItemType.NY10){
				return NewYear;
			}else if (id == ItemType.DOCTOR1 || id == ItemType.DOCTOR3 || id == ItemType.DOCTOR10){
				return Doctor;
			}else if (id == ItemType.ANGEL1 || id == ItemType.ANGEL3 || id == ItemType.ANGEL10){
				return Angel;
			}else if (id == ItemType.DEMON1 || id == ItemType.DEMON3 || id == ItemType.DEMON10){
				return Demon;
			}else if (id == ItemType.HELMET1 || id == ItemType.HELMET3 || id == ItemType.HELMET10){
				return Helmet;
			}else if (id == ItemType.POLICEHAT1 || id == ItemType.POLICEHAT3 || id == ItemType.POLICEHAT10){
				return PoliceHat;
			}else if (id == ItemType.HIPHOP1 || id == ItemType.HIPHOP3 || id == ItemType.HIPHOP10){
				return HipHop;
			}else if (id == ItemType.GLAMUR1 || id == ItemType.GLAMUR3 || id == ItemType.GLAMUR10){
				return Glamur;
			}else if (id == ItemType.SWEET){
				return Sweet;
			}else if (id == ItemType.STRAWBERRY){
				return StrawBerry;
			}else if (id == ItemType.RASPBERRY){
				return RaspBerry;
			}else if (id == ItemType.FLOWER){
				return Flower;
			}else if (id == ItemType.SWEETS){
				return Sweets;
			}else if (id == ItemType.CACKE){
				return Cacke;
			}else if (id == ItemType.BEER){
				return Beer;
			}else if (id == ItemType.HEART){
				return Heart;
			}else if (id == ItemType.COCKTAIL){
				return Cocktail;
			}else if (id == ItemType.FLOWERS){
				return Flowers;
			}else if (id == ItemType.FIRTREE){
				return FirTree;
			}else if (id == ItemType.SNOWMAN){
				return SnowMan;
			}else if (id == ItemType.CHEESE){
				return Cheese;
			}else if (id == ItemType.BEAR){
				return Bear;
			}else if (id == ItemType.BOOK){
				return BookPresent;
			}else if (id == ItemType.BUTTERFLY){
				return ButterFly;
			}else if (id == ItemType.MONEYBAG){
				return MoneyPresent;
			}else if (id == ItemType.CAR){
				return Car;
			}else if (id == ItemType.GUN){
				return Target;
			}else if (id == ItemType.KISS){
				return Kiss;
			}else if (id == ItemType.RING){
				return Ring;
			}else if (id == ItemType.INLOVE){
				return InLove;
			}else if (id == ItemType.BALLS){
				return Balls;
			}else if (id == ItemType.STAR9MAY){
				return Star9May;
			}else if (id == ItemType.MAY9){
				return May9;
			}else if (id == ItemType.CASTLE){
				return Castle;
			}else if (id == ItemType.PARROT){
				return Parrot;
			}
			return MovieClip;	
		}
		
		public static function getGameItemClass(id:int):Class{
			if (id == ItemType.BOX){
				return BoxSkin;
			}else if (id == ItemType.BALL){
				return BallSkin;
			}else if (id == ItemType.STATIC){
				return StaticSkin;
			}else if (id == ItemType.HEAVYBOX){
				return HeavyBoxSkin;
			}else if (id == ItemType.STICK){
				return StickSkin;
			}else if (id == ItemType.SPRINGBROAD){
				return SpringboardSkin;
			}else if (id == ItemType.KERNELLEFT){
				return KernelLeftSkin;
			}else if (id == ItemType.KERNELRIGHT){
				return KernelRightSkin;
			}else if (id == ItemType.MAGIC_HAND){
				return HandSkin;
			}else if (id == ItemType.FREEZE){
				return IconFreeze;
			}else if (id == ItemType.ANTIFREEZE){
				return IconAntiFreeze;
			}else if (id == ItemType.GUN){
				return Target;
			}		
			return MovieClip;			
		}
		
		public static function getGameItemBounds(id:int):Rectangle{
			var rect:Rectangle = new Rectangle();
			rect.width = 30;
			rect.height = 30;
			if (id == ItemType.BOX){
				rect.width = 50;
				rect.height = 50;				
			}else if (id == ItemType.BALL){
				rect.width = 25;
				rect.height = 25;				
			}else if (id == ItemType.STATIC){
				rect.width = 30;
				rect.height = 80;				
			}else if (id == ItemType.HEAVYBOX){
				rect.width = 41;
				rect.height = 22;				
			}else if (id == ItemType.STICK){
				rect.width = 300;
				rect.height = 10;				
			}else if (id == ItemType.SPRINGBROAD){
				rect.width = 51;
				rect.height = 10;				
			}else if (id == ItemType.KERNELLEFT){
				rect.width = 26;
				rect.height = 26;				
			}else if (id == ItemType.KERNELRIGHT){
				rect.width = 26;
				rect.height = 26;				
			}		
			return rect;
		}		
		
		public static function getArtefactClassByID(id:int):Class{
			if (id == ArtefactType.GOLD_STAR){
				return AGoldStar;
			}else if (id == ArtefactType.GOLD_COINS){
				return AGoldCoins;
			}else if (id == ArtefactType.GOLD_NOTE){
				return AGoldNote;
			}else if (id == ArtefactType.GOLD_HEART){
				return AGoldHeart;
			}else if (id == ArtefactType.GOLD_RING_1){
				return AGoldRing1;
			}else if (id == ArtefactType.GOLD_CUP){
				return AGoldCup;
			}else if (id == ArtefactType.GOLDS){
				return AGolds;
			}else if (id == ArtefactType.GOLD_RING_2){
				return AGoldRing2;
			}	
			return MovieClip;			
		}
	}
}