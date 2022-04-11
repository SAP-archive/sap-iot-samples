package com.sap.iot.streamaccess.util;

import com.sap.iot.streamaccess.util.types.RegisterService;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * utility class to deserialize avro message
 */
public class AvroUtils {

    private static final Logger logger = LoggerFactory.getLogger(AvroUtils.class);

    @SuppressWarnings("unchecked")
    public static GenericRecord deserialize(byte[] data) {

        GenericData genericData = RegisterService.initializeCustomTypes();

        DatumReader<GenericRecord> readerWithoutSchema = new GenericDatumReader<>();

        try (ByteArrayInputStream is = new ByteArrayInputStream(data); DataFileStream<GenericRecord> dataFileReader = new DataFileStream<>(is, readerWithoutSchema)) {
            //Deserializing message to get the schema
            GenericRecord genericRecord = null;

            if (dataFileReader.hasNext()) {
                genericRecord = dataFileReader.next(genericRecord);
            }

            if (genericRecord == null) {
                return null;
            }

            Schema schema = genericRecord.getSchema();

            logger.debug("AVRO Schema: {}", schema);

            /**
             * The generic record returned from here doesn't return schema. It is a deserialized message using schema
             * returned from above deserialization.
             */

            SeekableByteArrayInput sin = new SeekableByteArrayInput(data);
            DatumReader<GenericRecord> readerWithSchema = genericData.createDatumReader(schema);

            return processGenericRecordWithSchema(sin, readerWithSchema);
        } catch (IOException e) {
            logger.error("error in reading data from input stream - {}", e.getMessage());
        }

        return null;
    }

    private static GenericRecord processGenericRecordWithSchema(SeekableByteArrayInput sin, DatumReader<GenericRecord> readerWithSchema) {

        try (DataFileReader<GenericRecord> in = new DataFileReader<>(sin, readerWithSchema)) {
            return in.next();

        } catch (IOException e) {
            logger.warn("Error in reading data from input stream:{}", e.getMessage());

        }

        return null;
    }
}
