package com.sap.iot.kafka2hana.listener;

import com.sap.iot.kafka2hana.model.Measures;
import com.sap.iot.kafka2hana.repository.MeasuresRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.sap.iot.kafka2hana.util.Constants.*;

/**
 * process_timeseries topic consumption example
 * uses avro deserializer
 */
@Slf4j
public class ProcessTimeSeriesStreamingAccess implements AbstractStreamingAccess {

    @Autowired
    MeasuresRepository measuresRepository;

    public void processMessage(GenericRecord deserializedMessage) {
        log.debug("----- processing record: " + deserializedMessage);
        String thingId = deserializedMessage.get(MESSAGE_PROPERTY_THINGID).toString();
        String propertySetType = deserializedMessage.get(MESSAGE_PROPERTY_PST).toString();
        GenericData.Array<GenericRecord> measurements = (GenericData.Array<GenericRecord>) deserializedMessage.get(MESSAGE_PROPERTY_MEASUREMENTS);
        for (GenericRecord meas : measurements) {
            List<Schema.Field> fields = meas.getSchema().getFields();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date((long)meas.get(MESSAGE_PROPERTY_TIME));
            for (Schema.Field key : fields) {
                if (key.name().equalsIgnoreCase(MESSAGE_PROPERTY_TIME)) {
                    continue;
                }
                Float propertyValue = (Float) meas.get(key.name());
                if (!ObjectUtils.isEmpty(propertyValue)) {
                    Measures measure = new Measures();
                    measure.setThingId(thingId);
                    measure.setPst(propertySetType);
                    measure.setMeasurement(key.name());
                    measure.setValue(propertyValue);
                    measure.setTime(date);
                    measuresRepository.save(measure);
                }
            }
        }
    }
}
