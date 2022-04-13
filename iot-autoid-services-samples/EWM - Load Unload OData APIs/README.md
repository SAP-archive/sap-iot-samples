# Load and Unload, SAP Internet of Things

## How to Load and Unload Process with SAP IoT System

* [General](#general)

* [Prerequisites](#prerequisites)

* [Create ODATA service](#create-odata-service)

  + [Create Entity Types](#create-entity-types)

  + [Create Function Imports](#create-function-imports)

  + [Generate ODATA Services Class](#generate-odata-services-class)

* [Register services](#register-services)

* [Development](#development)

* [Test](#test)


# General

The Purpose of this sample is to set up an ODATA service for the SAP Internet of Things to Load and Unload Handling Units.

# Prerequisites

-   The naming are case sensitive

# Create ODATA service

A new project needs to be created in the ODATA services by transaction
(TC) SEGW.

<img src="./media/image3.png"
style="width:5.52014in;height:2.46844in" />

Add a project name and description

<img src="./media/image4.png" style="width:6.26806in;height:4.1875in" />

## Create Entity Types

After the project has been created, the next step is to create a entity
type. It is a advice to use already existing structures or create an own
structure and import it into the entity type.

<img src="./media/image5.png"
style="width:5.41058in;height:4.32798in" />

For the Load/Unload Process import the structure /SCWM/S_HUIDENT:

<img src="./media/image6.png" style="width:6.26806in;height:3.7875in" />

Choose the fields LGNUM and HUIDENT to be imported into the entity type.

<img src="./media/image7.png" style="width:6.26806in;height:3.7875in" />

Add the Key flag for both entries and finish the creation.

<img src="./media/image8.png" style="width:6.26806in;height:3.7875in" />

Adding another structure for the messaging.

<img src="./media/image5.png"
style="width:5.41058in;height:4.32798in" />

Select the standard structure /SCWM/S_ODATA_MESSAGE

<img src="./media/image9.png" style="width:6.26806in;height:3.7875in" />

Choose the fields MSG_SUCCESS, MSGID, MSGNO, MESSAGE, HUIDENT and LGNUM
to be imported into the entity type.

<img src="./media/image10.png"
style="width:6.26806in;height:3.7875in" />

<img src="./media/image11.png"
style="width:6.26806in;height:3.7875in" />

Add the Key flag for the fields MSGID, MSGTY, HUIDENT and LGNUM and
finish the creation.

<img src="./media/image12.png"
style="width:6.26806in;height:3.7875in" />

## Create Function Imports

### LOAD

The next step is to create function imports for loading and unloading.
Select the data model and create via wizard:

<img src="./media/image13.png"
style="width:5.83852in;height:4.14764in" />

Add a function import name *load*

<img src="./media/image14.png"
style="width:6.26806in;height:1.65139in" />

The settings for the function import have to be chosen. The return type
kind has to be selected “Entity Type” and the return type has to be
message. The Return Cardinality must be 1. The HTTP Method type must be
POST.

<img src="./media/image15.png"
style="width:6.26806in;height:1.21944in" />

For the function import, the parameters must be entered with huident and
lgnum. Both fields need to be mapped to the correct ABAP field name.

<img src="./media/image16.png"
style="width:6.26806in;height:1.05417in" />

###  UNLOAD

Creation of next function import unload:

<img src="./media/image13.png"
style="width:5.83852in;height:4.14764in" />

Add a function import name *unload*

<img src="./media/image17.png"
style="width:6.26806in;height:1.65139in" />

The settings for the function import have to be chosen. The return type
kind has to be selected “Entity Type” and the return type has to be
message. The Return Cardinality must be 1. The HTTP Method type must be
POST.

<img src="./media/image15.png"
style="width:6.26806in;height:1.21944in" />

For the function import, the parameters must be entered with huident and
lgnum. Both fields need to be mapped to the correct ABAP field name.

<img src="./media/image16.png"
style="width:6.26806in;height:1.05417in" />

## Generate ODATA Services Class

<img src="./media/image18.png"
style="width:6.26806in;height:3.62569in" />

<img src="./media/image19.png"
style="width:6.26806in;height:4.59861in" />

Generated classes are included:

<img src="./media/image20.png"
style="width:5.32225in;height:4.15573in" />

# Register services

Open TC /IWFND/MAINT_SERVICE to register the new service. Add a new
Service:

<img src="./media/image21.png"
style="width:6.26806in;height:1.78611in" />

Search with LOCAL the created ODATA service:

<img src="./media/image22.png"
style="width:6.26806in;height:1.44653in" />

Select the technical service and ad service:

<img src="./media/image23.png"
style="width:6.26806in;height:2.17292in" />

Add Name and package. Create the new service

<img src="./media/image24.png"
style="width:6.26806in;height:4.67917in" />

# Development

Start the workbench with TC SE80 and select the package, which the ODATA
service classes are generated. Always redefine methods in class with
\*DPC_EXT. Redefine the method
/IWBEP/IF_MGW_APPL_SRV_RUNTIME\~EXECUTE_ACTION. This method will be
called when a function import is executed.

<img src="./media/image25.png"
style="width:6.26806in;height:3.41181in" />

Add following coding:

<img src="./media/image26.png"
style="width:2.23133in;height:3.73193in" />

<img src="./media/image27.png"
style="width:3.64203in;height:2.82627in" />

Creating Exception class:

<img src="./media/image28.png"
style="width:6.69375in;height:2.23542in" />

![](./media/image29.emf)![](./media/image30.emf)![](./media/image31.emf)![](./media/image32.emf)![](./media/image33.emf)![](./media/image34.emf)![](./media/image35.emf)

# Test

<img src="./media/image36.png"
style="width:6.69375in;height:3.60833in" />

<img src="./media/image37.png"
style="width:6.69375in;height:3.48889in" />

<img src="./media/image38.png"
style="width:6.69375in;height:3.54028in" />
