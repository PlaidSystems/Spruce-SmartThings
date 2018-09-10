/**
 *  Sample code to turn a Spruce zone on and off based on moisture or time
 *	Set preferences
 *	Subscribe and unsubscribe to sensors
 *  Spruce specific commands:
 *		z1on, z1off, z2on, z2off, z3on, z3off, z4on,z4off up to 16
 *  	notify(status,message)
 */

definition(
    name: "Spruce Water on Demand",
    namespace: "plaidsystems",
    author: "plaidsystems",
    description: "Spruce Zone controlled by moisture",
    category: "My Apps",
    iconUrl: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX2Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX3Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    oauth: false)

preferences {
	section("Spruce GEN2/WIFI Water on Demand SmartApp\n ")
    section("Select a Spruce device to control:\n Controller: will turn on all zones for set duration\n Schedule: will turn on, duration has no effect\n Zone: will turn on based on set parameters") {
		input name: "switches", type: "capability.switch", multiple: false, required: true
        input name: "duration", title: "Duration", type: "number", required: false
	}
    
    section("Set a specific start time or use sensor control in the next section") {
		input name: "startTime", title: "Turn On Time?", type: "time", required: false
	}
    
    section("Sensor control options:\n Select a sensor to start or stop water") {
		
        input name: "sensor", value: "humidity", type: "capability.relativeHumidityMeasurement", multiple: false, required: false
        input name: "sensorlowon", title: "Turn On when Sensor is below?", type: "bool", required: false
        input "low", "number", title: "Turn On When Moisture is below?", required: false
        input name: "sensorhighoff", title: "Turn Off when Sensor is above?", type: "bool", required: false
        input "high", "number", title: "Turn Off When Moisture is above?", required: false
	}
    
    section("General Settings")
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	unschedule()
    unsubscribe()
    initialize()    
}

def updated(settings) {
	unschedule()
    unsubscribe()
    initialize()	             
}

def initialize(){
	//if start time set, set schedule for each day
    log.debug startTime
    if(startTime != null){
    	def runTime = timeToday(startTime, location.timeZone)
        schedule(runTime, startWater)   		        
    }
	//start moisture subscription
    subscriptions()
}

def subscriptions(){
	if(sensorlowon) subscribe(sensor, "humidity", humidityHandler)	//if sensor low setpoint is on, subscribe to sensor
}

//called whenever sensor reports value
def humidityHandler(evt){
    
    def soil = sensor.latestValue("humidity")    
    
    log.debug "Soil Moisture = $soil %"
    
    if (soil <= low && sensorlowon) startWater()
    if (soil >= high && sensorhighoff) stopWater()
    
}

//starts water
def startWater() {
	log.debug "start water"
    //only start 
    if(sensorhighoff) subscribe(sensor,"humidity",humidityHandler)
    
    try{switches.setLevel(duration)}
    catch($e){}
    
    switches.on()
}

def stopWater() {	
    if(sensorhighoff) unsubscribe()
    subscriptions()
    
    switches.off()    
}
