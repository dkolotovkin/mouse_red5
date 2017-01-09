package utils.user
{
	import application.GameApplication;
	import application.components.clanitem.ClanItem;
	
	import flash.display.MovieClip;
	import flash.events.EventDispatcher;
	
	import utils.shop.item.ItemType;

	public class User extends EventDispatcher
	{
		[Bindable]
		public var id:uint;
		[Bindable]
		public var title:String;
		[Bindable]
		public var sex:uint;
		[Bindable]
		public var level:uint;
		[Bindable]
		public var experience:uint;
		[Bindable]
		public var exphour:uint;
		[Bindable]
		public var expday:uint;
		[Bindable]
		public var popular:uint;
		[Bindable]
		public var maxExperience:uint;
		[Bindable]
		public var money:uint;
		[Bindable]
		public var role:uint;
		[Bindable]
		public var colortype:uint;
		public var colortime:uint;
		[Bindable]
		public var accessorytype:uint;
		public var accessorytime:uint;
		[Bindable]
		public var url:String;	
		
		public var isonline:Boolean;
		
		public var claninfo:ClanUserInfo;
		[Bindable]
		public var pet:PetInfo;
		
		public function User()
		{
		}
		
		public function clone():User{			
			var user:User = new User();
			user.id = id;
			user.title = title;
			user.sex = sex;
			user.level = level;
			user.popular = popular;
			user.experience = experience;
			user.exphour = exphour;
			user.expday = expday;
			user.maxExperience = maxExperience;			
			user.money = user.money;
			user.role = user.role;
			user.url = user.url;
			return user;
		}
		
		public function update():void{
			if (id == GameApplication.app.userinfomanager.myuser.id){
				GameApplication.app.userinfomanager.myuser.title = title;
				GameApplication.app.userinfomanager.myuser.sex = sex;
				GameApplication.app.userinfomanager.myuser.level = level;
				GameApplication.app.userinfomanager.myuser.experience = experience;
				GameApplication.app.userinfomanager.myuser.exphour = exphour;
				GameApplication.app.userinfomanager.myuser.expday = expday;
				GameApplication.app.userinfomanager.myuser.maxExperience = maxExperience;				
				GameApplication.app.userinfomanager.myuser.money = money;
				GameApplication.app.userinfomanager.myuser.role = role;
				GameApplication.app.userinfomanager.myuser.url = url;
			}
			dispatchEvent(new UserEvent(UserEvent.UPDATE));
		}
		
		public static function createFromObject(u:Object):User{			
			if(u){
				var user:User = new User();
				user.id = int(u["id"]);
				user.sex = int(u["sex"]);
				user.title = String(u["title"]);	
				user.level = int(u["level"]);
				user.experience = int(u["experience"]);
				user.exphour = int(u["exphour"]);
				user.expday = int(u["expday"]);
				user.popular = int(u["popular"]);
				user.maxExperience = int(u["nextLevelExperience"]);				
				user.money = int(u["money"]);
				user.role = int(u["role"]);
				user.colortype = int(u["colortype"]);
				user.colortime = int(u["colortime"]);
				user.accessorytype = int(u["accessorytype"]);
				user.accessorytime = int(u["accessorytime"]);
				user.url = String(u["url"]);
				user.isonline = Boolean(u["isonline"]);
				
				if(u["claninfo"] != null){
					user.claninfo = new ClanUserInfo(u["claninfo"]["clanid"], u["claninfo"]["clantitle"], u["claninfo"]["clandepositm"],
														u["claninfo"]["clandeposite"], u["claninfo"]["clanrole"], u["claninfo"]["getclanmoneyat"]);
				}
				return user;
			}
			return null;
		}
		
		public static function getSkinClassByColorType(type:int):Class{
			if(type == ColorType.BLACK1 || type == ColorType.BLACK3 || type == ColorType.BLACK10){
				return LittleMouseBlack;
			}else if(type == ColorType.WHITE1 || type == ColorType.WHITE3 || type == ColorType.WHITE10){
				return LittleMouseWhite;
			}else if(type == ColorType.ORANGE1 || type == ColorType.ORANGE3 || type == ColorType.ORANGE10){
				return LittleMouseOrange;
			}else if(type == ColorType.FIOLET1 || type == ColorType.FIOLET3 || type == ColorType.FIOLET10){
				return LittleMouseFiolet;
			}else if(type == ColorType.BLUE1 || type == ColorType.BLUE3 || type == ColorType.BLUE10){
				return LittleMouseBlue;
			}
			return LittleMouse;
		}
		
		public static function getAccessoryClass(type:int):Class{
			if (type == Accessorytype.PEN1 || type == Accessorytype.PEN3 || type == Accessorytype.PEN10){
				return Pen;
			}else if (type == Accessorytype.BANDAGE1 || type == Accessorytype.BANDAGE3 || type == Accessorytype.BANDAGE10){
				return Bandage;
			}else if (type == Accessorytype.CRONE1 || type == Accessorytype.CRONE3 || type == Accessorytype.CRONE10){
				return Crone;
			}else if (type == Accessorytype.CYLINDER1 || type == Accessorytype.CYLINDER3 || type == Accessorytype.CYLINDER10){
				return Cylinder;
			}else if (type == Accessorytype.COOKHAT1 || type == Accessorytype.COOKHAT3 || type == Accessorytype.COOKHAT10){
				return CookHat;
			}else if (type == Accessorytype.KOVBOYHAT1 || type == Accessorytype.KOVBOYHAT3 || type == Accessorytype.KOVBOYHAT10){
				return KovboyHat;
			}else if (type == Accessorytype.FLASHHAIR1 || type == Accessorytype.FLASHHAIR3 || type == Accessorytype.FLASHHAIR10){
				return FlashHair;
			}else if (type == Accessorytype.PUMPKIN1 || type == Accessorytype.PUMPKIN3 || type == Accessorytype.PUMPKIN10){
				return PumpKin;
			}else if (type == Accessorytype.NYHAT1 || type == Accessorytype.NYHAT3 || type == Accessorytype.NYHAT10){
				return HatNewYear;
			}else if (type == Accessorytype.NY1 || type == Accessorytype.NY3 || type == Accessorytype.NY10){
				return NewYear;
			}else if (type == Accessorytype.DOCTOR1 || type == Accessorytype.DOCTOR3 || type == Accessorytype.DOCTOR10){
				return Doctor;
			}else if (type == Accessorytype.HELMET1 || type == Accessorytype.HELMET3 || type == Accessorytype.HELMET10){
				return Helmet;
			}else if (type == Accessorytype.POLICEHAT1 || type == Accessorytype.POLICEHAT3 || type == Accessorytype.POLICEHAT10){
				return PoliceHat;
			}else if (type == Accessorytype.ANGEL1 || type == Accessorytype.ANGEL3 || type == Accessorytype.ANGEL10){
				return Angel;
			}else if (type == Accessorytype.DEMON1 || type == Accessorytype.DEMON3 || type == Accessorytype.DEMON10){
				return Demon;
			}else if (type == Accessorytype.HIPHOP1 || type == Accessorytype.HIPHOP3 || type == Accessorytype.HIPHOP10){
				return HipHop;
			}else if (type == Accessorytype.GLAMUR1 || type == Accessorytype.GLAMUR3 || type == Accessorytype.GLAMUR10){
				return Glamur;
			}
			return MovieClip;			
		}
	}
}