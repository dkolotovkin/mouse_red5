package utils.managers.pet
{
	import application.GameApplication;
	
	import flash.net.Responder;
	
	import utils.shop.BuyResultCode;
	import utils.shop.CategoryType;

	public class PetManager
	{
		public var artefacts:Object;
		public var petprices:Array;
		public var petfeedprices:Array;
		public var needpetexperience:Array;	
		
		public var petsartefacts:Array = new Array();		
		
		public function PetManager()
		{			
		}
		
		public static function getNextPetType(petlevel:int):int{
			if(petlevel == 1){
				return 2;
			}else if(petlevel == 2){
				return 3;
			}else if(petlevel == 3){
				return 4;
			}else if(petlevel == 4){
				return 5;
			}else if(petlevel == 5){
				return 6;
			}
			return 1;
		}
		
		public static function getPetType(petlevel:int):int{
			if(petlevel == 1){
				return 1;
			}else if(petlevel == 2){
				return 2;
			}else if(petlevel == 3){
				return 3;
			}else if(petlevel == 4){
				return 4;
			}else if(petlevel == 5){
				return 5;
			}
			return 6;
		}
		
		public function updatePetInfo():void{
			GameApplication.app.connection.call("petmanager.getPetInfo", new Responder(onGetPetInfo, onError));
		}
		
		private function onGetPetInfo(obj:Object):void{
			if(obj){
				GameApplication.app.userinfomanager.myuser.pet.experience = obj["experience"];
				GameApplication.app.userinfomanager.myuser.pet.energy = obj["energy"];				
			}
		}
		
		public function init():void{
			GameApplication.app.connection.call("petmanager.getparams", new Responder(onGetParams, onError));
		}		
		private function onGetParams(obj:Object):void{
			artefacts = obj["artefacts"];
			petprices = obj["petprices"];
			petfeedprices = obj["petfeedprices"];
			needpetexperience = obj["needpetexperience"];
			
			petsartefacts.push(obj["pet1artefacts"]);
			petsartefacts.push(obj["pet2artefacts"]);
			petsartefacts.push(obj["pet3artefacts"]);
			petsartefacts.push(obj["pet4artefacts"]);
			petsartefacts.push(obj["pet5artefacts"]);	
			
//			trace("-------------->petprices: " + petprices);
//			trace("-------------->petfeedprices: " + petfeedprices);
//			trace("-------------->needpetexperience: " + needpetexperience);
//			
//			trace("artefact id3: " + artefacts[3]["title"]);
//			
//			for each(var obj in artefacts){
//				trace("artefact id: " + obj["id"]);
//				trace("artefact title: " + obj["title"]);
//			}
		}
		private function onError(error:Object):void{
			GameApplication.app.popuper.showInfoPopUp("Ошибка при обращении к серверу.");
		}
		
		public function buypet():void{
			GameApplication.app.connection.call("petmanager.buyPet", new Responder(onBuyPet, onError));
		}
		
		private function onBuyPet(obj:Object):void{		
			if(obj != null){
				GameApplication.app.userinfomanager.updatePetInfo(obj);
				GameApplication.app.navigator.goPetPage();
			}else{
				GameApplication.app.popuper.showInfoPopUp("Невозможно выполнить операцию");
			}
		}
		
		public function changeArtefact(artID:int):void{
			GameApplication.app.connection.call("petmanager.changeArtefact", new Responder(onChangeArtefact, onError), artID);
		}
		
		private function onChangeArtefact(obj:Object):void{		
			GameApplication.app.navigator.goBag(CategoryType.ARTEFACTS);
		}
		
		public function feedPet():void{
			GameApplication.app.connection.call("petmanager.feedPet", new Responder(onFeed, onError));
		}
		
		private function onFeed(buyresult:Object):void{			
			if (buyresult["error"] == BuyResultCode.OK){
				GameApplication.app.popuper.showInfoPopUp("Ваш помощник снова полон энергии.");
				GameApplication.app.navigator.goPetPage();
			}else if (buyresult["error"] == BuyResultCode.NOT_ENOUGH_MONEY){
				GameApplication.app.popuper.showInfoPopUp("У Вас не достаточно денег для этой покупки.");
			}else if (buyresult["error"] == BuyResultCode.NOT_PROTOTYPE){
				GameApplication.app.popuper.showInfoPopUp("Невозможно купить эту вещь.");
			}else if (buyresult["error"] == BuyResultCode.SQL_ERROR){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 756. Сообщите об ошибке разработчикам.");
			}else if (buyresult["error"] == BuyResultCode.OTHER){
				GameApplication.app.popuper.showInfoPopUp("Ошибка при покупке. Код ошибки 796. Сообщите об ошибке разработчикам.");
			}			
		}
	}
}