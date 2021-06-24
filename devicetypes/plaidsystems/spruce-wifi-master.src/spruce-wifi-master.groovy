/**
 *  Spruce Controller wifi master *
 *  Copyright 2018 Plaid Systems
 *
 *	
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *

 Version v2.0 5-2021
 * Convert for new SmartThings app

 Version v1.0 5-2018
 * Spruce Controller wifi master control tile
 * Manual Schedule tiles
 
 **/
 
import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

//dth version
def getVERSION() {'v2.0 5-2021'}
def getDEBUG() {false}
def getHC_INTERVAL_MINS() {60}
//zigbee cluster, attribute, identifiers
def getALARMS_CLUSTER() {0x0009}
def getBINARY_INPUT_CLUSTER() {0x000F}
def getON_TIME_ATTRIBUTE() {0x4001}
def getOFF_WAIT_TIME_ATTRIBUTE() {0x4002}
def getOUT_OF_SERVICE_IDENTIFIER() {0x0051}
def getPRESENT_VALUE_IDENTIFIER() {0x0055}

metadata {
	definition (name: "Spruce Controller", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity",
		ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, vid: "2914a12b-504f-344f-b910-54008ba9408f") {

		capability "Actuator"
		capability "Switch"
		capability "Sensor"
		capability "Health Check"
		capability "heartreturn55003.status"
		capability "heartreturn55003.controllerState"
		capability "heartreturn55003.rainSensor"
		capability "heartreturn55003.valveDuration"

		capability "Configuration"
		capability "Refresh"

		attribute "status", "string"
		attribute "controllerState", "string"
		attribute "rainSensor", "string"
		attribute "valveDuration", "NUMBER"

		command "setStatus"
		command "setRainSensor"
		command "setControllerState"
		command "setValveDuration"

        //old
        capability "Switch Level"
        capability "Actuator"
        capability "Valve"
        capability "Refresh"
        
        attribute 'connected', 'string'
        attribute 'pause', 'string'
        
        command "on"
        command "off"
        command "online"
        command "offline"
        command "resume"
        command "pause"        
        command "generateEvent"
        command "update_settings"
        command "activeLabel"
        
	}
    preferences {
		//general device settings
		input title: "Device settings", displayDuringSetup: true, type: "paragraph", element: "paragraph",
			description: "Zone settings are configured in the Spruce app.\n\nRefresh the configuration changes by opening the Spruce Connect SmartApp and saving."
		input title: "Version", description: VERSION, displayDuringSetup: true, type: "paragraph", element: "paragraph"
	}
    
    /*
    childDeviceTile('schedule1', 'schedule1', childTileName: "switch")
    childDeviceTile('schedule2', 'schedule2', childTileName: "switch")
    childDeviceTile('schedule3', 'schedule3', childTileName: "switch")
    childDeviceTile('schedule4', 'schedule4', childTileName: "switch")
    childDeviceTile('schedule5', 'schedule5', childTileName: "switch")
    */  
}

def installed(){    
    initialize()
}

def uninstalled() {
	log.debug "uninstalled"
}

def updated() {
	log.debug "updated"
	initialize()
}

def initialize(){
	log.debug "initialize master"    
	sendEvent(name: "controllerState", value: "off", displayed: false)
	sendEvent(name: "status", value: "Initialize")
    sendEvent(name: "rainSensor", value: "dry", displayed: false)
	if (device.latestValue("valveDuration") == null) sendEvent(name: "valveDuration", value: 10)	
}

//-------------------------schedule devices TODO fix----------------------//
def createChildDevices() {
    //get and delete children avoids duplicate children
    
    try {
    	def children = getChildDevices()
        children.each{
        log.debug it
        	deleteChildDevice(it.deviceNetworkId)
        }
    }
    catch (e) {
    	log.debug "no children"
        }
        
	parent.child_schedules(device.deviceNetworkId)
}

//add schedule child devices
void createScheduleDevices(id, i, schedule, schName){
	log.debug "master child devices"
    log.debug schedule
    //def device = "${device.deviceNetworkId}".split('.')
    log.debug "${id}.${i}"
    
    //add children
    addChildDevice("Spruce wifi schedule", "${id}.${i}", null, [completedSetup: true, label: "${schName}", isComponent: true, componentName: "schedule${i}", componentLabel: "${schName}"])
}
//-------------------------end schedule devices TODO fix----------------------//


def generateEvent(Map results) {
    def currentStatus = device.currentValue('status')
    log.debug "master status: ${currentStatus}"
    log.debug "master results: ${results}"
    
	//status dependent events
    if(currentStatus == 'active'){
        def messageCurrent = device.latestValue('tileMessage')
        def message = messageCurrent.split('\n')
        log.debug "message[0] ${message[0]}"
        sendEvent(name: "tileMessage", value: "${message[0]}\n${results.descriptionText}", displayed: false)
    }    
    else if (results.name == 'status'){
    	sendEvent(name: "${results.name}", value: "${results.value}", descriptionText: "${results.descriptionText}", displayed: true)
        sendEvent(name: "tileMessage", value: "${results.descriptionText}", displayed: false)
    }
    
	//all events
    if (results.name == "amp"){
        sendEvent(name: "${results.name}", value: "${results.value}", descriptionText: "${results.descriptionText}", displayed: false)
    }  
    if (results.name == "rainsensor"){
        sendEvent(name: "${results.name}", value: "${results.value}", descriptionText: "${results.descriptionText}", displayed: true)
    }
    if (results.name == "pause"){
        sendEvent(name: "${results.name}", value: "${results.value}", descriptionText: "${results.descriptionText}", displayed: true)
        if(results.value == 'on')sendEvent(name: "status", value: "pause", displayed: false)
        if(results.value == 'off')sendEvent(name: "status", value: "active", displayed: false)
    }
    if (results.name == "switch"){
        if (results.value == "on") switchon(results)
        else off()
        sendEvent(name: "${results.name}", value: "${results.value}", descriptionText: "${results.descriptionText}", displayed: true)
        if(currentStatus != 'active')sendEvent(name: "tileMessage", value: "${results.descriptionText}", displayed: false)
    }
    if (results.name == "contact"){
        sendEvent(name: "${results.name}", value: "${results.value}", descriptionText: "${results.descriptionText}", displayed: true)
    } 

  
}

//---------------------custom commands---------------------------------//
def setStatus(status) {
	if (DEBUG) log.debug "status ${status}"
	sendEvent(name: "status", value: status, descriptionText: "Initialized")
}

def setRainSensor() {
	if (DEBUG) log.debug "Rain sensor: ${rainSensor}"

	sendEvent(name: "rainSensor", value: "dry", displayed: false)
}

def setValveDuration(duration) {
	if (DEBUG) log.debug "Valve Duration set to: ${duration}"

	sendEvent(name: "valveDuration", value: duration, displayed: false)
}

//controllerState
def setControllerState(state) {
	if (DEBUG) log.debug "state ${state}"
	sendEvent(name: "controllerState", value: state, descriptionText: "Initialized")

	switch(state) {
		case "on":
			if (!rainDelay()) {
				sendEvent(name: "switch", value: "on", displayed: false)
				sendEvent(name: "status", value: "initialize schedule", descriptionText: "initialize schedule")
				startSchedule()
			}
			break
		case "off":
			sendEvent(name: "switch", value: "off", displayed: false)
			scheduleOff()
			break
		case "pause":
			pause()
			break
		case "resume":
			resume()
			break
	}
}

//---------------------end custom commands---------------------------------//

//set minutes
def setLevel(percent) {
	log.debug "setLevel: ${percent}"
	sendEvent(name: "level", value: percent, displayed: false)
}

//set rainSensor
def setRain(value) {
	log.debug "setRain: ${value}"
	sendEvent(name: "rainsensor", value: value, displayed: false)
}

//set Pause
def setPause(value) {
	log.debug "setPause: ${value}"
	sendEvent(name: "pause", value: value, displayed: false)
}

//set Connected
def setConnected(value) {
	log.debug "setConnected: ${value}"
	sendEvent(name: "connected", value: value, displayed: false)
}

//************* Commands to/from pause and schedule children *******************
def zoneon(dni) {	
    log.debug "step 1"
   	def childDevice = childDevices.find{it.deviceNetworkId == dni}    
    
    if (childDevice.currentValue('switch') != 'on'){
    	log.debug "master zoneon ${childDevice} ${dni} on"
    	def result = [name: 'switch', value: 'on', descriptionText: "zone is on", isStateChange: true, displayed: true]    
    	childDevice.sendEvent(result)
        
        if("${childDevice}" != "Spruce Pause") parent.scheduleOnOff(childDevice, 1)
    	else pause()
    }
}

def zoneoff(dni) {    
    def childDevice = childDevices.find{it.deviceNetworkId == dni}
    
    if (childDevice.currentValue('switch') != 'off'){
    	log.debug "master zoneoff ${childDevice} off"
    	def result = [name: 'switch', value: 'off', descriptionText: "zone is off", isStateChange: true, displayed: true]
    	childDevice.sendEvent(result)
        
        if("${childDevice}" != "Spruce Pause") parent.scheduleOnOff(childDevice, 0)
    	else resume()
    }
}

void switchon(results){
	sendEvent(name: "status", value: 'active', descriptionText: "${results.descriptionText}", displayed: false)
    //sendEvent(name: "tileMessage", value: 'watering...', descriptionText: "Spruce is watering", displayed: false)
}

void on(){
	def runtime = device.latestValue('level') * 60
	parent.runAll(runtime)
}

void off(){	
    sendEvent(name: "switch", value: 'off', descriptionText: "${device.label} is off", displayed: false)
    sendEvent(name: "status", value: 'ready', descriptionText: "${device.label} is off", displayed: true)
    sendEvent(name: "tileMessage", value: 'Idle', descriptionText: "${device.label} is off", displayed: false)
    allSchedulesOff()
    parent.send_stop()
}

void allSchedulesOff(){
	def children = getChildDevices()
    children.each { child ->
        //log.debug "child ${child.displayName} has deviceNetworkId ${child.deviceNetworkId}"
        def result = [name: 'switch', value: 'off', descriptionText: "${child.displayName} is off", isStateChange: true, displayed: true]    
    	child.sendEvent(result)
    }    	
}

void refresh(){	   
    parent.getsettings()
}

void online(){
	sendEvent(name: "connected", value: 'online', descriptionText: "Spruce is Online", displayed: true)    
    parent.getsettings()
}

void offline(){
	sendEvent(name: "connected", value: 'offline', descriptionText: "Spruce is Offline", displayed: true)
    parent.getsettings()
}


void pause(){
	//def runtime = device.latestValue('level')
	sendEvent(name: "contact", value: 'open', descriptionText: "Contact Pause", displayed: true)
    parent.send_pause()
}

void resume(){	
    sendEvent(name: "contact", value: 'closed', descriptionText: "Resume", displayed: true)
    parent.send_resume()
}