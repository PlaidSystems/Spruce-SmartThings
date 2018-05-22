# Spruce-SmartThings
*Connect Spruce GEN2 Wifi controller to SmartThings(ST)*

Integrate Spruce GEN2 Controller and GEN1 or GEN2 Sensors with SmartThings. Control zones and manual schedules, pause or stop watering, recieve notifications, recieve sensor readings, use SmartThings automations or IFTTT.

**Requires**
  - Spruce GEN2 Wifi Controller
  - Samsung SmartThings Hub
  
**Installation**
  - Smartapp- Spruce Connect
  - Device Handlers- Spruce wifi master, Spruce wifi schedule, Spruce wifi zone
  
**Features**
  - Spruce Connect uses OAUTH2 to connect SmartThings to Spruce Cloud.
  - Master controller device with status, run all zones, pause and manual schedule controls
  - Child Zone devices with individual zone control and monitoring
  - Each zone and each manual schedule provided as "devices" that can be used as "switches" within standard ST automations and SmartApps
  - "Contacts" can be selected to pause/resume schedules
  - Pause device can be enabled and used in automations or SmartApps to pause/resume schedules
  - Schedule, zone and valve error notifications available
  - Valve health and flow (when used with flow meter)
  - Spruce sensors paired with ST can use the Spruce Connect to report values to the Spruce Cloud.
  
**Install Overview**

*It is recommended to setup and name all zones before connecting Spruce and SmartThings*
  1. Log into SmartThings IDE https://graph.api.smartthings.com/ with your SmartThings Username and Password
  2. Create a new SmartApp using the [*Spruce Connect*](https://github.com/PlaidSystems/Spruce-SmartThings/blob/master/smartapps/plaidsystems/spruce-connect.src/spruce-connect.groovy) code
      - Add a new SmartApp from Code
      - Copy and Paste the code
      - Save
      - In App Settings, **Enable OAUTH**
      - Save and Publish
  3. Create 3 new device handlers using the code from:
      - [*Spruce wifi master*](https://github.com/PlaidSystems/Spruce-SmartThings/blob/master/devicetypes/plaidsystems/spruce-wifi-master.src/spruce-wifi-master.groovy)
      - [*Spruce wifi schedule*](https://github.com/PlaidSystems/Spruce-SmartThings/blob/master/devicetypes/plaidsystems/spruce-wifi-schedule.src/spruce-wifi-schedule.groovy)
      - [*Spruce wifi zone*](https://github.com/PlaidSystems/Spruce-SmartThings/blob/master/devicetypes/plaidsystems/spruce-wifi-zone.src/spruce-wifi-zone.groovy)
      - Add new Device Handlers from code for each
      - Copy and Paste the code
      - Save and Publish each
      
**Setting up**

  1. Close and Restart the SmartThings app to reload the available SmartApps 
  2. In the SmartThings app under *Marketplace->SmartApps->My Apps* select **Spruce Connect**
  3. User your Spruce account credentials to login and link SmartThings and Spruce
  4. Select 1 Spruce Controller to link, go to *next*
  5. The SmartApp will list the zones that will be provided to SmartThings at the top of the page
  6. Select any **Contact** sensors you would like to use, these will **always** pause and resume the schedule
  7. Select notifications that you would like to recieve through SmartThings, these settings do not effect Spruce app notification settings
  8. Enable the **Spruce Pause Control** if you would like this control to show up as an available option for automations and SmartApps.  It can be used to pause or resume schedules.
  9. Select any Spruce moisture sensors that are conected to SmartThings so that they will be available in the Spruce App
  10. **Save!** your done, your Spruce devices will be under "Things" and any Sensors will start reporting to the Spruce Cloud at the next reporting interval.
  
  **TIPS**
  - Make sure zones are setup and named correctly in the Spruce App before setting up the SmartThings integration. Simple but descriptive names are best and will help keep everything organized.  *This also helps with voice commands*
  - Organize Master and Zone devices into a single "Room"
  - When switching on a zone manually, the time can be set in the lower righ corner of the tile
  - The *Start all Zones* button will run each zone for the time set in the lower right corner of the main tile
  - Schedules can be paused with the **Contact Button** to the left of the *Start all Zones* button
  - It is recommended to setup and name all zones before connecting Spruce and SmartThings, otherwise Automations or SmartApps utilizing the previous names will error.  When zones are re-named in the Spruce app, the device must be re-created for the name to refresh, open Spruce Connect and save again.  This will re-populate the zones and manual schedules within the SmartThings app.  The devices *must* be removed from other Automations and SmartApps before doing this.
  - If no zone names are changed, then opening and saving new settings in the Spruce Connect will **not** re-populate the zones so Automations and other SmartApps will not be effected.
