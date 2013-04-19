var requests = 0;

function doAction(query, URI, Refresh) { 
	var req = Create();
	req.open('GET', URI , true );
	req.onRefresh = Refresh;
	req.onreadystatechange = onStateChange;
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"); 
	req.send(query);  
}

function doAction(command){
	var query = ""; 
	Request(query, '/commands/'+command, null); 
}

function ge(id) 
{ 
	return document.getElementById(id); 
}

function send(){
	doAction(this.command)
}

/*function RefreshChecks(req){
	if(req.readyState == 4)
		if(req.responseXML != null){
			genChecksTable(req.responseXML);
			updateStat();}
		else
			showError("Ошибка парсинга XML таблицы чеков<br>"+req.responseText);
}*/