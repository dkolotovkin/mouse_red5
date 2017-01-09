package mouseapp.pet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mouseapp.pet.artefact.ArtefactInfo;

public class PetManagerParams {
	public Map<Integer, ArtefactInfo> artefacts = new HashMap<Integer, ArtefactInfo>();
	public List<Integer> petprices = Arrays.asList(500, 2000, 10000, 40000, 100000);
	public List<Integer> petfeedprices = Arrays.asList(20, 50, 200, 500, 1000);
	public List<Integer> needpetexperience = Arrays.asList(0, 20, 100, 500, 2000);
	
	public List<Double> probability = Arrays.asList(.2, .25, .3, .35, .4);
	
	public List<Integer> pet1artefacts = Arrays.asList(1);
	public List<Integer> pet2artefacts = Arrays.asList(1,1,1,2,3);
	public List<Integer> pet3artefacts = Arrays.asList(1,2,2,2,3,3,3,4,5);
	public List<Integer> pet4artefacts = Arrays.asList(1,2,2,3,3,4,4,4,4,5,5,5,5,6,7);
	public List<Integer> pet5artefacts = Arrays.asList(1,2,3,4,4,4,5,5,5,6,6,6,6,6,7,7,7,7,7,8);
	
	public PetManagerParams(){
		ArtefactInfo art;
		
		art = new ArtefactInfo(1, "������� ������", "+5 ����� � +5 ������������", 5, 5);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(2, "������� ������", "+10 ����� � +10 ������������", 10, 10);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(3, "������� ����", "+30 ����� � +20 ������������", 30, 20);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(4, "������� ������", "+50 ����� � +40 ������������", 50, 40);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(5, "������� ������ 1-�� ������", "+100 ����� � +80 ������������", 100, 80);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(6, "������� �����", "+200 ����� � +100 ������������", 200, 100);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(7, "������� ������", "+300 ����� � +200 ������������", 300, 200);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(8, "������� ������ 2-�� ������", "+500 ����� � +300 ������������", 500, 300);
		artefacts.put(art.id, art);
		
		art = null;
	}	
}
