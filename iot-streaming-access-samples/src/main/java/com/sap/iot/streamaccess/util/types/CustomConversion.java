package com.sap.iot.streamaccess.util.types;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.iot.streamaccess.exceptions.CustomAvroException;
import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Contains the conversion logic for Custom Logical Types created.
 */
public class CustomConversion implements Serializable {

    private static final long serialVersionUID = -3310980614407552917L;
    private static final String INVALID_TIMESTAMP_VALUE = "Invalid timestamp value.";
    private static final String MAX_LENGTH = "max-length";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.of("GMT"));

    /**
     * Conversion class for String data Type
     */
    public static class StringConversion extends Conversion<String> {

        private static final String NSTRING = "nString";
        private static final String MAX_LENGTH = "max-length";

        public Class<String> getConvertedType() {

            return String.class;
        }

        public String getLogicalTypeName() {

            return NSTRING;
        }

        @Override
        public String fromCharSequence(final CharSequence value, final Schema schema, final LogicalType type) {

            checkString(value.toString(), schema);
            return value.toString();
        }

        @Override
        public CharSequence toCharSequence(final String value, final Schema schema, final LogicalType type) {

            checkString(value, schema);
            return value;
        }

        private void checkString(final String value, final Schema schema) {

            if (value.length() > Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString())) {
                throw new IllegalArgumentException(
                        "Length of the String is greater than what is allowed in the schema");
            }
        }
    }

    /**
     * Conversion class for LargeString Data Type
     */
    public static class LargeStringConversion extends Conversion<String> {

        private static final String NLARGESTRING = "nLargeString";

        public Class<String> getConvertedType() {

            return String.class;
        }

        public String getLogicalTypeName() {

            return NLARGESTRING;
        }

        @Override
        public String fromCharSequence(final CharSequence value, final Schema schema, final LogicalType type) {

            checkLargeString(value.toString(), schema);
            return value.toString();
        }

        @Override
        public CharSequence toCharSequence(final String value, final Schema schema, final LogicalType type) {

            checkLargeString(value, schema);
            return value;
        }

        private void checkLargeString(final String value, final Schema schema) {

            if (value.length() > Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString())) {
                throw new IllegalArgumentException(
                        "Length of the LargeString is greater than what is allowed in the schema");
            }
        }

    }

    /**
     * Conversion class for ThingId and BPID data types
     */
    public static class UUIDConversion extends Conversion<String> {

        private static final String NUUID = "nUUID";
        private String regexUuid = "[0-9|A-F]{32}";

        public Class<String> getConvertedType() {

            return String.class;
        }

        public String getLogicalTypeName() {

            return NUUID;
        }

        @Override
        public String fromCharSequence(final CharSequence value, final Schema schema, final LogicalType type) {

            checkUUID(value.toString());
            return value.toString();

        }

        @Override
        public CharSequence toCharSequence(final String value, final Schema schema, final LogicalType type) {

            checkUUID(value);
            return value;
        }

        private void checkUUID(final String value) {

            if (!value.matches(regexUuid)) {
                throw new IllegalArgumentException("UUID does not match the given regrex");
            }
        }

    }

    /**
     * Conversion class for JSON data type
     */
    public static class JsonConversion extends Conversion<String> {

        private static final String NJSON = "nJson";
        private final ObjectMapper objectMapper = new ObjectMapper();

        public Class<String> getConvertedType() {

            return String.class;
        }

        public String getLogicalTypeName() {

            return NJSON;
        }

        @Override
        public String fromCharSequence(final CharSequence value, final Schema schema, final LogicalType type) {

            checkJson(value.toString(), schema);
            return value.toString();

        }

        @Override
        public CharSequence toCharSequence(final String value, final Schema schema, final LogicalType type) {

            checkJson(value, schema);
            return value;

        }

        private void checkJson(final String value, final Schema schema) {

            try {
                if (value.getBytes(StandardCharsets.UTF_16).length > Integer.parseInt(
                        schema.getObjectProp(MAX_LENGTH).toString())) {
                    throw new IllegalArgumentException(
                            "Length of the JSON is greater than what is allowed in the schema");
                } else {
                    objectMapper.readTree(value);
                }
            } catch (final IOException e) {
                throw new IllegalArgumentException("Not a Valid JSON", e);
            }
        }

    }

    /**
     * Conversion class for ByteArray Data Type
     */
    public static class ByteArrayConversion extends Conversion<String> {

        private static final String NBYTE = "nByte";

        public Class<String> getConvertedType() {

            return String.class;
        }

        public String getLogicalTypeName() {

            return NBYTE;
        }

        @Override
        public String fromBytes(final ByteBuffer value, final Schema schema, final LogicalType type) {

            final byte[] byteArray = value.array();
            checkByteArray(byteArray, schema);
            String s = new String(byteArray);
            return s.substring(s.indexOf('['));
        }

        private void checkByteArray(final byte[] byteArray, final Schema schema) {

            if (byteArray.length > Integer.parseInt(schema.getObjectProp(MAX_LENGTH).toString())) {
                throw new IllegalArgumentException(
                        "Length of the ByteArray is greater than what is allowed in the schema");
            }
        }

    }

    /**
     * Conversion class for DateTime data Type
     */
    public static class TimeSecondsConversion extends Conversion<String> {

        private static final String TIMESTAMP_SECONDS = "timestamp-seconds";

        public Class<String> getConvertedType() {

            return String.class;
        }

        public String getLogicalTypeName() {

            return TIMESTAMP_SECONDS;
        }

        @Override
        public String fromLong(final Long value, final Schema schema, final LogicalType type) {

            return Instant.ofEpochSecond(value).toString();
        }

    }

    /**
     * Conversion class for timestamp in millis
     */
    public static class TimeMillisConversion extends Conversion<String> {

        private static final String TIMESTAMP_MILLIS = "timestamp-millis";

        public Class<String> getConvertedType() {

            return String.class;
        }

        public String getLogicalTypeName() {

            return TIMESTAMP_MILLIS;
        }

        @Override
        public String adjustAndSetValue(String varName, String valParamName) {

            return varName + " = " + valParamName + ".truncatedTo(java.time.temporal.ChronoUnit.MILLIS);";
        }

        @Override
        public String fromLong(Long millisFromEpoch, Schema schema, LogicalType type) {

            return formatter.format(Instant.ofEpochMilli(millisFromEpoch));
        }

    }

    /**
     * Conversion class for Decimal Data type
     */
    public static class DecimalConversion extends Conversion<BigDecimal> {

        private static final String NDECIMAL = "nDecimal";

        public Class<BigDecimal> getConvertedType() {

            return BigDecimal.class;
        }

        public String getLogicalTypeName() {

            return NDECIMAL;
        }

        @Override
        public BigDecimal fromBytes(final ByteBuffer value, final Schema schema, final LogicalType type) {

            final byte[] bytes = value.get(new byte[value.remaining()]).array();
            final byte[] actualValue = Arrays.copyOfRange(bytes, 1, bytes.length);
            final BigDecimal bigDecimal = new BigDecimal(new BigInteger(actualValue), bytes[0]);
            checkDecimal(bigDecimal, schema);
            return bigDecimal;

        }

        @Override
        public ByteBuffer toBytes(final BigDecimal value, final Schema schema, final LogicalType type) {

            checkDecimal(value, schema);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write((byte) value.scale());

            try {
                out.write(value.unscaledValue().toByteArray());
            } catch (IOException e) {
                throw new CustomAvroException("Writing of value to the OutputStream Failed. ", e);
            }

            return ByteBuffer.wrap(out.toByteArray());
        }

        private void checkDecimal(final BigDecimal value, final Schema schema) {

            final int scale = Integer.parseInt(schema.getObjectProp("scale").toString());
            final int precision = Integer.parseInt(schema.getObjectProp("precision").toString());
            if (scale < value.scale()) {
                throw new IllegalArgumentException("Scale cant be greater than the specified scale in schema");
            }
            if (precision < value.precision()) {
                throw new IllegalArgumentException("Precision cant be greater than the specified precision in schema");
            }

        }

    }

    /**
     * Conversion class for Long Data type with is compatible with datetime
     */
    public static class LongTimeStampConversion extends Conversion<Long> {

        private static final String NTIMESTAMP = "nTimestamp";

        public Class<Long> getConvertedType() {

            return Long.class;
        }

        public String getLogicalTypeName() {

            return NTIMESTAMP;
        }

        @Override
        public Long fromLong(final Long value, final Schema schema, final LogicalType type) {

            if (value < 0) {
                throw new IllegalArgumentException(INVALID_TIMESTAMP_VALUE);
            }
            try {
                new DateTime(value, DateTimeZone.UTC);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(INVALID_TIMESTAMP_VALUE, e);
            }
            return value;
        }

        @Override
        public Long toLong(final Long value, final Schema schema, final LogicalType type) {

            return fromLong(value, schema, type);
        }

    }

    /**
     * Conversion class for GeoJSON data type
     */

    public static class GeoJsonConversion extends Conversion<String> {

        private static final String N_GEO_JSON = "nGeoJSON";

        @Override
        public Class<String> getConvertedType() {
            return String.class;
        }

        public String getLogicalTypeName() {
            return N_GEO_JSON;
        }

        @Override
        public String fromCharSequence(final CharSequence value, final Schema schema, final LogicalType type) {
            return value.toString();
        }

        @Override
        public CharSequence toCharSequence(final String value, final Schema schema, final LogicalType type) {
            return value;
        }

    }

}
