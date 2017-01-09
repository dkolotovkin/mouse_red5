package application.gamecontainer.scene.game
{
	import Box2D.Collision.Shapes.b2CircleDef;
	import Box2D.Collision.Shapes.b2MassData;
	import Box2D.Collision.Shapes.b2PolygonDef;
	import Box2D.Collision.Shapes.b2PolygonShape;
	import Box2D.Collision.Shapes.b2Shape;
	import Box2D.Collision.b2AABB;
	import Box2D.Common.Math.b2Vec2;
	import Box2D.Common.Math.b2XForm;
	import Box2D.Dynamics.Contacts.b2ContactEdge;
	import Box2D.Dynamics.Joints.b2Joint;
	import Box2D.Dynamics.Joints.b2MouseJoint;
	import Box2D.Dynamics.Joints.b2MouseJointDef;
	import Box2D.Dynamics.Joints.b2PrismaticJoint;
	import Box2D.Dynamics.b2Body;
	import Box2D.Dynamics.b2BodyDef;
	import Box2D.Dynamics.b2ContactListener;
	import Box2D.Dynamics.b2DebugDraw;
	import Box2D.Dynamics.b2World;
	
	import application.GameApplication;
	import application.GameMode;
	import application.components.findflash.FindFlash;
	import application.components.usertitle.UserTitle;
	import application.gamecontainer.scene.catalog.article.ArticleMovieClass;
	
	import flash.display.MovieClip;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Rectangle;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.text.StaticText;
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flash.text.engine.FontWeight;
	import flash.text.engine.Kerning;
	import flash.utils.clearInterval;
	import flash.utils.setInterval;
	
	import flashx.textLayout.formats.TextAlign;
	
	import mx.core.UIComponent;
	
	import org.flexunit.internals.namespaces.classInternal;
	
	import spark.components.Application;
	import spark.components.Group;
	import spark.components.VGroup;
	import spark.layouts.HorizontalAlign;
	import spark.primitives.Rect;
	
	import utils.game.GameContactListener;
	import utils.game.GameContactListenerEvent;
	import utils.game.PersFrameLabel;
	import utils.game.worldObject.GameWorldObject;
	import utils.interfaces.ISceneContent;
	import utils.shop.item.ItemType;
	import utils.user.User;
	
	public class GameWorld extends VGroup implements ISceneContent
	{
		private var _commongr:UIComponent = new UIComponent();
		private var _gameUI:UIComponent = new UIComponent();
		private var _mask:UIComponent = new UIComponent();		
		private var _gamebouns:b2AABB;
		private var _world:b2World;		
		private var kScale:Number = 1;
		private var _debugDraw:b2DebugDraw;		
		private var _doSleep:Boolean = true;		
		private var _m_iterations:int = 10;
		private var _m_timeStep:Number = 1.0 / 5;		
		private var _gravity:b2Vec2 = new b2Vec2(0, 20);
		private var _mouseVec:b2Vec2 = new b2Vec2();		
		private var _mouseJoint:b2MouseJoint;		
		private var _contactListener:GameContactListener = new GameContactListener();		
		private var _rightForceApllyed:Object = new Object();
		private var _leftForceApplyed:Object = new Object();		
		private var _creator:GameWorldCreator;		
		private var _urlLoader:URLLoader;
		private var _urlRequest:URLRequest;		
		protected var _locationXML:XML;
		
		public var users:Object = new Object(); 				//все b2body пользователей
		private var userIDs:Array = new Array();				//все id пользователей
		public var userstitles:Object = new Object();		
		
		private var _findsource:Boolean = false;
		private var _findexit:Boolean = false;
		
		public var roomID:int;		
		public var gametype:int;
		
		private var _heroX:Number;
		private var _heroY:Number;
		private var _myuserout:Boolean = false;
		private var _wrect:Rectangle = new Rectangle();
		
		private var _pressButtons:Object = new Object();
		public var createdItems:Object = new Object();
				
		private var _rightForce:int = 900;		
		private var _jumpForce:int = -1500 * 120;
		
		private var _freezemc:MovieClip;
		private var _freezetf:TextField;
		private var _freezetff:TextFormat = new TextFormat(null, 30, 0xffffff, FontWeight.BOLD, null, null, null, null, TextAlign.CENTER);
		private var _fsid:int;
		private var _freezecounter:int;
		private var _isfreeze:Boolean = false;
		
		private var _ismember:Boolean = false;
		
		public function get gameUI():UIComponent{
			return _gameUI;
		}
		
		public function GameWorld(roomID:int, wrect:Rectangle, locationXML:XML, usrs:Array, gt:int):void
		{
			super();
			this.roomID = roomID;
			this.gametype = gt;			
			_locationXML = locationXML;
			_wrect.x = wrect.x - 500;
			_wrect.y = wrect.y - 500;
			_wrect.width = wrect.width + 1000;
			_wrect.height = wrect.height + 1000;
			
			createWorld(_wrect.x, _wrect.y, _wrect.width, _wrect.height);
			_contactListener.world = _world;
			_creator = new GameWorldCreator(_world, _gameUI, userstitles);
			if (_locationXML){
				createWorldFromXML(_locationXML);
			}	
			
			
			var user:User;			
			for(var i:int = 0; i < usrs.length; i++){
				if(usrs[i] == GameApplication.app.userinfomanager.myuser.id){
					_ismember = true;
				}
				if(this.gametype == -1){							//test
					user = GameApplication.app.gameContainer.chat.getUserByID(usrs[i]);
				}else{
					user = GameApplication.app.gameContainer.chat.activeRoom.getUser(usrs[i]);
				}				
				if(user != null){
					users[int(user.id)] = _creator.createPers(_heroX, _heroY, 30, 30, user.colortype, user);
					userIDs.push(int(user.id));
				}
			}
			user = null;
			
			if(_ismember){
				_gameUI.addChild((users[GameApplication.app.userinfomanager.myuser.id] as b2Body).m_userData);
				_gameUI.addChild((userstitles[GameApplication.app.userinfomanager.myuser.id] as UserTitle));
				_contactListener.myusermc = (users[GameApplication.app.userinfomanager.myuser.id] as b2Body).m_userData;
			}
			
			//createDebugDraw(kScale, 1, 1, 0xFFFFFF);						
			
			_mask.graphics.beginFill(0x000000, .5);
			_mask.graphics.drawRect(0, 0, 740, 380);
			_mask.graphics.endFill();
			_gameUI.mask = _mask;
			
			_commongr.width = _gameUI.width = 740;
			_commongr.height = _gameUI.height = 380;
			addEventListener(Event.ADDED_TO_STAGE, addListeners, false, 0, true);
		}		
		
		private function addListeners(e:Event):void{				
			removeEventListener(Event.ADDED_TO_STAGE, addListeners);
			stage.addEventListener(Event.ENTER_FRAME, update, false, 0, true);
			if(GameApplication.app.config.mode == GameMode.DEBUG){
				stage.addEventListener(MouseEvent.MOUSE_DOWN, createMouseJoing);
			}
			stage.addEventListener(MouseEvent.MOUSE_UP,destroyMouseJoing);
			
			if(_ismember){
				stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
				stage.addEventListener(KeyboardEvent.KEY_UP, onKeyUp);
			}
			
			_world.SetContactListener(_contactListener);
			_contactListener.addEventListener(GameContactListenerEvent.MINK, onMinkContact);
			_contactListener.addEventListener(GameContactListenerEvent.CHEESE, onCheeseContact);
		}
		
		private function onMinkContact(e:GameContactListenerEvent):void{
			if (_findsource && !_findexit){
				var myuserbody:b2Body = users[GameApplication.app.userinfomanager.myuser.id];
				if(this.gametype != -1){		//test
					GameApplication.app.gamemanager.findexit(myuserbody.GetXForm().position.x, myuserbody.GetXForm().position.y);
				}else{
					GameApplication.app.gamemanager.endTestGame();
				}
				_findexit = true;
			}
		}
		private function onCheeseContact(e:GameContactListenerEvent):void{			
			if (!_findsource){				
				var myuserbody:b2Body = users[GameApplication.app.userinfomanager.myuser.id];
				if(this.gametype != -1){		//test
					GameApplication.app.gamemanager.findsource(myuserbody.GetXForm().position.x, myuserbody.GetXForm().position.y);
				}else{
					userFindSource(GameApplication.app.userinfomanager.myuser.id, myuserbody.GetXForm().position.x, myuserbody.GetXForm().position.y);
				}
				
				_findsource = true;
			}
		}
		
		protected function onKeyDown(event:KeyboardEvent):void 
		{			
			if(!_isfreeze){
				switch(int(event.keyCode))
				{
					case 65:
					case 37:{ 			//left
						if (!_pressButtons[event.keyCode]){
							_pressButtons[event.keyCode] = event.keyCode;
							var userb2bodyleft:b2Body = users[GameApplication.app.userinfomanager.myuser.id];
							if (userb2bodyleft && !_findexit){
								if(this.gametype != -1) GameApplication.app.gamemanager.goToLeft(true, userb2bodyleft.GetXForm().position.x, userb2bodyleft.GetXForm().position.y, userb2bodyleft.GetLinearVelocity().x, userb2bodyleft.GetLinearVelocity().y);
								userGotoLeft(GameApplication.app.userinfomanager.myuser.id, true, 0, 0, 0, 0, true);
							}
						}
						break;
					}
					case 68:
					case 39:{ 			//right
						if (!_pressButtons[event.keyCode]){						
							_pressButtons[event.keyCode] = event.keyCode;
							var userb2bodyright:b2Body = users[GameApplication.app.userinfomanager.myuser.id];
							if (userb2bodyright && !_findexit){
								if(this.gametype != -1) GameApplication.app.gamemanager.goToRight(true, userb2bodyright.GetXForm().position.x, userb2bodyright.GetXForm().position.y, userb2bodyright.GetLinearVelocity().x, userb2bodyright.GetLinearVelocity().y);
								userGotoRight(GameApplication.app.userinfomanager.myuser.id, true, 0, 0, 0, 0, true);						
							}
						}
						break;					
					}
					case 87:
					case 38:{ 			//up		
						if (!_pressButtons[event.keyCode]){
							_pressButtons[event.keyCode] = event.keyCode;
							var userb2body:b2Body = users[GameApplication.app.userinfomanager.myuser.id];
							if (userb2body && !_findexit){
								if(this.gametype != -1) GameApplication.app.gamemanager.jump(true, userb2body.GetXForm().position.x, userb2body.GetXForm().position.y, userb2body.GetLinearVelocity().x, userb2body.GetLinearVelocity().y);
								userJump(GameApplication.app.userinfomanager.myuser.id, true, 0, 0, 0, 0, true);						
							}
						}
					}
					case 40: break;{	//down
					}
					case 32:{ 			//space
						break;	
					}
					case 17: break;		//ctrl
					default: break;
				}
			}
		}
		
		protected function onKeyUp(event:KeyboardEvent):void
		{
			if(!_isfreeze && _pressButtons[event.keyCode]){
				delete _pressButtons[event.keyCode];
				switch(int(event.keyCode))
				{
					case 65:
					case 37:{ 			//left					
						var userb2bodyleft:b2Body = users[GameApplication.app.userinfomanager.myuser.id];
						if (userb2bodyleft && !_findexit){						
							if(this.gametype != -1) GameApplication.app.gamemanager.goToLeft(false, userb2bodyleft.GetXForm().position.x, userb2bodyleft.GetXForm().position.y, userb2bodyleft.GetLinearVelocity().x, userb2bodyleft.GetLinearVelocity().y);
							userGotoLeft(GameApplication.app.userinfomanager.myuser.id, false, 0, 0, 0, 0, true);						
						}					
						break;
					}
					case 68:
					case 39:{ 			//right
						var userb2bodyright:b2Body = users[GameApplication.app.userinfomanager.myuser.id];
						if (userb2bodyright && !_findexit){
							if(this.gametype != -1) GameApplication.app.gamemanager.goToRight(false, userb2bodyright.GetXForm().position.x, userb2bodyright.GetXForm().position.y, userb2bodyright.GetLinearVelocity().x, userb2bodyright.GetLinearVelocity().y);						
							userGotoRight(GameApplication.app.userinfomanager.myuser.id, false, 0, 0, 0, 0, true);						
						}
						break;
					}
					default:break;
				}
			}
		}
		
		public function userGotoLeft(userID:int, down:Boolean, userx:Number, usery:Number, lvx:Number, lxy:Number, ismyuser:Boolean = false):void{
			
			var userb2body:b2Body;
			var position:b2Vec2;
			if (down){
				if (_rightForceApllyed[userID]){
					delete _rightForceApllyed[userID];
				}
				if (!_leftForceApplyed[userID]){
					userb2body = users[userID];
					
					if (userb2body){
						position = new b2Vec2(userx, usery);						
						if (!ismyuser){
							userb2body.SetXForm(position, 0);
							userb2body.SetLinearVelocity(new b2Vec2(lvx, lxy));
						}
						
						_leftForceApplyed[userID] = userb2body;
						userb2body.m_userData.scaleX = -1;
					}
				}
			}else{
				delete _leftForceApplyed[userID];
				userb2body = users[userID];
				if(userb2body){
					position = new b2Vec2(userx, usery);						
					if (!ismyuser){
						userb2body.SetXForm(position, 0);
						userb2body.SetLinearVelocity(new b2Vec2(lvx, lxy));
					}
					
					userb2body.SetLinearVelocity(new b2Vec2(-15, userb2body.GetLinearVelocity().y));
					if (isGroundFromBody(userb2body, _world)){
						userb2body.m_userData.gotoAndStop(PersFrameLabel.STAND);
					}
				}
			}
		}
		
		public function userGotoRight(userID:int, down:Boolean, userx:Number, usery:Number, lvx:Number, lxy:Number, ismyuser:Boolean = false):void{
			var userb2body:b2Body;
			var position:b2Vec2;
			if (down){
				if (_leftForceApplyed[userID]){
					delete _leftForceApplyed[userID];
				}
				if (!_rightForceApllyed[userID]){
					userb2body = users[userID];
					if (userb2body){
						position = new b2Vec2(userx, usery);						
						if (!ismyuser){
							userb2body.SetXForm(position, 0);
							userb2body.SetLinearVelocity(new b2Vec2(lvx, lxy));
						}
						
						_rightForceApllyed[userID] = userb2body;
						userb2body.m_userData.scaleX = 1;
					}				
				}
			}else{
				delete _rightForceApllyed[userID];
				userb2body = users[userID];
				if(userb2body){
					position = new b2Vec2(userx, usery);						
					if (!ismyuser){
						userb2body.SetXForm(position, 0);
						userb2body.SetLinearVelocity(new b2Vec2(lvx, lxy));
					}
					
					userb2body.SetLinearVelocity(new b2Vec2(15, userb2body.GetLinearVelocity().y));
					if (isGroundFromBody(userb2body, _world)){
						userb2body.m_userData.gotoAndStop(PersFrameLabel.STAND);
					}
				}		
			}
		}
		
		public function userJump(userID:int, down:Boolean, userx:Number, usery:Number, lvx:Number, lxy:Number, ismyuser:Boolean = false):void{
			if (down){
				var userb2body:b2Body = users[userID];
				if(userb2body){
					var position:b2Vec2 = new b2Vec2(userx, usery);						
					if (!ismyuser){
						userb2body.SetXForm(position, 0);
						userb2body.SetLinearVelocity(new b2Vec2(lvx, lxy));
					}
					
					if (isGroundFromBody(userb2body, _world)){
						userb2body.SetLinearVelocity(new b2Vec2(userb2body.GetLinearVelocity().x, 0));
						userb2body.m_force.y = 0;
						userb2body.ApplyForce(new b2Vec2(0, _jumpForce * userb2body.m_userData["kjump"]), userb2body.GetLocalCenter());
						userb2body.m_userData.gotoAndStop(PersFrameLabel.JUMP);	
					}
				}			
			}
		}
		
		public function userFindSource(userID:int, userx:Number, usery:Number):void{
			if(GameApplication.app.userinfomanager.myuser.id == userID) _findsource = true;
			(userstitles[userID] as UserTitle).sourcevisible = true;
			var flash:FindFlash = new FindFlash(Flash);
			_gameUI.addChild(flash);
			flash.x = (users[userID] as b2Body).m_userData.x;
			flash.y = (users[userID] as b2Body).m_userData.y;
		}
		
		public function userFindExit(userID:int, userx:Number, usery:Number):void{
			if(GameApplication.app.userinfomanager.myuser.id == userID) _findexit = true;
			
			var flash:FindFlash = new FindFlash(Flash);
			_gameUI.addChild(flash);
			flash.x = (users[userID] as b2Body).m_userData.x;
			flash.y = (users[userID] as b2Body).m_userData.y;			
			
			(userstitles[userID] as UserTitle).visible = false;			
			((users[userID] as b2Body).m_userData as MovieClip).visible = false;			
		}
		
		public static function isGroundFromBody(body:b2Body, w:b2World):Boolean{			
			
			for (var cc:b2ContactEdge = body.m_contactList; cc; cc = cc.next) 
			{
				if (!body.GetShapeList().IsSensor() && !cc.other.GetShapeList().IsSensor()){
					var b_mc:MovieClip = (body.GetUserData() as MovieClip);
					var o_mc:MovieClip = (cc.other.GetUserData() as MovieClip);				
					
					if (b_mc && o_mc){
						var b_r:Rectangle = b_mc.getBounds(b_mc.parent);
						var o_r:Rectangle = o_mc.getBounds(o_mc.parent);			
						
						if (b_r.x < (o_r.x + o_r.width - 7) && (b_r.x + b_r.width) > o_r.x + 7){						
							var aabb:b2AABB = new b2AABB();
							aabb.lowerBound.Set(b_r.x + 7, b_r.y + b_r.height + 1);
							aabb.upperBound.Set(b_r.x + b_r.width - 7, b_r.y + b_r.height + 2);					
							
							var count:int = w.Query(aabb,new Array(), int.MAX_VALUE);						
							if (count > 0) return true;						
						}					
					}
				}
			}			
			return false;
		}	
		
		private function createWorld(minX:Number, minY:Number, maxX:Number, maxY:Number):void{
			_gamebouns = new b2AABB();
			_gamebouns.lowerBound.Set(minX, minY);
			_gamebouns.upperBound.Set(maxX, maxX);
			
			_world = new b2World(_gamebouns, _gravity, _doSleep);				
		}
		
		private function createWorldFromXML(xmlContent:XML):void
		{			
			var list:XMLList = xmlContent.elements("*");			
			
			for(var i:uint = 0; i < list.length(); i++)
			{
				if (list[i].name() == SceneElements.STATIC){
					_creator.createStaticBox(list[i].@x, list[i].@y, list[i].@width, list[i].@height, StaticSkin);
				}else if (list[i].name() == SceneElements.STATICRED){
					_creator.createStaticRedBox(list[i].@x, list[i].@y, list[i].@width, list[i].@height, StaticRedSkin);
				}else if (list[i].name() == SceneElements.STATICBLUE){
					_creator.createStaticBlueBox(list[i].@x, list[i].@y, list[i].@width, list[i].@height, StaticBlueSkin);
				}else if (list[i].name() == SceneElements.HEAVYBOX){
					_creator.createHeavyBox(list[i].@x, list[i].@y, list[i].@width, list[i].@height, HeavyBoxSkin);
				}else if (list[i].name() == SceneElements.BOX){
					_creator.createBox(list[i].@x, list[i].@y, list[i].@width, list[i].@height, BoxSkin);
				}else if (list[i].name() == SceneElements.STICK){
					_creator.createStick(list[i].@x, list[i].@y, list[i].@width, list[i].@height, StickSkin);
				}else if (list[i].name() == SceneElements.BALL){
					_creator.createBall(list[i].@x, list[i].@y, list[i].@width / 2, BallSkin);
				}else if (list[i].name() == SceneElements.SPRINGBROAD){
					_creator.CreateSpringboard(list[i].@x, list[i].@y, list[i].@width, list[i].@height, SpringboardSkin);
				}else if (list[i].name() == SceneElements.CHEESE){
					_creator.CreateCheese(list[i].@x, list[i].@y, list[i].@width, list[i].@height, CheeseSkin);
				}else if (list[i].name() == SceneElements.KERNELLEFT){
					_creator.createKernelLeft(list[i].@x, list[i].@y, list[i].@width / 2, KernelLeftSkin);
				}else if (list[i].name() == SceneElements.KERNELRIGHT){
					_creator.createKernelRight(list[i].@x, list[i].@y, list[i].@width / 2, KernelRightSkin);
				}else if (list[i].name() == SceneElements.MINK){
					_creator.CreateMink(list[i].@x, list[i].@y, list[i].@width, list[i].@height, MinkSkin);
				}else if (list[i].name() == SceneElements.CARRIERH){
					_creator.createCarrierH(list[i].@x, list[i].@y, list[i].@linewidth , list[i].@boxwidth, list[i].@boxheight, StaticBlackSkin);
				}else if (list[i].name() == SceneElements.CARRIERV){
					_creator.createCarrierV(list[i].@x, list[i].@y, list[i].@lineheight , list[i].@boxwidth, list[i].@boxheight, StaticBlackSkin);
				}else if (list[i].name() == SceneElements.MOUSESPOINT){
					_heroX = list[i].@x;
					_heroY = list[i].@y;
				}
			} 
		}
		public function createGameItem(itemtype:int, gwitemID:int, itemx:Number, itemy:Number, initiatorID:int = 0):void{
			var bounds:Rectangle = ArticleMovieClass.getGameItemBounds(itemtype);
			var movieclass:Class = ArticleMovieClass.getGameItemClass(itemtype);			
			if (itemtype == ItemType.BOX){
				createdItems[gwitemID] = _creator.createBox(itemx, itemy, bounds.width, bounds.height, movieclass);
			}else if (itemtype == ItemType.BALL){
				createdItems[gwitemID] = _creator.createBall(itemx, itemy, bounds.width / 2, movieclass);
			}else if (itemtype == ItemType.STATIC){
				createdItems[gwitemID] = _creator.createStaticBox(itemx, itemy, bounds.width, bounds.height, movieclass);
			}else if (itemtype == ItemType.HEAVYBOX){
				createdItems[gwitemID] = _creator.createHeavyBox(itemx, itemy, bounds.width, bounds.height, movieclass);
			}else if (itemtype == ItemType.STICK){
				createdItems[gwitemID] = _creator.createStick(itemx, itemy, bounds.width, bounds.height, movieclass);
			}else if (itemtype == ItemType.SPRINGBROAD){
				createdItems[gwitemID] = _creator.CreateSpringboard(itemx, itemy, bounds.width, bounds.height, movieclass);
			}else if (itemtype == ItemType.KERNELLEFT){
				createdItems[gwitemID] = _creator.createKernelLeft(itemx, itemy, bounds.width / 2, movieclass);
			}else if (itemtype == ItemType.KERNELRIGHT){
				createdItems[gwitemID] = _creator.createKernelRight(itemx, itemy, bounds.width / 2, movieclass);
			}else if (itemtype == ItemType.MAGIC_HAND){
				var flash:FindFlash = new FindFlash(Flash);
				_gameUI.addChild(flash);
				flash.x = createdItems[gwitemID].m_userData.x;
				flash.y = createdItems[gwitemID].m_userData.y;
				
				destroyBody(createdItems[gwitemID]);
				delete createdItems[gwitemID];
			}else if (itemtype == ItemType.GUN){
				var explot:FindFlash = new FindFlash(Explot);
				_gameUI.addChild(explot);
				
				if(gwitemID == GameApplication.app.userinfomanager.myuser.id){
					if(users[gwitemID] && !(users[gwitemID] as b2Body).GetUserData()["booking"]){
						_myuserout = true;
						GameApplication.app.gamemanager.userout();
					}
				}
				
				if(users[gwitemID] && !(users[gwitemID] as b2Body).GetUserData()["booking"]){
					explot.x = users[gwitemID].m_userData.x;
					explot.y = users[gwitemID].m_userData.y;				
					
					var bb:b2Body = users[gwitemID];
					if(bb && bb.m_userData)
					{
						var mass:b2MassData = new b2MassData();
						mass.mass = 0;
						bb.SetMass(mass);
						bb.SetXForm(new b2Vec2(-1000, -1000), 0);
						bb.m_userData.visible = false;
					}
					
					if(userstitles[gwitemID]){
						(userstitles[gwitemID] as UserTitle).visible = false;
					}
					delete users[gwitemID];
				}else{
					explot.x = itemx;
					explot.y = itemy;
				}
			}else if (itemtype == ItemType.FREEZE){
				if(initiatorID != GameApplication.app.userinfomanager.myuser.id){
					if(_freezemc && _gameUI.contains(_freezemc)){
						_gameUI.removeChild(_freezemc);
						_freezemc = null;
					}
					if(_freezetf && _gameUI.contains(_freezetf)){
						_gameUI.removeChild(_freezetf);
						_freezetf = null;
					}
					
					_freezecounter = 10;
					if(_fsid != -1){
						clearInterval(_fsid);
						_fsid = -1;
					}
					_fsid = setInterval(onFreeze, 1000, initiatorID)
					
					
					stage.dispatchEvent(new  KeyboardEvent(KeyboardEvent.KEY_UP, true, false, 0, 37));
					stage.dispatchEvent(new  KeyboardEvent(KeyboardEvent.KEY_UP, true, false, 0, 39));
					_isfreeze = true;
					
					
					_freezemc = new Freeze();
					_freezemc.width = 1000;
					_freezemc.height = 1000;
					
					_gameUI.addChild(_freezemc);
					
					_freezetf = new TextField();
					_freezetf.text = "00:" + _freezecounter;
					_freezetf.width = 760;
					_freezetf.y = 160;
					_freezetf.setTextFormat(_freezetff);
					
					_gameUI.addChild(_freezetf);
				}
			}else if (itemtype == ItemType.ANTIFREEZE){
				
				if(_freezemc && _gameUI.contains(_freezemc)){
					_gameUI.removeChild(_freezemc);
					_freezemc = null;
				}
				if(_freezetf && _gameUI.contains(_freezetf)){
					_gameUI.removeChild(_freezetf);
					_freezetf = null;
				}				
				
				if(_fsid != -1){
					clearInterval(_fsid);
					_fsid = -1;
				}
				_isfreeze = false;
			}				
		}
		
		private function onFreeze(initiatorID:int):void{			
			_freezecounter--;
			if(_freezecounter > 0){
				if(_freezetf){
					_freezetf.text = "00:0" + _freezecounter;			
					_freezetf.setTextFormat(_freezetff);
				}
			}else{
				if(_freezemc && _gameUI.contains(_freezemc)){
					_gameUI.removeChild(_freezemc);
					_freezemc = null;
				}
				if(_freezetf && _gameUI.contains(_freezetf)){
					_gameUI.removeChild(_freezetf);
					_freezetf = null;
				}				
				
				if(_fsid != -1){
					clearInterval(_fsid);
					_fsid = -1;
				}
				_isfreeze = false;
			}
		}
		
		public function getGameWorldObjects():Array{			//только динамические
			var arr:Array = new Array();
			/*var b2b:b2Body;
			var obj:GameWorldObject;
			for(var i:* in startItems){
				b2b = startItems[i];
				obj = new GameWorldObject(i, b2b.GetPosition().x, b2b.GetPosition().y, b2b.GetLinearVelocity().x, b2b.GetLinearVelocity().y, b2b.GetAngularVelocity(), b2b.GetAngle());
				arr.push(obj);
			}
			for(var j:* in createdItems){
				b2b = startItems[i];
				obj = new GameWorldObject(j, b2b.GetPosition().x, b2b.GetPosition().y, b2b.GetLinearVelocity().x, b2b.GetLinearVelocity().y, b2b.GetAngularVelocity(), b2b.GetAngle());
				arr.push(obj);
			}
			b2b = null;
			obj = null;*/
			return arr;
		}
		
		public function updateGameWorldObjects(arr:Array):void{
			/*var obj:GameWorldObject;
			for(var i:uint = 0; i < arr.length; i++){
				obj = GameWorldObject.createFromObject(arr[i]);
				if (startItems[obj.id] != null){
					(startItems[obj.id] as b2Body).SetXForm(new b2Vec2(obj.x, obj.y), obj.angle);
					(startItems[obj.id] as b2Body).SetLinearVelocity(new b2Vec2(obj.lvx, obj.lvy));
					(startItems[obj.id] as b2Body).SetAngularVelocity(obj.av);
				}
			}*/
		}
		
		private function createDebugDraw(drawScale:Number, fillAlpha:Number, linethckness:Number, drawFlags:uint):void{
			_debugDraw = new b2DebugDraw();
			var dbgSprite:Sprite = new Sprite();			
			_gameUI.addChild(dbgSprite);
			_debugDraw.m_sprite = dbgSprite;			
			_debugDraw.m_drawScale = drawScale;			
			_debugDraw.m_fillAlpha = fillAlpha;
			_debugDraw.m_lineThickness = linethckness;
			_debugDraw.m_drawFlags = drawFlags;
			_world.SetDebugDraw(_debugDraw);
		}	
		
		public function createMouseJoing(evt:MouseEvent):void {	
			var body:b2Body = GetBodyAtMouse();
			
			if (body) {
				var mouseJointDef:b2MouseJointDef = new b2MouseJointDef();
				mouseJointDef.body1 = _world.GetGroundBody();
				mouseJointDef.body2 = body;
				mouseJointDef.target.Set(mouseX / kScale, mouseY / kScale);
				mouseJointDef.maxForce = 3000000;
				mouseJointDef.timeStep = _m_timeStep;
				_mouseJoint = _world.CreateJoint(mouseJointDef) as b2MouseJoint;
			}
		}
		public function destroyMouseJoing(evt:MouseEvent):void {
			if (_mouseJoint) {
				_world.DestroyJoint(_mouseJoint);
				_mouseJoint = null;
			}
		}
		
		public function GetBodyAtMouse(includeStatic:Boolean=false):b2Body
		{
			var mouseXWorldPhys:Number = (mouseX) / kScale;
			var mouseYWorldPhys:Number = (mouseY) / kScale;
			_mouseVec.Set(mouseXWorldPhys, mouseYWorldPhys);
			var aabb:b2AABB = new b2AABB();
			aabb.lowerBound.Set(mouseXWorldPhys - 0.001, mouseYWorldPhys - 0.001);
			aabb.upperBound.Set(mouseXWorldPhys + 0.001, mouseYWorldPhys + 0.001);
			var k_maxCount:int = 10;
			var shapes:Array = new Array();
			var count:int = _world.Query(aabb,shapes,k_maxCount);
			var body:b2Body=null;
			
			for (var i:int = 0; i < count; ++i) {
				if (shapes[i].GetBody().IsStatic()==false||includeStatic) {
					var tShape:b2Shape = shapes[i] as b2Shape;
					var inside:Boolean = tShape.TestPoint(tShape.GetBody().GetXForm(),_mouseVec);
					if (inside) {
						body = tShape.GetBody();
						break;
					}
				}
			}
			return body;
		}
		
		public function destroyWorld():void
		{
			if(GameApplication.app.config.mode == GameMode.DEBUG){
				stage.removeEventListener(Event.ENTER_FRAME, update);
			}
			stage.removeEventListener(MouseEvent.MOUSE_DOWN,createMouseJoing);
			stage.removeEventListener(MouseEvent.MOUSE_UP,destroyMouseJoing);
			
			if(_ismember){
				stage.removeEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
				stage.removeEventListener(KeyboardEvent.KEY_UP, onKeyUp);
			}
			
			_contactListener.removeEventListener(GameContactListenerEvent.MINK, onMinkContact);
			_contactListener.removeEventListener(GameContactListenerEvent.CHEESE, onCheeseContact);
			
			_rightForceApllyed = null;
			_leftForceApplyed = null;
			createdItems = null;			
			
			for (var bb:b2Body = _world.m_bodyList; bb; bb = bb.m_next)
			{
				destroyBody(bb);
			}
			for (var jj:b2Joint = _world.m_jointList; jj; jj = jj.m_next){
				_world.DestroyJoint(jj);
			}
			
			for each(var elem:UserTitle in _gameUI){
				_gameUI.removeChild(elem);
			}
			_commongr.removeChild(_gameUI);
			_commongr.removeChild(_mask);
			
			removeElement(_commongr);
		}
		
		public function destroyBody(bb:b2Body): void 
		{
			if(bb.m_userData && _gameUI.contains(bb.m_userData))
			{
				_gameUI.removeChild(bb.m_userData);
				bb.m_userData = null;
			}
			_world.DestroyBody(bb);
		}
		
		public function update(e:Event):void
		{			
			//ПРОВЕРКА НЕ УПАЛ ЛИ МЫШЬ
			if (!_myuserout && !_findexit && _ismember){
				if ((users[GameApplication.app.userinfomanager.myuser.id] as b2Body).GetXForm().position.y > _wrect.y + _wrect.height ||
					(users[GameApplication.app.userinfomanager.myuser.id] as b2Body).GetXForm().position.y < _wrect.y ||
					(users[GameApplication.app.userinfomanager.myuser.id] as b2Body).GetXForm().position.x > _wrect.x + _wrect.width ||
					(users[GameApplication.app.userinfomanager.myuser.id] as b2Body).GetXForm().position.x < _wrect.x){
					_myuserout = true;
					GameApplication.app.gamemanager.userout();
					if(_ismember){
						stage.removeEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
						stage.removeEventListener(KeyboardEvent.KEY_UP, onKeyUp);
					}
				}
			}
			
			if (_mouseJoint)
			{
				var mouseXWorldPhys:Number = mouseX / kScale;
				var mouseYWorldPhys:Number = mouseY / kScale;
				var p2:b2Vec2 = new b2Vec2(mouseXWorldPhys,mouseYWorldPhys);
				_mouseJoint.SetTarget(p2);
			}
			
			for each(var bodyr:b2Body in _rightForceApllyed){				
				if (bodyr){				
					var force_r:int = _rightForce * bodyr.m_userData["kspeed"];
					if (!isGroundFromBody(bodyr, _world)) force_r /= 5;
					
					bodyr.ApplyForce(new b2Vec2((30 - bodyr.GetLinearVelocity().x) * force_r, bodyr.GetLinearVelocity().y), bodyr.GetLocalCenter());
					if (isGroundFromBody(bodyr, _world) && (bodyr.m_userData as MovieClip).currentFrameLabel == PersFrameLabel.STAND){
						bodyr.m_userData.gotoAndPlay(PersFrameLabel.RUN);
					}
				}
			}
			for each(var bodyl:b2Body in _leftForceApplyed){
				if (bodyl){
					var force_l:int = -_rightForce * bodyl.m_userData["kspeed"];
					if (!isGroundFromBody(bodyl, _world)) force_l /= 5;
					
					bodyl.ApplyForce(new b2Vec2((30 - Math.abs(bodyl.GetLinearVelocity().x)) * force_l, bodyl.GetLinearVelocity().y), bodyl.GetLocalCenter());
					if (isGroundFromBody(bodyl, _world) && (bodyl.m_userData as MovieClip).currentFrameLabel == PersFrameLabel.STAND){
						bodyl.m_userData.gotoAndPlay(PersFrameLabel.RUN);
					}
				}
			}
			
			_world.Step(_m_timeStep, _m_iterations);	
			for (var bb:b2Body = _world.m_bodyList; bb; bb = bb.m_next)
			{
				if (bb.m_userData is Sprite)
				{
					bb.m_userData.x = Math.round(bb.GetPosition().x * kScale);
					bb.m_userData.y = Math.round(bb.GetPosition().y * kScale);
					bb.m_userData.rotation = bb.GetAngle() * (180 / Math.PI);
					bb.WakeUp();
				}
			}
			
			for (var jj:b2Joint = _world.m_jointList; jj; jj = jj.m_next){
				if(jj is b2PrismaticJoint){
					var absdelda:Number;
					var realdelta:Number;
					
					//if horizontal carrier
					if(jj.m_body2.GetUserData().name == "toright" || jj.m_body2.GetUserData().name == "toleft"){
						absdelda = Math.abs((jj as b2PrismaticJoint).GetAnchor1().x - (jj as b2PrismaticJoint).GetAnchor2().x);
						realdelta = (jj as b2PrismaticJoint).GetUpperLimit();
						if(absdelda < 1){
							jj.m_body2.GetUserData().name = "toright";
							jj.m_body2.SetLinearVelocity(new b2Vec2(20, 0));
						}else if(Math.abs(absdelda - realdelta) < 1){
							jj.m_body2.GetUserData().name = "toleft";
							jj.m_body2.SetLinearVelocity(new b2Vec2(-20, 0));
						}else{
							if(jj.m_body2.GetUserData().name == "toright"){								
								jj.m_body2.SetLinearVelocity(new b2Vec2(20, 0));
							}else{
								jj.m_body2.SetLinearVelocity(new b2Vec2(-20, 0));
							}
						}							
					}else{
						//vertical carrier						
						absdelda = Math.abs((jj as b2PrismaticJoint).GetAnchor1().y - (jj as b2PrismaticJoint).GetAnchor2().y);
						realdelta = (jj as b2PrismaticJoint).GetUpperLimit();
						if(absdelda < 1){
							jj.m_body2.GetUserData().name = "totop";
							jj.m_body2.SetLinearVelocity(new b2Vec2(0, 10));
						}else if(Math.abs(absdelda - realdelta) < 1){
							jj.m_body2.GetUserData().name = "tobottom";
							jj.m_body2.SetLinearVelocity(new b2Vec2(0, -15));
						}else{
							if(jj.m_body2.GetUserData().name == "totop"){								
								jj.m_body2.SetLinearVelocity(new b2Vec2(0, 10));
							}else{
								jj.m_body2.SetLinearVelocity(new b2Vec2(0, -15));
							}
						}
					}
				}
			}
			
			for(var k:int = 0; k < userIDs.length; k++){
				var userbody:b2Body = users[userIDs[k]];
				if (userbody && userbody.m_userData){
					(userstitles[userIDs[k]] as UserTitle).x = -((userstitles[userIDs[k]] as UserTitle).width / 2) + userbody.m_userData.x;
					(userstitles[userIDs[k]] as UserTitle).y = userbody.m_userData.y - 50;					
				}
			}
		}
		
		override protected function createChildren():void{
			super.createChildren();			
			
			this.percentWidth = 100;
			horizontalAlign = HorizontalAlign.CENTER;
			_commongr.addChild(_gameUI);
			_commongr.addChild(_mask);
			addElement(_commongr);
		}
		
		public function onHide():void{			
		}
	}
}