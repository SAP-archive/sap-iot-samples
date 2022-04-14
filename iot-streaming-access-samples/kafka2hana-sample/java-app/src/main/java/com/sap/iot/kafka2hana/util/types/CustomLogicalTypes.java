package com.sap.iot.kafka2hana.util.types;

import org.apache.avro.LogicalType;

import java.io.Serializable;

/**
 * define all custom logic types
 */
public class CustomLogicalTypes implements Serializable {

    private static final long serialVersionUID = -3329228597881851503L;

    public static CustomString stringConversionWithAnnotation(final int maxLen, final boolean hasAnnotation) {

        return new CustomString(maxLen, hasAnnotation);
    }

    public static CustomString stringConversionWithOutAnnotation(final int maxLen) {

        return new CustomString(maxLen);
    }

    public static TimestampSeconds timeSecsConversion() {

        return new TimestampSeconds();
    }

    public static TimestampMillis timeMillisConversion() {

        return new TimestampMillis();
    }

    public static CustomDecimal decimalConversion(final int scale, final int precision) {

        return new CustomDecimal(scale, precision);
    }

    public static CustomByteArray byteArrayConversion(final int maxLen) {

        return new CustomByteArray(maxLen);
    }

    public static CustomLargeString largeStringConversion(final int maxLen) {

        return new CustomLargeString(maxLen);
    }

    public static CustomJson jsonConversion(final int maxLen) {

        return new CustomJson(maxLen);
    }

    public static CustomUUID uuidConversion(final int fixLen) {

        return new CustomUUID(fixLen);
    }

    public static CustomTimestamp timestampConversion() {

        return new CustomTimestamp();
    }

    public static CustomGeoJSON geoJSONConversion() {

        return new CustomGeoJSON();
    }

    /**
     * Class for Custom Logical Type "nString"
     */
    public static class CustomString extends LogicalType {

        private static final String NSTRING = "nString";
        private final int maxLength;
        private final boolean hasAnnotation;

        private CustomString(final int maxLength) {

            super(NSTRING);
            this.maxLength = maxLength;
            this.hasAnnotation = false;
        }

        private CustomString(final int maxLength, final boolean hasAnnotation) {

            super(NSTRING);
            this.maxLength = maxLength;
            this.hasAnnotation = hasAnnotation;
        }

    }

    /**
     * Class for Custom Logical Type "nJson"
     */
    public static class CustomJson extends LogicalType {

        private static final String NJSON = "nJson";
        private final int maxLength;

        private CustomJson(final int maxLength) {

            super(NJSON);
            this.maxLength = maxLength;
        }

    }

    /**
     * class for Custom Logical Type "timestamp-seconds"
     */
    public static class TimestampSeconds extends LogicalType {

        private static final String TIMESTAMP_SECONDS = "timestamp-seconds";

        private TimestampSeconds() {

            super(TIMESTAMP_SECONDS);

        }

    }

    /**
     * class for Custom Logical Type "timestamp-millis"
     */
    public static class TimestampMillis extends LogicalType {

        private static final String TIMESTAMP_MILLIS = "timestamp-millis";

        private TimestampMillis() {

            super(TIMESTAMP_MILLIS);

        }

    }

    /**
     * class for Custom Logical Type "nUUID"
     */
    public static class CustomUUID extends LogicalType {

        private static final String NUUID = "nUUID";
        private final int fixedLength;

        private CustomUUID(final int fixedLength) {

            super(NUUID);
            this.fixedLength = fixedLength;
        }

    }

    /**
     * class for Custom Logical Type "nDecimal"
     */
    public static class CustomDecimal extends LogicalType {

        private static final String NDECIMAL = "nDecimal";
        private final int scale;
        private final int precision;

        private CustomDecimal(final int scale, final int precision) {

            super(NDECIMAL);
            this.scale = scale;
            this.precision = precision;
        }

    }

    /**
     * class for Custom Logical Type "nLargeString"
     */
    public static class CustomLargeString extends LogicalType {

        private static final String NLARGESTRING = "nLargeString";
        private final int maxLength;

        private CustomLargeString(final int maxLength) {

            super(NLARGESTRING);
            this.maxLength = maxLength;
        }

    }

    /**
     * class for Custom Logical Type "nByte"
     */
    public static class CustomByteArray extends LogicalType {

        private static final String NBYTE = "nByte";
        private final int maxLength;

        private CustomByteArray(final int maxLength) {

            super(NBYTE);
            this.maxLength = maxLength;
        }

    }

    /**
     * Class for Custom Logical Type "nTimestamp"
     */
    public static class CustomTimestamp extends LogicalType {

        private static final String NTIMESTAMP = "nTimestamp";

        private CustomTimestamp() {

            super(NTIMESTAMP);
        }

    }

    /**
     * Class for Custom Logical Type "nGeoJson"
     */

    public static class CustomGeoJSON extends LogicalType {

        private static final String N_GEO_JSON = "nGeoJSON";

        private CustomGeoJSON() {

            super(N_GEO_JSON);

        }

    }

}