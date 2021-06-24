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

 Version v3.5
 * update zigbee ONOFF cluster
 * update Health Check
 * remove binding since reporting handles this

 Version v3.4
 * update presentation with 'patch' to rename 'valve' to 'Zone x'
 * remove commands on, off
 * add command setValveDuration
 * update settings order and description
 * fix controllerStatus -> status

 Version v3.3
 * change to remotecontrol with components
 * health check -> ping

 Version v3.2
 * add zigbee constants
 * update to zigbee commands
 * tabs and trim whitespace

 Version v3.1
 * Change to work with standard ST automation options
 * use standard switch since custom attributes still don't work in automations
 * Add schedule minute times to settings
 * Add split cycle to settings
 * deprecate Spruce Scheduler compatibility

 Version v3.0
 * Update for new Samsung SmartThings app
 * update vid with status, message, rainsensor
 * maintain compatibility with Spruce Scheduler
 * Requires Spruce Valve as child device

 Version v2.7
 * added Rain Sensor = Water Sensor Capability
 * added Pump/Master
 * add "Dimmer" to Spruce zone child for manual duration

**/

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

//dth version
def getVERSION() {'v1.0 5-2021'}
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
	definition (name: "Spruce Wifi Controller", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity",
		ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, vid: "2914a12b-504f-344f-b910-54008ba9408f") {

		capability "Actuator"
		capability "Switch"
		capability "Sensor"
		//capability "Health Check"
		capability "heartreturn55003.status"
		capability "heartreturn55003.controllerState"
		capability "heartreturn55003.rainSensor"
		capability "heartreturn55003.valveDuration"

		//capability "Configuration"
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
			description: "Zone settings are configured in the Spruce app.\n\nRefresh the configuration changes by opening the Spruce Connect SmartApp and saving."
		input title: "Version", description: VERSION, displayDuringSetup: true, type: "paragraph", element: "paragraph"
	}
}

//----------------------zigbee parse-------------------------------//

// Parse incoming device messages to generate events
def generateEvent(description) {
	log.debug "generateEvent ${description}"
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
	//createChildDevices()
	parent.getValveConfiguration()
	initialize()
}

def uninstalled() {
	log.debug "uninstalled"
	removeChildDevices()
}

def updated() {
	log.debug "updated"
	initialize()
}

def initialize() {
	log.debug "initialize"
	sendEvent(name: "switch", value: "off", displayed: false)
	sendEvent(name: "controllerState", value: "off", displayed: false)
	sendEvent(name: "status", value: "Initialize")
	sendEvent(name: "rainSensor", value: "dry")
	if (device.latestValue("valveDuration") == null) sendEvent(name: "valveDuration", value: 10)

	//update zigbee device settings
	//response(setDeviceSettings() + setTouchButtonDuration() + setRainSensor() + refresh())
}

def createChildDevices(tempZoneMap) {
	log.debug "create children"
	log.debug "tempZoneMap ${tempZoneMap}"
	def pumpMasterZone = (pumpMasterZone ? pumpMasterZone.replaceFirst("Zone ","").toInteger() : null)

	//create, rename, or remove child
	tempZoneMap.each{
		//endpoint is offset, zone number +1
		//def endpoint = "${it.key}"
		def i = it.key.toInteger()

		def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${i}"}
		//create child
		if (!child) {
			def childLabel = "Zone$i"
			child = addChildDevice("Spruce Valve", "${device.deviceNetworkId}:${i}", device.hubId,
					[completedSetup: true, label: "${childLabel}", isComponent: true, componentName: "Zone$i", componentLabel: "Zone$i"])
			log.debug "${child}"
			child.sendEvent(name: "valve", value: "closed", displayed: false)
		}

	}

	state.oldLabel = device.label
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
def poll() { log.debug "poll" }
def refresh() { log.debug "refresh" }
def configure() { log.debug "configure" }

def setStatus(status) {
	if (DEBUG) log.debug "status ${status}"
	sendEvent(name: "status", value: status, descriptionText: "Initialized")
}

def setRainSensor() {
	if (DEBUG) log.debug "Rain sensor: ${rainSensorEnable}"
	sendEvent(name: "rainSensor", value: "dry")
	//if (rainSensorEnable) return zigbee.writeAttribute(BINARY_INPUT_CLUSTER, OUT_OF_SERVICE_IDENTIFIER, DataType.BOOLEAN, 1, [destEndpoint: 18])
	//else return zigbee.writeAttribute(BINARY_INPUT_CLUSTER, OUT_OF_SERVICE_IDENTIFIER, DataType.BOOLEAN, 0, [destEndpoint: 18])
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
	if (DEBUG) log.debug "state ${state.hasConfiguredHealthCheck} ${zigbee.ONOFF_CLUSTER}"
	parent.zoneOnOff(endpoint, 1, duration)
}

def valveOff(valueMap) {
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()

	sendEvent(name: "status", value: "${valueMap.label} turned off", descriptionText: "${valueMap.label} turned off")

	parent.zoneOnOff(endpoint, 0, 0)
}


//------------------end commands----------------------------------//

//get times from settings and send to controller, then start schedule
def startSchedule() {
	def startRun = false
	def runTime, totalTime=0
	def scheduleTimes = []

	for (i in 1..16) {
		def endpoint = i + 1
		//if (settings."z${i}" && settings."z${i}Duration" != null) {
		if (settings."z${i}Duration" != null) {
			runTime = Integer.parseInt(settings."z${i}Duration")
			totalTime += runTime
			startRun = true

			scheduleTimes.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint]))
		}
		else {
			scheduleTimes.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, 0, [destEndpoint: endpoint]))
		}
	}
	if (!startRun || totalTime == 0) return noSchedule()

	//start after scheduleTimes are sent
	scheduleTimes.push(zigbee.command(zigbee.ONOFF_CLUSTER, 1, "", [destEndpoint: 1]))
	sendEvent(name: "status", value: "Scheduled for ${totalTime}min(s)", descriptionText: "Start schedule ending in ${totalTime} mins")
	return scheduleTimes
}
