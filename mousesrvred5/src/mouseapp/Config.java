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
	public static int currentVersion(){return 19;}											//������ �������
	
	public static List<Integer> exphourprizes = Arrays.asList(300, 200, 100, 50, 30);		//������� ������ �� ���
	public static List<Integer> expdayprizes = Arrays.asList(3000, 2000, 1000, 500, 300);	//������� ������ �� ����
	
	public static int maxMessageCountInRoom(){return 50;} 						//���������� ���������, ������� ������ � �������	
	public static int energyForGame(){return 10;}								//���������� �������, ����������� ��� ����	
	public static int energyPetForGame(){return 1;}								//���������� �������, ����������� ��� ���� �������	
	public static int timeEnergyUpdate(){return 20;}							//����� ����� ������� ����������� �������	
	public static int valueEnergyUpdate(){return 3;}							//�������� �� ������� ���������� ������� ����� timeEnergyUpdate	 
	public static int valueEnergyUpdateInBan(){return 2;}						//�������� �� ������� ���������� ������� � ���� �� ������	
	public static int valuePopularUpdateInBan(){return 2;}						//�������� �� ������� ���������� ������������ � ���� �� ������	 
	public static int banminutePrice(){return 20;}								//���� �� ������ ����	 
	public static int maxValueEnergyUpdateInBan(){return 1000;}					//�������� �� ������� ���������� ������� � ���� �� ������	
	public static int changeInfoPrice(){return 200;}							//���� �� ����� ���������� � ���������	
	public static int sendMailPrice(){return 20;}								//���� �� �������� �����	
	public static int createClanPrice(){return 100000;}							//���� �� ������� �����	
	public static int createClanNeedLevel(){return 10;}							//����������� ������� ��� �������� �����	
	public static int showLinkPrice(){return 50;}								//���� �� �������� ������	
	public static int victorinaPrize(){return 5;}								//������� ����� �� ����� � ���������	
	public static int experiencePrize(){return 10;}								//������� ����� �� ������ � ������	
	public static int experienceBonus(){return 3;}								//����� ����� �� ���� ��� ���������	
	public static int experienceClanBonus(){return 1;}							//����� ����� ����� �� ���� ��� ���������	
	public static int friendBonus(){return 30;}									//����� �� ������������ �����	
	public static int moneyPrize(){return 7;}									//������� ����� �� ������ � ������	
	public static int moneyBonus(){return 1;}									//����� ����� �� ������ � ������	
	public static int percentMoneyUsers(){return 25;}							//������� ��������� ������������� ������� �������� ����	
	public static int experiencePrizeDelta(){return 2;}							//������� ����� ���������(�����) �������� ������	
	public static int buyexperiencek(){return 4;}								//����������� ������� �����	
	
	public static int waitTimeToStartBets(){return 5;}							//����� �������� ���� �� �������	
	public static int waitTimeToStart(){if(Config.mode() == Config.TEST){return 3;}else{return 20;}}		//����� �������� ����
	public static int waitTimeToStartBet(){if(Config.mode() == Config.TEST){return 6;}else{return 40;}}		//����� �������� ���� �� ������	
	
	public static int minUsersInGame(){if(Config.mode() == Config.TEST){return 1;}else{return 2;}}			//����������� ���������� ������������� � ����
	public static int minUsersInGameByLevel(){return 5;}						//���������� ������������� ����������� ��� ������ ���� ��� ����������� �� �������	
	public static int maxUsersInGame(){return 10;}								//������������ ���������� ������������� � ����	
	
	public static int moneyToVote(){return 400;}											//����������� ����� �� 1 ����� VK
	
	public static String protectedSecretSiteVK(){return "xxxxxxxxxxxxxxxxxx";}				//protected secret VK (for site)
	public static String protectedSecretVK(){return "xxxxxxxxxxxxxxxxx";}					//protected secret VK	 
	public static String protectedSecretSiteMM(){return "xxxxxxxxxxxxxxxxxxxxxxxxxxxx";}	//protected secret MM (for site)
	public static String protectedSecretMM(){return "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx";}		//protected secret MM
	public static String protectedSecretSiteOD(){return "xxxxxxxxxxxxxxxxxxxxxxxxxx";}		//protected secret OD (for site)
	public static String applicationKeySiteOD(){return "xxxxxxxxxxxxxxxxxxxxx";}			//application key OD  (for site)
	public static String protectedSecretOD(){return "xxxxxxxxxxxxxxxxxxxxx";}				//protected secret OD													 

	public static int appIdVK(){return 11111111;}												//������� ��� �����������	
	public static int appIdMM(){return 11111;}													//������� ��� �����������	
	public static int appIdOD(){return 1111111;}												//������� ��� �����������	
	
	public static String apiUrlMM(){return "http://www.appsmail.ru/platform/api";}				//������� ��� �����������
	public static String apiUrlVK(){return "http://api.vkontakte.ru/api.php";}					//������� ��� �����������
	
	public static String loginUrlVK(){return "https://api.vkontakte.ru/oauth/access_token";}	//������� ��� �����
	public static String loginUrlMM(){return "https://connect.mail.ru/oauth/token";}			//������� ��� �����
	public static String loginUrlOD(){return "http://api.odnoklassniki.ru/oauth/token.do";}		//������� ��� �����
	
	
	public static String oficalSiteUrl(){return "http://mouserun.ru";}							//������� ��� �����
}
