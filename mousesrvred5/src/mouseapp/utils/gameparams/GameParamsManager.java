package mouseapp.utils.gameparams;

import mouseapp.user.AccessoryType;
import mouseapp.user.ColorType;

public class GameParamsManager {
	//����
	public static boolean isPen(int atype){
		if(atype == AccessoryType.PEN1 || atype == AccessoryType.PEN3 || atype == AccessoryType.PEN10) return true;
		return false;
	}
	//�������
	public static boolean isBandage(int atype){
		if(atype == AccessoryType.BANDAGE1 || atype == AccessoryType.BANDAGE3 || atype == AccessoryType.BANDAGE10) return true;
		return false;
	}
	//������
	public static boolean isCrone(int atype){
		if(atype == AccessoryType.CRONE1 || atype == AccessoryType.CRONE3 || atype == AccessoryType.CRONE10) return true;
		return false;
	}
	//�������
	public static boolean isCylinder(int atype){
		if(atype == AccessoryType.CYLINDER1 || atype == AccessoryType.CYLINDER3 || atype == AccessoryType.CYLINDER10) return true;
		return false;
	}
	//����� ������
	public static boolean isCookHat(int atype){
		if(atype == AccessoryType.COOKHAT1 || atype == AccessoryType.COOKHAT3 || atype == AccessoryType.COOKHAT10) return true;
		return false;
	}
	//������ ������
	public static boolean isHipHop(int atype){
		if(atype == AccessoryType.HIPHOP1 || atype == AccessoryType.HIPHOP3 || atype == AccessoryType.HIPHOP10) return true;
		return false;
	}
	//������ ������
	public static boolean isGlamur(int atype){
		if(atype == AccessoryType.GLAMUR1 || atype == AccessoryType.GLAMUR3 || atype == AccessoryType.GLAMUR10) return true;
		return false;
	}
	//����� ����������
	public static boolean isKovboyHat(int atype){
		if(atype == AccessoryType.KOVBOYHAT1 || atype == AccessoryType.KOVBOYHAT3 || atype == AccessoryType.KOVBOYHAT10) return true;
		return false;
	}
	//�������-�������
	public static boolean isFlashHair(int atype){
		if(atype == AccessoryType.FLASHHAIR1 || atype == AccessoryType.FLASHHAIR3 || atype == AccessoryType.FLASHHAIR10) return true;
		return false;
	}
	//�����	
	public static boolean isPumpkin(int atype){
		if(atype == AccessoryType.PUMPKIN1 || atype == AccessoryType.PUMPKIN3 || atype == AccessoryType.PUMPKIN10) return true;
		return false;
	}
	//���������� �����
	public static boolean isNYHat(int atype){
		if(atype == AccessoryType.NYHAT1 || atype == AccessoryType.NYHAT3 || atype == AccessoryType.NYHAT10) return true;
		return false;
	}
	//���������� ������
	public static boolean isNY(int atype){
		if(atype == AccessoryType.NY1 || atype == AccessoryType.NY3 || atype == AccessoryType.NY10) return true;
		return false;
	}
	//������ �������
	public static boolean isDoctor(int atype){
		if(atype == AccessoryType.DOCTOR1 || atype == AccessoryType.DOCTOR3 || atype == AccessoryType.DOCTOR10) return true;
		return false;
	}
	//������ ������
	public static boolean isAngel(int atype){
		if(atype == AccessoryType.ANGEL1 || atype == AccessoryType.ANGEL3 || atype == AccessoryType.ANGEL10) return true;
		return false;
	}
	//������ ������
	public static boolean isDemon(int atype){
		if(atype == AccessoryType.DEMON1 || atype == AccessoryType.DEMON3 || atype == AccessoryType.DEMON10) return true;
		return false;
	}
	//�����
	public static boolean isHelmet(int atype){
		if(atype == AccessoryType.HELMET1 || atype == AccessoryType.HELMET3 || atype == AccessoryType.HELMET10) return true;
		return false;
	}
	//�������
	public static boolean isPolicehat(int atype){
		if(atype == AccessoryType.POLICEHAT1 || atype == AccessoryType.POLICEHAT3 || atype == AccessoryType.POLICEHAT10) return true;
		return false;
	}
	//������ ����
	public static boolean isBlack(int ctype){
		if(ctype == ColorType.BLACK1 || ctype == ColorType.BLACK3 || ctype == ColorType.BLACK10) return true;
		return false;
	}
	//����� ����
	public static boolean isWhite(int ctype){
		if(ctype == ColorType.WHITE1 || ctype == ColorType.WHITE3 || ctype == ColorType.WHITE10) return true;
		return false;
	}
	//������� ����
	public static boolean isBlue(int ctype){
		if(ctype == ColorType.BLUE1 || ctype == ColorType.BLUE3 || ctype == ColorType.BLUE10) return true;
		return false;
	}
	//������� ����
	public static boolean isFiolet(int ctype){
		if(ctype == ColorType.FIOLET1 || ctype == ColorType.FIOLET3 || ctype == ColorType.FIOLET10) return true;
		return false;
	}
	//����� ����
	public static boolean isOrange(int ctype){
		if(ctype == ColorType.ORANGE1 || ctype == ColorType.ORANGE3 || ctype == ColorType.ORANGE10) return true;
		return false;
	}
	
	public static boolean getExperienceBonus(int atype, int ctype){
		if(
			isPen(atype) ||
			isBandage(atype) ||
			isCrone(atype) ||
			isCylinder(atype) ||
			isCookHat(atype) ||
			isKovboyHat(atype) ||
			isGlamur(atype) ||
			isHipHop(atype) ||
			isHelmet(atype) ||
			isPolicehat(atype) ||
			isFlashHair(atype) ||
//			isPumpkin(atype) ||
//			isNYHat(atype) ||
			isNY(atype) ||
			isDoctor(atype) ||
			isAngel(atype) ||
			isDemon(atype) ||
			isBlack(ctype) ||
			isWhite(ctype) ||
			isBlue(ctype) ||
			isFiolet(ctype) ||
			isOrange(ctype)
		){
			return true;
		}
		return false;
	}
	
	public static boolean getExperienceClanBonus(int atype, int ctype){
		if(
//			isPen(atype) ||
//			isBandage(atype) ||
//			isCrone(atype) ||
			isCylinder(atype) ||
			isCookHat(atype) ||
			isGlamur(atype) ||
			isHipHop(atype) ||
			isKovboyHat(atype) ||
			isHelmet(atype) ||
			isPolicehat(atype) ||
			isFlashHair(atype) ||
			isPumpkin(atype) ||
			isNYHat(atype) ||
			isNY(atype) ||
			isDoctor(atype) ||
			isAngel(atype) ||
			isDemon(atype) ||
//			isBlack(ctype) ||
//			isWhite(ctype) ||
			isBlue(ctype) ||
			isFiolet(ctype) ||
			isOrange(ctype)
		){
			return true;
		}
		return false;
	}
	
	public static boolean getMoneyBonus(int atype, int ctype){
		if(
//			isPen(atype) ||
//			isBandage(atype) ||
//			isCrone(atype) ||
//			isCylinder(atype) ||
//			isCookHat(atype) ||
//			isKovboyHat(atype) ||
//			isFlashHair(atype) ||
			isPumpkin(atype) ||
			isNYHat(atype) ||
			isNY(atype) ||
			isDoctor(atype) ||
			isAngel(atype) ||
			isDemon(atype) ||
			isBlack(ctype) ||
			isWhite(ctype)
//			isBlue(ctype) ||
//			isFiolet(ctype) ||
//			isOrange(ctype)
		){
			return true;
		}
		return false;
	}
}
