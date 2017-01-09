package mouseapp.user.pet;

public class PetInfo {
	public int id;
	public int level;
	public int experience;
	public int energy;
	public int changeenergyat;
	
	
	public PetInfo(int id, int level, int experience, int energy, int changeenergyat){
		this.id = id;
		this.level = level;
		this.experience = experience;
		this.energy = energy;
		this.changeenergyat = changeenergyat;
	}
}
