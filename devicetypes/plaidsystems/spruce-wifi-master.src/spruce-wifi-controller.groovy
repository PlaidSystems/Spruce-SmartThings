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
 * Update to use similar DTH as SmartThings Spruce Controller
 * includes custom capabilities and vid identical to published Gen1 Controller


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

//----------------------events-------------------------------//

def parse(String description) {
	log.debug "parse ${description}"

    sendEvent(name: "switch", value: "off", isStateChange: true)
	sendEvent(name: "controllerState", value: "off", isStateChange: true)
	sendEvent(name: "status", value: "Initialize", isStateChange: true)
	sendEvent(name: "rainSensor", value: "dry", isStateChange: true)
	sendEvent(name: "valveDuration", value: 10, isStateChange: true)
}

// Parse incoming device messages to generate events
def generateEvent(eventMap) {
	log.debug "generateEvent ${eventMap}"

	log.debug "${eventMap.name} : ${eventMap.value} : ${eventMap.descriptionText}"

	//find valve child device
    def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${eventMap.name}"}
    if (child) {
    	child.sendEvent(name: "valve", value: eventMap.value)

		def valveState = eventMap.value == "open" ? "on" : "off"
		sendEvent(name: "controllerState", value: valveState, isStateChange: true, displayed: false)
    }
	else if (eventMap.name == 0) {
		sendEvent(name: "controllerState", value: eventMap.value, isStateChange: true)
	}
	else if (eventMap.name == "status") {
		sendEvent(name: "status", value: eventMap.value, isStateChange: true)
	}
	else if (eventMap.name == "rainsensor") {
		sendEvent(name: "rainSensor", value: eventMap.value, isStateChange: true)
	}
	else if (eventMap.name == "pause") {
		sendEvent(name: "controllerState", value: "pause", isStateChange: true)
	}
	else if (eventMap.name == "resume") {
		sendEvent(name: "controllerState", value: "on", isStateChange: true)
	}
	//contact and motion have no effect since they will be handled by pause
	else log.debug "uncaught event >> ${eventMap}"

	//always update status message
	sendEvent(name: "status", value: eventMap.descriptionText, isStateChange: true)

}


//--------------------end events-------------------------------//

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
		def dni = "${device.deviceNetworkId}:${i}"
		def child = childDevices.find{it.deviceNetworkId == dni}
		def isChild = child != null ? true : false

		if (DEBUG) log.debug "child ${i} ${enabledZone} ${child}"

		//create child
		if (enabledZone && !isChild) {
			def childLabel = "Zone$i"
			child = addChildDevice("Spruce Valve", dni, device.hubId,
					[completedSetup: true, label: "${childLabel}", isComponent: true, componentName: "Zone$i", componentLabel: "Zone$i"])
			child.sendEvent(name: "valve", value: "closed", displayed: false)
		}
		//or remove
		else if (!enabledZone && isChild) {
			deleteChildDevice(dni)
		}
		else if (isChild) child.sendEvent(name: "valve", value: "closed", displayed: false)

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

def getValveDuration() {
	return (device.latestValue("valveDuration").toInteger())
}

def on() {
	log.debug "on"
	setControllerState("on")
}

def off() {
	log.debug "off"
	setControllerState("off")
}

//controllerState
def setControllerState(state) {
	if (DEBUG) log.debug "state ${state}"
	sendEvent(name: "controllerState", value: state, descriptionText: "Initialized")

	switch(state) {
		case "on":
			sendEvent(name: "switch", value: "on", displayed: false)
			runAllZones()
			break
		case "pause":
			pause()
			break
		case "resume":
			resume()
			break
		case "off":
			sendEvent(name: "switch", value: "off", displayed: false)
			turnOff()
			break
	}
}

//run all enabled zones
def runAllZones() {
	def duration = getValveDuration()

	if (DEBUG) log.debug "startSchedule all enabled zones for ${duration}"
	parent.runAll(duration)
}

//stop everything
def turnOff() {
	parent.sendStop()
}

//pause schedule
def pause() {
	log.debug "pause"
	sendEvent(name: "status", value: "paused schedule", descriptionText: "pause on")

	parent.sendPause(0)
}

//resume schedule
def resume() {
	log.debug "resume"
	sendEvent(name: "status", value: "resumed schedule", descriptionText: "resume on")

	parent.sendResume()
}


// Commands to zones/valves
def valveOn(valueMap) {
	//get endpoint from deviceNetworkId
	def zone = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()
	def duration = getValveDuration()

	sendEvent(name: "status", value: "${valueMap.label} on for ${duration}min(s)", descriptionText: "Zone ${valueMap.label} on for ${duration}min(s)")
	if (DEBUG) log.debug "valve on"
	parent.zoneOnOff(zone, 1, duration)
}

def valveOff(valueMap) {
	def zone = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()

	sendEvent(name: "status", value: "${valueMap.label} turned off", descriptionText: "${valueMap.label} turned off")
	if (DEBUG) log.debug "valve off"
	parent.zoneOnOff(zone, 0, 0)
}
