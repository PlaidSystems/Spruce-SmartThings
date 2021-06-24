/**
 *  Spruce Controller wifi zone child *
 *  Copyright 2017 Plaid Systems
 *
 *	Author: NC
 *	Date: 2017-6
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
 -------------5-2018 update---------------
 * zone child for wifi controller
 * 
 */
 
 metadata {
	definition (name: 'Spruce wifi zone', namespace: 'plaidsystems', author: 'Plaid Systems') {		
        capability "Actuator"
        capability "Valve"
        capability "Sensor"
        
        command "on"
        command "off"
        command "updated"
        command "refresh"
        command "setLevel"
        command "setAmp"
        command "setGPM"
        command "setMoisture"
        command "setLastWater"
        command "generateEvent"
	}
    preferences {}    
}

def installed(){
	initialize()
}

//when device preferences are changed
def updated(){	
    initialize()
}

private initialize() {
	sendEvent(name: "valve", value: "closed")
    if (device.latestValue("valveDuration") == null) sendEvent(name: "valveDuration", value: 10)
}

def generateEvent(Map results) {
  log.debug results
  
  sendEvent(name: "${results.name}", value: "${results.value}", descriptionText: "${results.descriptionText}", isStateChange: true, displayed: "${results.displayed}")
  if (results.name == "switch") setLastWater(results)
  
  return null
}

def setValveDuration(duration) {
	if (DEBUG) log.debug "Valve Duration set to: ${duration}"

	sendEvent(name: "valveDuration", value: duration, displayed: false)
}

void open(){
	def runtime = device.latestValue('level')
	log.debug runtime
	parent.zoneOnOff(device.deviceNetworkId, 1, runtime)
}

void close(){
    parent.zoneOnOff(device.deviceNetworkId, 0, 0)
}

//settings from cloud
def childSettings(zone_num, Map results){
	log.debug "Spruce Zone ${zone_num} settings ${results}"    
}

//------not used?-----------

//set minutes
def setValveDuration(duration) {
	if (DEBUG) log.debug "Valve Duration set to: ${duration}"

	sendEvent(name: "valveDuration", value: duration, displayed: false)
}
