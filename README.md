# Spruce-SmartThings
*Connect Spruce GEN2 Wifi controller to SmartThings(ST)*

Integrate Spruce GEN2 Controller and GEN1, GEN2 or GEN3 Sensors with SmartThings 2021 app. Control zones, pause or stop watering, receive notifications, receive sensor readings, use SmartThings automations or IFTTT.

**Requires**
  - Spruce GEN2 Wifi Controller
  - Samsung SmartThings Hub
  
**Installation**
  - Smartapp- Spruce Connect
  - Device Handlers- Spruce Wifi Controller
  
**Features**
  - Spruce Connect uses OAUTH2 to connect SmartThings to Spruce Cloud
  - Spruce Controller device Controller State, status, time, rain sensor and each enabled zone
  - Full device capability now available for use in the SmartThings app Automation tool.  Including on, pause, resume, off, status, rain sensor, duration, each zone.
  - "Contacts" can be selected to pause/resume schedules
  - Pause device can be enabled and used in automations or SmartApps to pause/resume schedules
  - Schedule and zone notifications available
  - Spruce sensors paired with ST can use the Spruce Connect to report values to the Spruce Cloud
  
**Install Overview**

*It is recommended to setup and configure all zones before connecting Spruce and SmartThings*
  1. Log into SmartThings IDE https://graph.api.smartthings.com/ with your SmartThings Username and Password
  2. Create a new SmartApp using the [*Spruce Connect*](https://raw.githubusercontent.com/PlaidSystems/Spruce-SmartThings/master/smartapps/plaidsystems/spruce-connect.src/spruce-connect.groovy) code
      - Add a new SmartApp from Code
      - Copy and Paste the code
      - Save
      - In App Settings, **Enable OAUTH**
      - Save and Publish
  3. Create 3 new device handlers using the code from:
      - [*Spruce Wifi Controller*](https://raw.githubusercontent.com/PlaidSystems/Spruce-SmartThings/master/devicetypes/plaidsystems/spruce-wifi-controller.src/spruce-wifi-controller.groovy)
      - Add new Device Handler from code
      - Copy and Paste the code
      - Save and Publish each

Alternative setup for Github accounts:
  
  1. Add the GitHub Repository:
      - Owner: PlaidSystems
      - Name: Spruce-SmartThings
      - Branch: Master
      
  2. Update from Repo selecting the device handler and smartapp listed above
      
**Setting up**

  1. Close and Restart the SmartThings app to reload the available SmartApps 
  2. In the SmartThings app under *Marketplace->SmartApps->My Apps* select **Spruce Connect**
  3. User your Spruce account credentials to login and link SmartThings and Spruce
  4. Select 1 Spruce Controller to link, go to *next*
  5. The SmartApp will list the zones that will be provided to SmartThings at the top of the page
  6. Select any **Contact** sensors you would like to use, these will **always** pause and resume the schedule
  7. Select notifications that you would like to recieve through SmartThings, these settings do not effect Spruce app notification settings 
  8. Select any Spruce moisture sensors that are conected to SmartThings so that they will be available in the Spruce App
  9. **Save!** your done, your Spruce Controller device will be under "Devices" and any Sensors will start reporting to the Spruce Cloud at the next reporting interval.
  
  **TIPS**  
  - When switching on a zone manually, the watering time can be set with the slider, or tap the slider for +/- buttons
  - Changing the **Controller State** to **on** will run each enabled zone for the time set in the slider
  - Schedules can be paused or resumed with the **Controller State** by selecting **pause** or **resume**
  - If new **zones are enabled or disabled** in the Spruce app, the Spruce Controller device will be updated the next time the Spruce Connect smartapp is opened.  Automations or smartapps that utilize this device will need to be updated appropriately.
  
