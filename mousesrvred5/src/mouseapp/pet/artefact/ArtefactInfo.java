package mouseapp.pet.artefact;

public class ArtefactInfo {
	public int id;
	public String title;
	public String description;
	public int experience;
	public int popular;
	
	public ArtefactInfo(int id, String title, String description, int experience, int popular){
		this.id = id;
		this.title = title;
		this.description = description;
		this.experience = experience;
		this.popular = popular;
	}
}
