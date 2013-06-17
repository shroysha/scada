Washington County SCADA Server Information README

SiteConfigs.dat file format:

# at start of line signifies a comment

The site is setup in this manner:

Site Name=NAME_OF_SITE
Lat=Y_POSITION_ON_MAP
Long=X_POSITION_ON_MAP

Device Name=NAME_OF_DEVICE_AT_SITE
TCPIP=_TCP_IP_ADDRESS_OF_DEVICE
Modbus= 0 means not Modbus, 1 means it is a Modbus component
Discretes=Name:10001w, etc.  NAME_OF_DISCRETE:PORT(n,w,c)  (Normal, Warning Component, Critical Component)
Registers=Name:30001w, etc.  NAME_OF_REGISTER:PORT(n,w,c)  (Normal, Warning Component, Critical Component)

#Continue adding devices in this manner.  Leave a space in between each device.
Device Name=Touch Screen
TCPIP=192.168.10.10
Modbus=1
Discretes=Pump:10001w,Relay:10002n,Power:10003c
Registers=Level:30001c
end
#end MUST BE AFTER THE LAST LINE OF THE SITE

