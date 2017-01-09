package utils.user
{
	public class PetInfo
	{
		public var id:int;
		[Bindable]
		public var level:int;
		[Bindable]
		public var experience:int;
		[Bindable]
		public var energy:int;
		public var changeenergyat:int;
		
		
		public function PetInfo(id:int, level:int, experience:int, energy:int, changeenergyat:int){
			this.id = id;
			this.level = level;
			this.experience = experience;
			this.energy = energy;
			this.changeenergyat = changeenergyat;		
		}
	}
}