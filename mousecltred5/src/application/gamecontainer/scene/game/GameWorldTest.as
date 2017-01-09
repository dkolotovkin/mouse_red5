package application.gamecontainer.scene.game
{
	import application.GameApplication;
	
	import flash.geom.Rectangle;
	
	import spark.components.Group;
	
	import utils.interfaces.ISceneContent;
	
	public class GameWorldTest extends GameWorld
	{	
		public function GameWorldTest(wrect:Rectangle):void
		{
			var loc:XML = new XML("<?xml version='1.0' encoding='UTF-8'?><scene><littlemouse x='39.5' y='354.4' width='25' height='28.35'/>	<minkskin x='711.25' y='356.85' width='35' height='26.3'/><cheeseskin x='347.25' y='289.8' width='37' height='21.9'/>	<staticskin x='370.8' y='374.9' width='740' height='10'/><staticskin x='4.8' y='189.9' width='10' height='380'/>	<staticskin x='735.8' y='189.9' width='10' height='380'/><staticskin x='370.8' y='3.9' width='740' height='10'/>	<staticskin x='157.85' y='359.9' width='70' height='10'/><staticskin x='223.85' y='342.9' width='70' height='10'/><staticskin x='286.8' y='328.75' width='70' height='10'/><staticskin x='346.75' y='312.75' width='70' height='10'/><staticskin x='407.75' y='326.75' width='70' height='10'/><staticskin x='471.75' y='338.75' width='70' height='10'/><staticskin x='536.75' y='352.9' width='70' height='10'/></scene>");
			var users:Array = new Array();
			users.push(GameApplication.app.userinfomanager.myuser.id);
			super(0, wrect, loc, users, -1);
		}		
	}
}