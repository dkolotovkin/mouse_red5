package mouseapp;

import java.util.Arrays;
import java.util.List;

public class Config {
	
	public static byte MAINSERVER = 1;
	public static byte HELPSERVER = 2;
	
	public static byte SERVERTYPE = MAINSERVER;
	
	public static byte TEST = 0;
	public static byte RELEASE = 1;
	
	public static int mode(){return RELEASE;}
	public static int currentVersion(){return 19;}											//бепяхъ йкхемрю
	
	public static List<Integer> exphourprizes = Arrays.asList(300, 200, 100, 50, 30);		//мюцпюдю ксвьхл гю вюя
	public static List<Integer> expdayprizes = Arrays.asList(3000, 2000, 1000, 500, 300);	//мюцпюдю ксвьхл гю демэ
	
	public static int maxMessageCountInRoom(){return 50;} 						//йнкхвеярбн яннаыемхи, йнрнпше упюмхл б йнлмюре	
	public static int energyForGame(){return 10;}								//йнкхвеярбн щмепцхх, менаундхлне дкъ хцпш	
	public static int energyPetForGame(){return 1;}								//йнкхвеярбн щмепцхх, менаундхлне дкъ хцпш охрнлжс	
	public static int timeEnergyUpdate(){return 20;}							//бпелъ вепег йнрнпне намнбкъеряъ щмепцхъ	
	public static int valueEnergyUpdate(){return 3;}							//гмювемхе мю йнрнпне хглемъеряъ щмепцхъ вепег timeEnergyUpdate	 
	public static int valueEnergyUpdateInBan(){return 2;}						//гмювемхе мю йнрнпне хглемъеряъ щмепцхъ б аюме гю лхмсрс	
	public static int valuePopularUpdateInBan(){return 2;}						//гмювемхе мю йнрнпне хглемъеряъ оноскъпмнярэ б аюме гю лхмсрс	 
	public static int banminutePrice(){return 20;}								//жемю гю лхмсрс аюмю	 
	public static int maxValueEnergyUpdateInBan(){return 1000;}					//гмювемхе мю йнрнпне хглемъеряъ щмепцхъ б аюме гю лхмсрс	
	public static int changeInfoPrice(){return 200;}							//жемю гю ялемс хмтнплюжхх н оепянмюфе	
	public static int sendMailPrice(){return 20;}								//жемю гю нропюбйс онврш	
	public static int createClanPrice(){return 100000;}							//жемю гю онйсойс йкюмю	
	public static int createClanNeedLevel(){return 10;}							//менаундхлши спнбемэ дкъ янгдюмхъ йкюмю	
	public static int showLinkPrice(){return 50;}								//жемю гю опнялнрп яяшкйх	
	public static int victorinaPrize(){return 5;}								//мюцпюдю ношрю гю нрбер б бхернпхме	
	public static int experiencePrize(){return 10;}								//мюцпюдю ношрю гю онаедс б гюаеце	
	public static int experienceBonus(){return 3;}								//анмся ношрю гю жбер хкх юйяеяясюп	
	public static int experienceClanBonus(){return 1;}							//анмся ношрю йкюмю гю жбер хкх юйяеяясюп	
	public static int friendBonus(){return 30;}									//анмся гю опхбедеммнцн дпсцю	
	public static int moneyPrize(){return 7;}									//мюцпюдю демец гю онаедс б гюаеце	
	public static int moneyBonus(){return 1;}									//анмся демец гю онаедс б гюаеце	
	public static int percentMoneyUsers(){return 25;}							//яйнкэйн опнжемрнб онкэгнбюрекеи онксвюр демефмши опхг	
	public static int experiencePrizeDelta(){return 2;}							//пюгмхжю лефдс мюцпюдюлх(ношрю) опхгепнб гюаецю	
	public static int buyexperiencek(){return 4;}								//йнщттхжхемр онйсойх ношрю	
	
	public static int waitTimeToStartBets(){return 5;}							//бпелъ нфхдюмхъ хцпш ян ярюбйюл	
	public static int waitTimeToStart(){if(Config.mode() == Config.TEST){return 3;}else{return 20;}}		//бпелъ нфхдюмхъ хцпш
	public static int waitTimeToStartBet(){if(Config.mode() == Config.TEST){return 6;}else{return 40;}}		//бпелъ нфхдюмхъ хцпш мю демэцх	
	
	public static int minUsersInGame(){if(Config.mode() == Config.TEST){return 1;}else{return 2;}}			//лхмхлюкэмне йнкхвеярбн онкэгнбюрекеи б хцпе
	public static int minUsersInGameByLevel(){return 5;}						//йнкхвеярбн онкэгнбюрекеи менаундхлне дкъ мювюкю хцпш аег назедхмемхъ он спнбмъл	
	public static int maxUsersInGame(){return 10;}								//люйяхлюкэмне йнкхвеярбн онкэгнбюрекеи б хцпе	
	
	public static int moneyToVote(){return 400;}											//йнкхвеяйрбн лнмер гю 1 цнкня VK
	
	public static String protectedSecretSiteVK(){return "xxxxxxxxxxxxxxxxxx";}				//protected secret VK (for site)
	public static String protectedSecretVK(){return "xxxxxxxxxxxxxxxxx";}					//protected secret VK	 
	public static String protectedSecretSiteMM(){return "xxxxxxxxxxxxxxxxxxxxxxxxxxxx";}	//protected secret MM (for site)
	public static String protectedSecretMM(){return "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx";}		//protected secret MM
	public static String protectedSecretSiteOD(){return "xxxxxxxxxxxxxxxxxxxxxxxxxx";}		//protected secret OD (for site)
	public static String applicationKeySiteOD(){return "xxxxxxxxxxxxxxxxxxxxx";}			//application key OD  (for site)
	public static String protectedSecretOD(){return "xxxxxxxxxxxxxxxxxxxxx";}				//protected secret OD													 

	public static int appIdVK(){return 11111111;}												//уюпдйнд дкъ сбеднлкемхи	
	public static int appIdMM(){return 11111;}													//уюпдйнд дкъ сбеднлкемхи	
	public static int appIdOD(){return 1111111;}												//уюпдйнд дкъ сбеднлкемхи	
	
	public static String apiUrlMM(){return "http://www.appsmail.ru/platform/api";}				//уюпдйнд дкъ сбеднлкемхи
	public static String apiUrlVK(){return "http://api.vkontakte.ru/api.php";}					//уюпдйнд дкъ сбеднлкемхи
	
	public static String loginUrlVK(){return "https://api.vkontakte.ru/oauth/access_token";}	//уюпдйнд дкъ яюирю
	public static String loginUrlMM(){return "https://connect.mail.ru/oauth/token";}			//уюпдйнд дкъ яюирю
	public static String loginUrlOD(){return "http://api.odnoklassniki.ru/oauth/token.do";}		//уюпдйнд дкъ яюирю
	
	
	public static String oficalSiteUrl(){return "http://mouserun.ru";}							//уюпдйнд дкъ яюирю
}
