package utils.game
{
	import Box2D.Collision.b2AABB;
	import Box2D.Collision.b2ContactPoint;
	import Box2D.Common.Math.b2Vec2;
	import Box2D.Dynamics.Contacts.b2ContactEdge;
	import Box2D.Dynamics.b2Body;
	import Box2D.Dynamics.b2ContactListener;
	import Box2D.Dynamics.b2World;
	
	import application.gamecontainer.scene.game.GameWorld;
	
	import flash.display.MovieClip;
	import flash.geom.Rectangle;
	
	public class GameContactListener extends b2ContactListener
	{
		public var world:b2World;
		public var myusermc:MovieClip;
		
		public function GameContactListener()
		{
			super();
		}
		
		override public function Add(point:b2ContactPoint):void 
		{
			setFramePers(point.shape1.GetBody());
			setFramePers(point.shape2.GetBody());
			
			upadateForceElements(point.shape1.GetBody(), point.shape2.GetBody());
			checkSensors(point.shape1.GetBody(), point.shape2.GetBody());
			
			super.Add(point);
		}
		
		private function checkSensors(b1:b2Body, b2:b2Body):void{
			if(b1.GetUserData() && b1.GetUserData().name == "LittleMouse" && 
				b2.GetUserData() && b2.GetUserData().name == "Cheese"){
				if (myusermc && myusermc.hitTestObject(b2.GetUserData())){
					dispatchEvent(new GameContactListenerEvent(GameContactListenerEvent.CHEESE));
				}
			}
			if(b2.GetUserData() && b2.GetUserData().name == "LittleMouse" && 
				b1.GetUserData() && b1.GetUserData().name == "Cheese"){				
				if (myusermc && myusermc.hitTestObject(b1.GetUserData())){
					dispatchEvent(new GameContactListenerEvent(GameContactListenerEvent.CHEESE));
				}
			}
			
			if(b1.GetUserData() && b1.GetUserData().name == "LittleMouse" && 
				b2.GetUserData() && b2.GetUserData().name == "Mink"){
				if (myusermc && myusermc.hitTestObject(b2.GetUserData())){
					dispatchEvent(new GameContactListenerEvent(GameContactListenerEvent.MINK));
				}				
			}
			if(b2.GetUserData() && b2.GetUserData().name == "LittleMouse" && 
				b1.GetUserData() && b1.GetUserData().name == "Mink"){				
				if (myusermc && myusermc.hitTestObject(b1.GetUserData())){
					dispatchEvent(new GameContactListenerEvent(GameContactListenerEvent.MINK));
				}	
			}
		}
		
		private function upadateForceElements(b1:b2Body, b2:b2Body):void{
			if(b1.GetUserData() && b1.GetUserData().name == "LittleMouse" && 
				b2.GetUserData() && b2.GetUserData().name == "Springboard"){
				b1.SetLinearVelocity(new b2Vec2(b2.GetLinearVelocity().x,0));
				b1.ApplyForce(new b2Vec2(0,-1500 * 90), b1.GetLocalCenter());
				b1.m_userData.gotoAndStop(PersFrameLabel.JUMP);
			}
			if(b2.GetUserData() && b2.GetUserData().name == "LittleMouse" && 
				b1.GetUserData() && b1.GetUserData().name == "Springboard"){
				b2.SetLinearVelocity(new b2Vec2(b2.GetLinearVelocity().x,0));
				b2.ApplyForce(new b2Vec2(0,-1500 * 90), b2.GetLocalCenter());
				b2.m_userData.gotoAndStop(PersFrameLabel.JUMP);
			}
			/*
			if(b1.GetUserData() && b1.GetUserData().name == "LittleMouse" && 
				b2.GetUserData() && b2.GetUserData().name == "StaticBrownSkin"){
				//b1.SetLinearVelocity(new b2Vec2(b2.GetLinearVelocity().x,0));
				var speed:int = 100;
				if(b2.GetLinearVelocity().x < 0) speed *= -1;
				b2.ApplyForce(new b2Vec2(speed, b2.GetLinearVelocity().y), b2.GetLocalCenter());
				b2.m_userData.gotoAndStop(PersFrameLabel.RUN);
			}
			if(b2.GetUserData() && b2.GetUserData().name == "LittleMouse" && 
				b1.GetUserData() && b1.GetUserData().name == "StaticBrownSkin"){
				//b2.SetLinearVelocity(new b2Vec2(b2.GetLinearVelocity().x,0));
				var speed:int = 100;
				if(b2.GetLinearVelocity().x < 0) speed *= -1;
				b2.ApplyForce(new b2Vec2(speed, b2.GetLinearVelocity().y), b2.GetLocalCenter());
				b2.m_userData.gotoAndStop(PersFrameLabel.RUN);
			}*/
		}
		
		private function setFramePers(body:b2Body):void{				
			if(body.GetUserData() && body.GetUserData().name == "LittleMouse" && body.GetLinearVelocity().y != 0){
				if (GameWorld.isGroundFromBody(body, world)){
					body.GetUserData().gotoAndStop(PersFrameLabel.STAND);
				}				
			}
		}
	}
}