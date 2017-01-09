var filepas = "file:///c|/work/mouse";

var lastID;
var uin = 0;
var globallibs = new Object ();
var ids;
var errors = new Array ();
var faces = new Object ();
var exportSWF = false;

for (var i in  fl.documents){
	fl.trace ("-------------->  START "+fl.documents[i].name.split(".")[0]);
	start (fl.documents[i]);
}
if (errors.length > 0){
	fl.trace("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	fl.trace("++++++++++++++++  ERROR  ++++++++++++++++++++++  ERROR  ++++++++++++++++");
	fl.trace("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	for (var i = 0;i < errors.length;i++){
		traceError (errors[i].txt,errors[i].file);
	}
}


function start(){
	var layers = document.getTimeline().layers	
	var xml = "<?xml version='1.0' encoding='UTF-8'?>\r<scene>\r"
	
	for (i = layers.length - 1; i >= 0; i--){
		var layer  = layers[i];		
		if (layer.layerType == "normal"){			
			var frames = layer.frames;
			for (var j = 0; j < frames.length; j++){
				var frame = frames[j];
				var elements = frame.elements;
				for (var k = 0; k < elements.length; k++){
					var element = elements[k];					
					if (element.libraryItem){
						if(element.libraryItem.name.toLowerCase() == "carriervertical" ||
							element.libraryItem.name.toLowerCase() == "carrierhorizontal"){
							xml += "\t<" + element.libraryItem.name.toLowerCase();
							
							var framecarrier = element.libraryItem.timeline.layers[0].frames[0];
							for (var f = 0; f < framecarrier.elements.length; f++){
								var elementcarrier = framecarrier.elements[f];
								if (elementcarrier.libraryItem){
									if(elementcarrier.libraryItem.name.toLowerCase() == "staticblackskin"){										
										xml += " boxwidth='" + elementcarrier.width + "'";
										xml += " boxheight='" + elementcarrier.height + "'";
									}else if(elementcarrier.libraryItem.name.toLowerCase() == "prismaticline"){
										xml += String(" x='" + (element.x + elementcarrier.x) + "'");
										xml += String(" y='" + (element.y + elementcarrier.y) + "'");
										
										xml += " linewidth='" + elementcarrier.width + "'";
										xml += " lineheight='" + elementcarrier.height + "'";
									}									
								}
							}
							xml += "/>\r";						
						}else{						
							xml += "\t<" + element.libraryItem.name.toLowerCase();
							xml += " x='" + element.x + "'";
							xml += " y='" + element.y + "'";
							xml += " width='" + element.width + "'";
							xml += " height='" + element.height + "'";
							xml += "/>\r";
						}
					}else if(element.elementType == "text" && element.name == "timer"){
						xml += "\t<timer";
						xml += " time='" + element.getTextString() + "'";						
						xml += "/>\r";
					}
				} 
			}
		}
	}
	xml += "</scene>";
	FLfile.createFolder(filepas);
	FLfile.write(filepas+"/"+document.name+".xml",xml);
	fl.trace("->* " + xml);
}


function traceError (txt,file){
	fl.trace (file +" -----> "+txt);
}