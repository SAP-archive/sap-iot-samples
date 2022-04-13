# Push HUs, SAP Internet of Things

## Announce Handling Units to SAP IoT during Inbound Process

* [General](#general)

* [Prerequisites](#prerequisites)

* [Settings](#settings)

    + [Creation of a Profile for the IoT Connection](#creation-of-a-profile-for-the-iot-connection)

    + [Configuration of the OAuth Client](#configuration-of-the-oauth-client)

    + [PPF Action](#ppf-action)

    + [Settings for the Service](#settings-for-the-service)

* [Creation of Objects](#creation-of-objects)

    + [Dictionary Objects](#dictionary-objects)

    + [Class Libraries](#class-libraries)

* [Test](#test)

# General

Sample to announce the creation of new EWM Inbound Process Handling Units SAP Internet of Things. The sample, after the creation of new Handling Units in EWM, is invoking the SAP IoT Custom Master Data APIs to create new Object Instances representing the HUs.

# Prerequisites

-   The naming are case sensitive

# Settings

## Creation of a Profile for the IoT Connection

Create an OAuth Profile for the Token generation. A scope is needed if a
special handling of the service in the IoT system is needed

<img src="./media/image3.png"
style="width:6.69375in;height:4.6930555555555555in" />

## Configuration of the OAuth Client

To connect to a IoT system, a client needs to be created. Call
transaction OA2C_CONFIG

<img src="./media/image4.png"
style="width:6.69375in;height:0.6145833333333334in" />

Create a client with the created profile. Add the client ID into the
client:

<img src="./media/image5.png"
style="width:4.558898731408574in;height:2.00867782152231in" />

After the creation, the attribute of the service needs to be inserted.
Insert the Client Secret, the authorization points and the toke point.
The access settings needs to be Basic, Header field and Client
Credentials

<img src="./media/image6.png"
style="width:3.9515234033245843in;height:4.412717629046369in" />

## PPF Action

The correct Actions profile needs to be selected for the PPF. For our
purpose we used the standard /SCWM/PDI_01:

<img src="./media/image7.png"
style="width:6.69375in;height:3.066666666666667in" />

Based on the requirements, the settings can be variable. We would
recommend to not start the PPF action in dialogue manually:

<img src="./media/image8.png"
style="width:6.69375in;height:6.458333333333333in" />

Subsequently, the processing method needs to be selected. Create a
method call and add a new custom entry:

<img src="./media/image9.png"
style="width:6.69375in;height:6.158333333333333in" />

To distinguish between actions, an own method should be selected for the
announcement. If a deletion is needed, that should be done in a
different method:

<img src="./media/image10.png"
style="width:6.69375in;height:4.704166666666667in" />

A custom class needs to be created at that point. An example coding is
included in this document:

<img src="./media/image11.png"
style="width:6.69375in;height:2.8979166666666667in" />

It is also recommended to implement a scheduler:

<img src="./media/image12.png"
style="width:6.69375in;height:5.33125in" />

## Settings for the Service

The PPF will be executed only, if the settings are done for the action.
Therefore, a custom view needs to be implement:

<img src="./media/image13.png"
style="width:6.69375in;height:2.15625in" />

With theses settings, the creation of the HUs in the IoT system will be
triggered by the PPF.

# Creation of Objects

## Dictionary Objects

### Database Table

<img src="./media/image14.png"
style="width:6.69375in;height:3.9520833333333334in" />

### View

<img src="./media/image15.png"
style="width:6.69375in;height:2.66875in" />

### Table Type

<img src="./media/image16.png"
style="width:6.69375in;height:4.014583333333333in" />

### Structure

<img src="./media/image17.png"
style="width:6.69375in;height:1.8965277777777778in" />

### Data Elements

<img src="./media/image18.png"
style="width:4.795596019247594in;height:3.15576990376203in" />

<img src="./media/image19.png"
style="width:4.860673665791776in;height:3.6146194225721784in" />

<img src="./media/image20.png"
style="width:4.749761592300962in;height:3.25668416447944in" />

### Domain

<img src="./media/image21.png"
style="width:6.69375in;height:4.0055555555555555in" />

## Class Libraries

### Classes of PPF Action

#### Class

[ZCL_IM_EWM_IOT_SERVICE_class.abap](./ZCL_IM_EWM_IOT_SERVICE_class.abap)

#### Methods

[ANNOUNCE_HU_TO_IOT.abap](./ANNOUNCE_HU_TO_IOT.abap)

[ZCL_IM_EWM_IOT_SERVICE.abap](./ZCL_IM_EWM_IOT_SERVICE.abap)

### Classes for Service

#### Class

[ZEWM_CL_IOT_ANNOUNCE_SRVC.abap](./ZEWM_CL_IOT_ANNOUNCE_SRVC.abap)

#### Methods

[ANNOUNCE_PDI_HU.abap](./ANNOUNCE_PDI_HU.abap)

[GET_NAME_MAPPING_ANNOUNCE.abap](./GET_NAME_MAPPING_ANNOUNCE.abap)

[SENDING_IOT_SERVICE.abap](./SENDING_IOT_SERVICE.abap)

# Test

Packing 2 HUs of the inbound delivery 410000013800:

<img src="./media/image29.png"
style="width:6.69375in;height:2.2152777777777777in" />

<img src="./media/image30.png"
style="width:6.69375in;height:3.660416666666667in" />

After Save, the PPF will run and create HUs in SAP IoT

<img src="./media/image31.png"
style="width:6.69375in;height:2.28125in" />

PPF was successful:

<img src="./media/image32.png"
style="width:6.69375in;height:4.25in" />

New entries in SAP IoT:

<img src="./media/image33.png"
style="width:6.69375in;height:3.752083333333333in" />

<img src="./media/image34.png"
style="width:4.432902449693788in;height:2.6972681539807524in" />

<img src="./media/image35.png"
style="width:3.7548359580052493in;height:3.4291765091863518in" />
