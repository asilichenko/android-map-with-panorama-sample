# Android Google Map with Street View sample
The sample Android app demonstrates how to use a custom combination of Google Maps and StreetView.

<div style="align: center; display: flex;">
  <img src="/img/screenshot.jpg" style="height: 300px;"/>
  <img src="/img/interactions.jpg" style="height: 300px;"/>
</div>

## Maps API key
The app require that you add your own Google Maps API key:
* Get a Maps API key.
* Create a file in the **root directory of the project** called _secure.properties_ (this file **should NOT be under version control** to protect your API key).
* Add a single line to _secure.properties_ that looks like: _MAPS_API_KEY=YOUR_API_KEY_, where _YOUR_API_KEY_ is the API key you obtained in the first step.
* Build and run
