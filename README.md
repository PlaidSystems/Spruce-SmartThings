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
  1 - Create a new SmartApp using the *Spruce Connect* code
    - **Enable OAUTH** in the app settings
  2 - Create 3 new device handlers using the code from:
    - Spruce wifi master
    - Spruce wifi schedule
    - Spruce wifi zone
  3 - In the SmartThings app under *My Apps* select **Spruce Connect**
  4 - User your Spruce account credentials to login and link SmartThings and Spruce
  5 - Select 1 Spruce Controller to link, go to *next*
  6 - The SmartApp will list the zones that will be provided to SmartThings at the top of the page
  7 - Select and *Contact* sensors you would like to use, these will *always* pause and resume the schedule
  8 - Select notifications that you would like to recieve through SmartThings, these settings do not effect Spruce app notification settings
  9 - Enable the *Spruce Pause Control* if you would like this control to show up as an available option for automations and SmartApps.  It can be used to pause or resume schedules.
  10 - Select any Spruce moisture sensors that are conected to SmartThings so that they will be available in the Spruce App
  
  **Save!**
