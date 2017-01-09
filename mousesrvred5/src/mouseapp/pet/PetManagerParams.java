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
		
		art = new ArtefactInfo(1, "«олота€ звезда", "+5 опыта и +5 попул€рности", 5, 5);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(2, "«олотые монеты", "+10 опыта и +10 попул€рности", 10, 10);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(3, "«олота€ нота", "+30 опыта и +20 попул€рности", 30, 20);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(4, "«олотое сердце", "+50 опыта и +40 попул€рности", 50, 40);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(5, "«олотое кольцо 1-го уровн€", "+100 опыта и +80 попул€рности", 100, 80);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(6, "«олотой кубок", "+200 опыта и +100 попул€рности", 200, 100);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(7, "«олотые слитки", "+300 опыта и +200 попул€рности", 300, 200);
		artefacts.put(art.id, art);
		art = new ArtefactInfo(8, "«олотое кольцо 2-го уровн€", "+500 опыта и +300 попул€рности", 500, 300);
		artefacts.put(art.id, art);
		
		art = null;
	}	
}
