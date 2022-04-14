package com.sap.iot.streamaccess.util.types;

import org.apache.avro.LogicalTypes;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;

import java.io.Serializable;


public class RegisterService implements Serializable {

    private static final long serialVersionUID = 8518738387000986526L;
    private static final String MAX_LENGTH = "max-length";
    private static final GenericData genericData = new GenericData();
    private static final String FIX_LENGTH = "fix-length";
    private static final String SCALE = "scale";
    private static final String PRECISION = "precision";

    /**
     * * @return Generic Data object which can be used to write Avro Messages.
     */
    public static GenericData initializeCustomTypes() {

        LogicalTypes.register(
                "nString", schema -> CustomLogicalTypes.stringConversionWithAnnotation(
                        Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString()), Boolean.getBoolean(
                                schema.getObjectProp("has-annotation").toString())));

        LogicalTypes.register(
                "nString", schema -> CustomLogicalTypes.stringConversionWithOutAnnotation(
                        Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString())));

        LogicalTypes.register(
                "nJson", schema -> CustomLogicalTypes.jsonConversion(
                        Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString())));

        LogicalTypes.register(
                "nByte", schema -> CustomLogicalTypes.byteArrayConversion(
                        Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString())));

        LogicalTypes.register("timestamp-seconds", schema -> CustomLogicalTypes.timeSecsConversion());

        LogicalTypes.register("timestamp-millis", schema -> CustomLogicalTypes.timeMillisConversion());

        LogicalTypes.register(
                "nLargeString", schema -> CustomLogicalTypes.largeStringConversion(
                        Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString())));

        LogicalTypes.register(
                "nUUID", schema -> CustomLogicalTypes.uuidConversion(
                        Integer.parseInt(schema.getObjectProp(FIX_LENGTH).toString())));

        LogicalTypes.register(
                "nDecimal", schema -> CustomLogicalTypes.decimalConversion(
                        Integer.parseInt(schema.getObjectProp(SCALE).toString()), Integer.parseInt(
                                schema.getObjectProp(PRECISION).toString())));

        LogicalTypes.register("nTimestamp", schema -> CustomLogicalTypes.timestampConversion());

        LogicalTypes.register("nGeoJSON", schema -> CustomLogicalTypes.geoJSONConversion());

        genericData.addLogicalTypeConversion(new CustomConversion.TimeSecondsConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.TimeMillisConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.UUIDConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.StringConversion());
        genericData.addLogicalTypeConversion(new TimeConversions.DateConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.ByteArrayConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.DecimalConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.JsonConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.LargeStringConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.LongTimeStampConversion());
        genericData.addLogicalTypeConversion(new CustomConversion.GeoJsonConversion());

        return genericData;
    }
}