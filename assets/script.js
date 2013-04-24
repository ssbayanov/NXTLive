var requests = 0;

var A = 1;
var B = 7;
var C = 11;

function Create(){  
	if(navigator.appName == "Microsoft Internet Explorer"){  
		req = new ActiveXObject("Microsoft.XMLHTTP");}
	else{  
		req = new XMLHttpRequest();}  
	return req;  
} 

function Request(query, URI, Refresh) { 
	var req = Create();
	req.open('GET', URI , true );
	req.onRefresh = Refresh;
	//req.onreadystatechange = onStateChange;
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"); 
	req.send(query);  
}

function doAction(command){
	Request("", '/commands/'+command, null); 
}

function sendMessage(msg, mailbox){
	Request("", '/message/'+mailbox+'/'+msg, null); 
}

function ge(id) 
{ 
	return document.getElementById(id); 
}

function send(event){
	var target = event ? event.target : window.event.srcElement;
	doAction(target.attributes["command"].value);
}



/*function RefreshChecks(req){
	if(req.readyState == 4)
		if(req.responseXML != null){
			genChecksTable(req.responseXML);
			updateStat();}
		else
			showError("Ошибка парсинга XML таблицы чеков<br>"+req.responseText);
}*/