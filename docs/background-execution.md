# Background Execution

## Android
Use Activity Recognition transitions, geofencing, WorkManager, boot receivers, and foreground services only for active detection windows requiring reliability. Stop continuous updates after `PARKED`.

## iOS
Use significant-location-change monitoring, Core Motion activity updates, region monitoring, restoration after relaunch, and Background Tasks for deferred work. Do not attempt unrestricted continuous background execution.
