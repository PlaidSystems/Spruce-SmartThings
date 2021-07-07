/**
 *  Spruce Sensor -updated with SLP3 model number 3/2019
 *
 *  Copyright 2014 Plaid Systems
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
 
 -------6/2021 Updates--------
 - Update for 2021 SmartThings App
 - Add Signal Strength
 
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

//dth version
def getVERSION() {"v1.0 6-2021"}
def getDEBUG() {true}
def getHC_INTERVAL_MINS() {60}

metadata {
	definition (name: "Spruce Sensor Test V3", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity",
    	mcdSync: true, vid: "02dc7702-2bbd-3dc0-bfc5-fb168cbca308") {
		
		capability "Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Battery"        
        capability "Signal Strength"
        capability "Health Check"
        capability "Configuration"
		capability "Refresh"
        
        attribute "reportingInterval", "NUMBER"
        
        //new release
		fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-01", zigbeeNodeType: "SLEEPY_END_DEVICE", deviceJoinName: "Spruce Irrigation Sensor"
        fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-SLP1", zigbeeNodeType: "SLEEPY_END_DEVICE", deviceJoinName: "Spruce Irrigation Sensor"
        fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-SLP3", zigbeeNodeType: "SLEEPY_END_DEVICE", deviceJoinName: "Spruce Irrigation Sensor"
	}

	preferences {
		input description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph", title: ""
		input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		
        input description: "Gen 1 & 2 Sensors only: Measurement Interval 1-120 minutes (default: 10 minutes)", displayDuringSetup: false, type: "paragraph", element: "paragraph", title: ""
        input "interval", "number", title: "Measurement Interval", description: "Set how often you would like to check soil moisture in minutes", range: "1..120", defaultValue: 10, displayDuringSetup: false
		
        input title: "Version", description: VERSION, displayDuringSetup: true, type: "paragraph", element: "paragraph"        
    }
	
}

// Parse incoming device messages to generate events
def parse(description) {
	if (DEBUG) log.debug "Parse description $description config: ${device.latestValue("configuration")} interval: $interval"    
    
    getSignalStrength() 
    
    def map = zigbee.parseDescriptionAsMap(description)
	
    if (map.raw) {
    	log.debug "map raw ${map}"
        map = parseCatchAllMessage(description)
    }
    else if (description?.startsWith("read attr -")) {
    	log.debug "read attr - ${description}"
		map = parseReportAttributeMessage(description)
	}   
    else if (isSupportedDescription(description)) {
    	log.debug "supported description: $description"
        map = parseCustomMessage(description)
    }    
    else if (description?.startsWith("catchall:")) {
		log.debug "catchall ${description}"
        map = parseCatchAllMessage(description)
	}
	
    log.debug "map: $map"
    def result = map ? createEvent(map) : null
 	
    //check in configuration change    
    if (isIntervalChange()) result = ping()    
 	
    log.debug "result: $result"
    return result    
}

private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def linkText = getLinkText(device)	
    def map = zigbee.parseDescriptionAsMap(description)
    
    log.debug "${map.command} ${map.clusterId}"
    
    //check humidity configuration is complete
    if (map.command == "07" && map.clusterId == "0405"){    	
        def configInterval = 10
        if (interval != null) configInterval = interval        
        sendEvent(name: "reportingInterval", value: configInterval, descriptionText: "Configuration Successful")        
        if (DEBUG) log.debug "config complete"
    }
    else if (map.clusterId == 0x0405) {
        return getHumidityResult(map.value);
    }
    else if (map.clusterId == 0x0402) {
        return getTemperatureResult(map.value);
    }
    else if (map.clusterId == 0x0001) {
        return getBatteryResult(map.value);
    }
        /*
    else if (descMap.command == 0x0001){
        //zigbee.convertHexToInt  
    	def hexString = "${hex(descMap.data[5])}" + "${hex(descMap.data[4])}"
    	def intString = Integer.parseInt(hexString, 16)    
    	log.debug "command: ${descMap.command} clusterid: ${descMap.clusterId} ${descMap.value} ${hexString} ${intString}"

    
    	if (descMap.clusterId == 0x0402){    	
            def value = getTemperature(hexString)
            resultMap = getTemperatureResult(value)    
        }
        else if (descMap.clusterId == 0x0405){
            def value = Math.round(new BigDecimal(intString / 100)).toString()
            resultMap = getHumidityResult(value)

        }
        else return null
    }*/
    return null

}    
    
private Map parseReportAttributeMessage(String description) {	
    def descMap = parseDescriptionAsMap(description)
	//log.debug "parseReportAttributeMessage: $descMap"
    log.debug "Report Attributes"
 
	Map resultMap = [:]
	if (descMap.cluster == "0001" && descMap.attrId == "0000") {		
        resultMap = getBatteryResult(descMap.value)
	}    
    return resultMap
}

private Map parseCustomMessage(String description) {
	Map resultMap = [:]  
        
	log.debug "parseCustomMessage"
	if (description?.startsWith("temperature: ")) {
		def value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
		resultMap = getTemperatureResult(value)
	}
	else if (description?.startsWith("humidity: ")) {
		def pct = (description - "humidity: " - "%").trim()
		if (pct.isNumber()) {        	
            def value = Math.round(new BigDecimal(pct)).toString()
			resultMap = getHumidityResult(value)
		} else {
			log.error "invalid humidity: ${pct}"
		}    
	}
    
	return resultMap
}


//----------------------event values-------------------------------//

private Map getHumidityResult(value) {
    if (DEBUG) log.debug "Humidity: $value"
    def linkText = getLinkText(device)
    
    return [
    	name: "humidity",
    	value: value,
    	unit: "%",
        descriptionText: "${linkText} soil moisture is ${value}%"
    ]
}

private Map getTemperatureResult(value) {
	if (DEBUG) log.debug "Temperature: $value"
	def linkText = getLinkText(device)
        
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset        
	}
	def descriptionText = "${linkText} is ${value}°${temperatureScale}"
	
    return [
		name: "temperature",
		value: value,
		descriptionText: descriptionText,
		unit: temperatureScale
	]
}

private Map getBatteryResult(value) {
	if (DEBUG) log.debug "Battery: $value"
	def linkText = getLinkText(device)    
    	
	def min = 2500   
	def percent = ((Integer.parseInt(value, 16) - min) / 5)
	percent = Math.max(0, Math.min(percent, 100.0))
    value = Math.round(percent)
    
    def descriptionText = "${linkText} battery is ${result.value}%"
    if (percent < 10) descriptionText = "${linkText} battery is getting low $percent %."
	
	return [
		name: "battery",
		value: value,
		descriptionText: descriptionText
	]
}

def getSignalStrength() {
	def meshInfo = device.getDataValue("meshInfo") 
    def results = new groovy.json.JsonSlurper().parseText(meshInfo)
    //if (DEBUG) log.debug "RSSI: ${results.metrics.lastRSSI} LQI: ${results.metrics.lastLQI}"
    
    sendEvent(name: "rssi", value: results.metrics.lastRSSI)
	sendEvent(name: "lqi", value: results.metrics.lastLQI)    
}

//----------------------configuration-------------------------------//

def installed() {
	//check every 62 minutes
    sendEvent(name: "checkInterval", value: 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

//when device preferences are changed
def updated() {	
    if (DEBUG) log.debug "device updated"
    
    //if (!device.latestValue("configuration")) configure()
    //else 
    if (isIntervalChange()) {    	
        sendEvent(name: "reportingInterval", value: 0, descriptionText: "Settings changed and will update at next report. Measure interval set to ${interval} mins")
    }

    // Device-Watch every 62mins or interval + 120s
    def reportingIntervalSeconds  = interval * 60 + 2 * 60
    if (reportingIntervalSeconds < 3720) reportingIntervalSeconds = 3720
    sendEvent(name: "checkInterval", value: reportingIntervalSeconds, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

//has interval been updated
def isIntervalChange() {
    //log.debug "isIntervalChange ${interval} ${device.latestValue("reportingInterval")}"
    return (interval != device.latestValue("reportingInterval"))    
}

//ping
def ping() {
	if (DEBUG) log.debug "device health ping"
	
    /*
    List cmds = []
    if (!device.latestValue("configuration")) cmds += configure()
    else if (device.latestValue("configuration").toInteger() != interval && interval != null) { 
    	cmds += intervalUpdate()
    }

    return cmds?.collect { new physicalgraph.device.HubAction(it) }    
    */

    //if (device.latestValue("configuration").toInteger() != interval && interval != null) return reporting()
    
    List cmds = []
    
    if (isIntervalChange()) cmds = reporting()
    else cmds = refresh()

    return cmds?.collect { new physicalgraph.device.HubAction(it) } 
}

/*
//update intervals
def intervalUpdate() {
    //log.debug device.getDataValue("model")
	return reporting()
}
*/

//configure
def configure() {
    return reporting() + refresh()    
}

//set reporting
def reporting() {
    //set minReport = measurement in minutes
    def minReport = 10
    def maxReport = 610
    if (interval != null) {
    	minReport = interval
        maxReport = interval * 61
    }

    def reportingCmds = []
	reportingCmds += zigbee.configureReporting(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0, DataType.INT16, 1, 0, 0x01, [destEndpoint: 1])
	reportingCmds += zigbee.configureReporting(zigbee.RELATIVE_HUMIDITY_CLUSTER, 0, DataType.UINT16, minReport, maxReport, 0x6400, [destEndpoint: 1])
    reportingCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0, DataType.UINT16, 0x0C, 0, 0x0500, [destEndpoint: 1])
    
    return reportingCmds
}

def refresh() {
	log.debug "refresh"
    def refreshCmds = []
    refreshCmds += zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0, [destEndpoint: 1])
    refreshCmds += zigbee.readAttribute(zigbee.RELATIVE_HUMIDITY_CLUSTER, 0, [destEndpoint: 1])
    refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0, [destEndpoint: 1])

    return refreshCmds
}
