![GitHub Workflow Status](https://img.shields.io/github/workflow/status/odin568/FE2_Kartengenerierung/Java%20CI%20with%20Gradle?style=plastic) ![Docker Cloud Automated build](https://img.shields.io/docker/cloud/automated/odin568/fe2_kartengenerierung?style=plastic) ![Docker Cloud Build Status](https://img.shields.io/docker/cloud/build/odin568/fe2_kartengenerierung?style=plastic)

# FE2_Kartengenerierung
## Motivation
Die Feuerwehr Baudenbach möchte eine Alarmdepesche ausdrucken. Bisher wird nur das Fax ausgedruckt.  
FE2 bietet zwar die Möglichkeit eine Alarmdepesche auszudrucken, jedoch keine Kartenintegration. Hierfür wird der AlarmMonitor4 benötigt, der jedoch nicht die gewünschte Flexibilität bietet.  
Aus diesem Grund ist hier ein Service entstanden, der im Alarmablauf (Plugin [URL öffnen](https://alamos-support.atlassian.net/wiki/spaces/documentation/pages/219480774/URL+ffnen)) aufgerufen werden kann. Die gespeicherten Karten können dann mit regulärer FE2 Funktionalität in die Alarmdepesche integriert und final ausgedruckt werden. 
## Features
* Applikation bietet REST Services für verschiedene Karten und Dienste:  
  Overview: http://localhost:8080/overview?lat=49.123&lng=10.500  
  Route: http://localhost:8080/route?lat=49.123&lng=10.500  
  Health: http://localhost:8080/actuator/health  
  Test: http://localhost:8080/test
* Erstellt eine Übersichtskarte mithilfe von [Google Static Maps API](https://developers.google.com/maps/documentation/maps-static/overview) 
  (API Key und optionaler Signing Key werden benötigt)
* Fragt Hydranten im Umkreis per API von Wasserkarte.info ab und fügt sie als Marker in die Karte ein (optional). 
  Dabei werden (bis zu 5) eigene Icons unterstützt, müssen aber getrennt gehostet werden. 
  Die Applikation prüft, ob die Icons erreichbar sind. Falls nicht: Fallback auf default icon.
* Erstellt eine Routenkarte mithilfe von [Google Directions API](https://developers.google.com/maps/documentation/directions/overview)
  (API Key wird benötigt)
* Gibt die Karte als Response zurück.  
* Speicherung der Karten an beliebigem Ort (für Depeschendruck), optional abschaltbar (bspw. für Integration in andere Systeme).
## Beispiel
Mit custom icons (kann auch gemischt werden)  
![Alt text](screenshots/readme/overview.png?raw=true "Generated overview with custom icons")
Ohne custom icons (kann auch gemischt werden)  
![Alt text](screenshots/readme/overview_noicons.png?raw=true "Generated overview without custom icons")
Route  
![Alt text](screenshots/readme/route.png?raw=true "Generated route")
Health check  
![Alt text](screenshots/readme/health.png?raw=true "Health check")
## Installation
### Docker
Docker und Docker-Compose müssen installiert sein (Windows oder Linux)
* Lade ![docker-compose.yml](/deploy/docker/docker-compose.yml?raw=true "docker-compose.yml") herunter
* Passe Konfiguration an (**volumes**, **environments**)
* ```docker-compose up -d```
### Windows Service (WinSW)
Java muss installiert sein (Path-Variable gesetzt)
* Lade [Archiv](https://downgit.github.io/#/home?url=https://github.com/odin568/FE2_Kartengenerierung/tree/main/deploy/winsw&fileName=FE2_Kartengenerierung&rootDirectory=FE2_Kartengenerierung) herunter und entpacke es.
* Passe Konfiguration in *FE2_Kartengenerierung.xml* an (**env**)
* Lade aktuelles [FE2_Kartengenerierung.jar](https://github.com/odin568/FE2_Kartengenerierung/releases) in den selben Ordner herunter  
* ```./FE2_Kartengenerierung.bat install```
## Konfiguration
Das Tool benötigt Konfiguration, insbesondere API-Keys. Des Weiteren gibt es optionale Schalter.  
Die gesamte Konfiguration erfolgt über Umgebungsvariablen, die entweder manuell oder über Docker/WinSW (s.o.) gesetzt werden.  
Eine Auflistung aller Optionen:  
```
###### MANDATORY: The Google Cloud API Key authorized to access 'Maps Static API'
gcp.maps.apiKey=123456
  
###### OPTIONAL: If configured in Cloud console for static maps apiKey, sign each request for improved security.
gcp.maps.signingKey=123546
  
###### OPTIONAL: The Google Cloud API Key authorized to access 'Directions API' and the starting points for the route.
gcp.directions.apiKey=123456
gcp.directions.origin.lat=49.123
gcp.directions.origin.lng=10.123
  
###### MANDATORY: The target folder
output.folder=C:\\temp\\maps\\
  
###### MANDATORY: The output format. Supported: png8,png32,gif,jpg,jpg-baseline
output.format=png32
  
###### OPTIONAL: The Wasserkarte.info access token
wk.token=123456
  
###### OPTIONAL: Custom icons per sourceType of Wasserkarte.info. The icons need to be hosted somewhere and must be reachable from Internet!
###### If not defined for any or all sourceTypeIds, fallback to default icon.
###### Example: <id>=<url>;<id2>=<url2> etc.
wk.customIcons=1=https://bit.ly/Hydrant16O.png;2=https://bit.ly/Hydrant16U.png;3=https://bit.ly/Hydrant16W.png
```
## Links
* [DockerHub](https://hub.docker.com/r/odin568/fe2_kartengenerierung) 
* [Alamos Forum](https://board.alamos-gmbh.com/viewtopic.php?f=24&t=6445)
