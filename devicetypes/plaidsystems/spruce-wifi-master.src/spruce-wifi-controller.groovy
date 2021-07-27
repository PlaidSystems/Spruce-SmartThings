/**
 *  Copyright 2020 PlaidSystems
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

 Version v1.0
 * Update to use similar DTH as SmartThings Spruce Controller, including custom capabilities
 * 
 
**/

import groovy.json.JsonOutput

//dth version
def getVERSION() {"v1.0 7-2021"}
def getDEBUG() {true}
def getHC_INTERVAL_MINS() {60}

metadata {
	definition (name: "Spruce Wifi Controller", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity",
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
		
	}

	preferences {
		//general device settings
		input title: "Device settings", displayDuringSetup: true, type: "paragraph", element: "paragraph",
			description: "Zone and Schedule settings are configured in the Spruce app."
		input title: "Version", description: VERSION, displayDuringSetup: true, type: "paragraph", element: "paragraph"
	}
}

//----------------------zigbee parse-------------------------------//

def parse(String description) {
	log.debug "parse ${description}"        
    
    sendEvent(name: "switch", value: "off", isStateChange: true)
	sendEvent(name: "controllerState", value: "off", isStateChange: true)
	sendEvent(name: "status", value: "Initialize", isStateChange: true)
	sendEvent(name: "rainSensor", value: "dry", isStateChange: true)
	sendEvent(name: "valveDuration", value: 10, isStateChange: true)
}

// Parse incoming device messages to generate events
def generateEvent(description) {
	log.debug "generateEvent ${description}"
    
    def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${description.name}"}
    if (child) {
    	child.sendEvent(name: "valve", value: description.value)
    }
	/*
	def result = []
	def endpoint, value, command
	def map = zigbee.parseDescriptionAsMap(description)
	if (DEBUG && !map.raw) log.debug "map ${map}"

	if (description.contains("on/off")) {
		command = 1
		value = description[-1]
	}
	else {
		endpoint = ( map.sourceEndpoint == null ? zigbee.convertHexToInt(map.endpoint) : zigbee.convertHexToInt(map.sourceEndpoint) )
		value = ( map.sourceEndpoint == null ? zigbee.convertHexToInt(map.value) : null )
		command = (value != null ? commandType(endpoint, map.clusterInt) : null)
	}

	if (DEBUG && command != null) log.debug "${command} >> endpoint ${endpoint} value ${value} cluster ${map.clusterInt}"
	switch (command) {
	  case "alarm":
		result.push(createEvent(name: "status", value: "alarm"))
		break
	  case "schedule":
		def scheduleValue = (value == 1 ? "on" : "off")
		def scheduleState = device.latestValue("controllerState")
		def scheduleStatus = device.latestValue("status")

		if (scheduleState == "pause") log.debug "pausing schedule"
		else {
			if (scheduleStatus != "off" && scheduleValue == "off") result.push(createEvent(name: "status", value: "Schedule ${scheduleValue}"))
			result.push(createEvent(name: "controllerState", value: scheduleValue))
			result.push(createEvent(name: "switch", value: scheduleValue, displayed: false))
		}
		break
	  case "zone":
	  	def onoff = (value == 1 ? "open" : "closed")
		def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${endpoint}"}
		if (child) child.sendEvent(name: "valve", value: onoff)

		if (device.latestValue("controllerState") == "off") return setTouchButtonDuration()
		break
	  case "rainsensor":
		def rainSensor = (value == 1 ? "wet" : "dry")
		if (!rainSensorEnable) rainSensor = "disabled"
		result.push(createEvent(name: "rainSensor", value: rainSensor))
		break
	  case "refresh":
		//log.debug "refresh command not used"
		break
	  default:
	  	//log.debug "not used command"
		break
	}

	return result
	*/
}


//--------------------end zigbee parse-------------------------------//

def installed() {
	//get configuration
	parent.getValveConfiguration()	
}

def uninstalled() {
	log.debug "uninstalled"
	removeChildDevices()
}

def updated() {
	log.debug "updated"
	parent.getValveConfiguration()
	initialize()
}

def initialize() {
	log.debug "initialize"
	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)    
    updateDataValue("manufacturer", "Plaid Systems")
    
    sendEvent(name: "switch", value: "off", isStateChange: true)
	sendEvent(name: "controllerState", value: "off", isStateChange: true)
	sendEvent(name: "status", value: "Initialize", isStateChange: true)
	sendEvent(name: "rainSensor", value: "dry", isStateChange: true)
	sendEvent(name: "valveDuration", value: 10, isStateChange: true)
}

def createChildDevices(zoneMap) {
	log.debug "create children"
	if (DEBUG) log.debug "zoneMap ${zoneMap}"	

	//create or remove child	
    for (i in 1..16) {
		//check if zone enabled
		def enabledZone = zoneMap.containsKey("${i}")
		//look for current child
		def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${i}"}

		if (DEBUG) log.debug "child ${i} ${enabledZone} ${child}"

		//create child
		if (enabledZone && !child) {
			def childLabel = "Zone$i"
			child = addChildDevice("Spruce Valve", "${device.deviceNetworkId}:${i}", device.hubId,
					[completedSetup: true, label: "${childLabel}", isComponent: true, componentName: "Zone$i", componentLabel: "Zone$i"])			
			child.sendEvent(name: "valve", value: "closed", displayed: false)
		}
		//or remove
		else if (!enabledZone && child) {
			deleteChildDevice(child)
		}

	}
	
}

def removeChildDevices() {
	log.debug "remove all children"

	//get and delete children avoids duplicate children
	def children = getChildDevices()
	if (children != null) {
		children.each{
			deleteChildDevice(it.deviceNetworkId)
		}
	}
}


//----------------------------------commands--------------------------------------//

// handle commands
def ping() { log.debug "ping" }
def refresh() { log.debug "refresh" }
def configure() { log.debug "configure" }

def setStatus(status) {
	if (DEBUG) log.debug "status ${status}"
	sendEvent(name: "status", value: status, descriptionText: "Initialized")
}

def setRainSensor() {
	if (DEBUG) log.debug "Rain sensor: ${rainSensorEnable}"
	sendEvent(name: "rainSensor", value: "dry")
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

//on & off from switch
def on() {
	log.debug "switch on"
	setControllerState("on")
}

def off() {
	log.debug "switch off"
	setControllerState("off")
}

def pause() {
	log.debug "pause"
	sendEvent(name: "switch", value: "off", displayed: false)
	sendEvent(name: "status", value: "paused schedule", descriptionText: "pause on")
	scheduleOff()
}

def resume() {
	log.debug "resume"
	sendEvent(name: "switch", value: "on", displayed: false)
	sendEvent(name: "status", value: "resumed schedule", descriptionText: "resume on")
	scheduleOn()
}

//set raindelay
def rainDelay() {
	if (rainSensorEnable && device.latestValue("rainSensor") == "wet") {
		sendEvent(name: "switch", value: "off", displayed: false)
		sendEvent(name: "controllerState", value: "off")
		sendEvent(name: "status", value: "rainy")
		return true
	}
	return false
}

//set schedule
def noSchedule() {
	sendEvent(name: "switch", value: "off", displayed: false)
	sendEvent(name: "controllerState", value: "off")
	sendEvent(name: "status", value: "Set schedule in settings")
}

//schedule on/off
def scheduleOn() {
	
}
def scheduleOff() {
	
}

// Commands to zones/valves
def valveOn(valueMap) {
	//get endpoint from deviceNetworkId
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()
	def duration = (device.latestValue("valveDuration").toInteger())

	sendEvent(name: "status", value: "${valueMap.label} on for ${duration}min(s)", descriptionText: "Zone ${valueMap.label} on for ${duration}min(s)")
	if (DEBUG) log.debug "valve on"
	parent.zoneOnOff(endpoint, 1, duration)
}

def valveOff(valueMap) {
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()

	sendEvent(name: "status", value: "${valueMap.label} turned off", descriptionText: "${valueMap.label} turned off")
	if (DEBUG) log.debug "valve off"
	parent.zoneOnOff(endpoint, 0, 0)
}


//------------------end commands----------------------------------//

//get times from settings and send to controller, then start schedule
def startSchedule() {
	if (DEBUG) log.debug "startSchedule"
}
