package utils.shop.artefact
{
	public class Artefact
	{
		public var id:int;
		public var type:int;
		public var title:String;
		public var description:String;
		public var experience:int;
		public var popular:int;
		
		public function Artefact(id:int, type:int, title:String, description:String, experience:int, popular:int):void{
			this.id = id;
			this.type = type;
			this.title = title;
			this.description = description;
			this.experience = experience;
			this.popular = popular;
		}
	}
}