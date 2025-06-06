![GitHub Workflow Status](https://img.shields.io/github/workflow/status/odin568/FE2_Kartengenerierung/Java%20CI%20with%20Gradle?style=plastic) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/odin568/FE2_Kartengenerierung?style=plastic)  

# FE2_Kartengenerierung
## Motivation
Die Feuerwehr Baudenbach möchte eine Alarmdepesche ausdrucken. Bisher wird nur das Fax ausgedruckt.  
FE2 bietet zwar die Möglichkeit eine Alarmdepesche auszudrucken, jedoch keine Kartenintegration. Hierfür wird der AlarmMonitor4 benötigt, der jedoch nicht die gewünschte Flexibilität bietet.  
Aus diesem Grund ist hier ein Service entstanden. Dieser bietet zwei Möglichkeiten:  
* Aufruf im Alarmablauf (Plugin [URL öffnen](https://alamos-support.atlassian.net/wiki/spaces/documentation/pages/219480774/URL+ffnen)). Die Karte wird dann als Bild abgelegt und kann in einem Jaspersoft Report eingebunden werden.  
* Jaspersoft Report kann Bilder auch direkt über eine URL aufrufen. Somit integriert sich dieser Service nativ in diese Lösung. 
## Technische Beschreibung
* Erstellt Karten mithilfe von [Google Static Maps API](https://developers.google.com/maps/documentation/maps-static/overview) 
  (API Key und optionaler Signing Key werden benötigt)
* Fragt Hydranten im Umkreis per API von [Wasserkarte.info](https://wasserkarte.info) ab (optional, API Key wird benötigt) und fügt sie als Marker in die Karte ein. 
  Dabei werden (bis zu 5) eigene Icons unterstützt, **müssen aber separat gehostet werden** (Pixelgröße beachten!). 
  Die Applikation prüft, ob die Icons erreichbar sind. Falls nicht: Fallback auf default icon.
* Erstellt Routenführung mithilfe von [Google Routes API](https://developers.google.com/maps/documentation/routes)
  (API Key wird benötigt)
* Gibt die Karte als HTTP Response zurück. Dadurch auch nutzbar im AM4 über Plugin [Website Ansicht](https://alamos-support.atlassian.net/wiki/spaces/documentation/pages/219480152/Website+Ansicht)
* Optional: Größe der Karte konfigurierbar über URL-Parameter size (z.B. &size=320x320 )  
* Optional: Speichert die Karten an beliebigem Ort auf der Festplatte. 
* Optional: Mit dem URL-Parameter 'identifier' kann ein Zieldateiname (ohne Dateiendung) angegeben werden.
## Funktionen
### Overview
*(Mit optionalen eigenen Icons)*  
Erstellt eine Übersichtskarte des Einsatzortes mit Hydranten, sowie dem letzten Teil der Anfahrtsstrecke.  
```http://localhost:8080/overview?lat=49.123&lng=10.500&size=640x640```
![Alt text](screenshots/readme/overview.png?raw=true "Generated overview with custom icons")
### Detail
*(Ohne eigene Icons)*  
Erstellt eine Detailkarte des Einsatzortes in Satellitenansicht inklusive Hydranten.  
```http://localhost:8080/detail?lat=49.123&lng=10.500&size=640x640```
![Alt text](screenshots/readme/detail.png?raw=true "Generated detail")
### Route
Erstellt eine Routenkarte vom Standort zum Einsatzort. Zeigt größere Wasservorkommen in der Umgebung an.  
```http://localhost:8080/route?lat=49.123&lng=10.500&size=640x640```
![Alt text](screenshots/readme/route.png?raw=true "Generated route")
### Generic
Erstellt eine generische Karte. Alle Parameter müssen von Hand gesetzt werden, und werden
1:1 an die [Google Static Maps API](https://developers.google.com/maps/documentation/maps-static/start) weitergegeben.  
Die einzige Ausnahme betrifft den Parameter *center*. Dieser muss getrennt als *lat* und *lng* angegeben werden.  
Folgende Parameter werden unterstützt: 
* lat, lng (bilden intern den Parmameter *center*)
* size
* scale (optional)
* zoom
* maptype
* language (optional)
* region (optional)
* style (optional, kann mehrfach gesetzt werden)
* markers (optional, kann mehrfach gesetzt werden)  

Des Weiteren werden Hydranten und Routen unterstützt:
* showHydrants (optional, default = false)
* showRoute (optional, default = false)

Da in der Regel keine POIs oder Bushaltestellen auf der Karte gebraucht werden, werden diese Styles standardmäßig deaktiviert, dieses Verhalten kann aber überschrieben werden:
* showPois (optional, default = false)

Für den Fall, dass die Karte abgespeichert werden soll, kann auch ein Identifier für den Dateinamen angegeben werden:
* identifier (optional, default = <empty>)
 
Beispiel:  
```http://localhost:8080/generic?lat=49.64703345265409&lng=10.566260347368512&size=640x640&scale=2&zoom=15&maptype=roadmap&showRoute=true&showHydrants=true&identifier=abc```

### Health check  
Bietet eine Monitoringschnittstelle zur Überwachung und zum Prüfen der Konfiguration.  
```http://localhost:8080/actuator/health```  
![Alt text](screenshots/readme/health.png?raw=true "Health check")
### Test
Bietet eine einfache Möglichkeit zum Testen und Debuggen inklusive Performancemessung.  
```http://localhost:8080/test```
## Installation
### Docker
Vorbedingung: Docker und Docker-Compose müssen installiert sein.  
Architekturen: ```linux/amd64``` und ```linux/arm64```  
* Lade [docker-compose.yml](https://github.com/odin568/FE2_Kartengenerierung/releases) herunter
* Passe Konfiguration an (**volumes**, **environments**)
* ```docker-compose up -d```
* Für Updates genügt es in Zukunft die Versionsnummer in der Datei *docker-compose.yml* anzupassen.
### Windows Service
*Realisiert durch [WinSW](https://github.com/winsw/winsw)*  
Vorbedingung: Java 21 (oder neuer) muss korrekt installiert sein (Path-Variable gesetzt)  
Überprüfen kann man das mit dem Befehl ```java -version``` auf der Kommandozeile.  
Um mehrere Java Versionen auf einem Rechner zu vermeiden, kann sowohl die PATH Variable gesetzt werden oder aber der absolute Pfad zur java.exe in der Konfigurationsdatei angegeben werden.
* Lade Archiv [FE2_Kartengenerierung_WinSW.zip](https://github.com/odin568/FE2_Kartengenerierung/releases) herunter und entpacke es.
* Passe Konfiguration in *FE2_Kartengenerierung.xml* an (**env**)
* Führe ```FE2_Kartengenerierung.exe install``` aus. Alternativ benutze bereitgestellte Skripte: ```install.bat```
* Für Updates genügt es in Zukunft den Service zu stoppen (```stop.bat```) und das aktuellste [FE2_Kartengenerierung.jar](https://github.com/odin568/FE2_Kartengenerierung/releases) herunterzuladen und die alte Datei zu ersetzen. Dann kann der Service wieder gestartet werden (```start.bat```).
## Konfiguration
Das Tool benötigt Konfiguration, insbesondere API-Keys. Des Weiteren gibt es optionale Schalter.  
Die gesamte Konfiguration erfolgt über Umgebungsvariablen, die entweder manuell oder über Docker/WinSW (s.o.) gesetzt werden.  
Eine Auflistung aller Optionen:  
```
###### OPTIONAL: If configured, application will cache results for 60 minutes.
gcp.caching.enabled=true

###### MANDATORY: The Google Cloud API Key authorized to access 'Maps Static API'
gcp.maps.apiKey=123456
  
###### OPTIONAL: If configured in Cloud console for static maps apiKey, sign each request for improved security.
gcp.maps.signingKey=123546
  
###### OPTIONAL: The Google Cloud API Key authorized to access 'Routes API' and the starting points for the route.
gcp.routes.apiKey=123456
gcp.routes.origin.lat=49.123
gcp.routes.origin.lng=10.123
  
###### OPTIONAL: A target folder to optionally save the image to
output.folder=C:\\temp\\maps\\
  
###### MANDATORY: The output format. Supported: png8,png32,gif,jpg,jpg-baseline
output.format=png32
  
###### OPTIONAL: The Wasserkarte.info access token
wk.token=123456
  
###### OPTIONAL: Custom icons per sourceType of Wasserkarte.info. The icons need to be hosted somewhere and must be reachable from Internet!
###### If not defined for any or all sourceTypeIds, fallback to default icon.
###### Example: <id>=<url>;<id2>=<url2> etc.
wk.customIcons=1=https://bit.ly/Hydrant16O.png;2=https://bit.ly/Hydrant16U.png;3=https://bit.ly/Hydrant16W.png

###### OPTIONAL: Provide custom port. Default, if omitted is 8080
server.port=8080
```
## Links
* [DockerHub](https://hub.docker.com/r/odin568/fe2_kartengenerierung) 
* [Alamos Forum](https://board.alamos-gmbh.com/viewtopic.php?f=24&t=6445)
