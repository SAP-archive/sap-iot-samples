package com.sap.iot.kafka2hana.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "MEASURES")
public class Measures implements Serializable {
    /* spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl
       spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

       permits supporting to non-standard tables name, i.e. lowercase or camelCase
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //requires IDENTITY automatic definition on HANA
    @Column(name = "`id`")
    private long id;
    @Column(name = "`thingId`", length = 32)
    private String thingId;
    @Column(name = "`pst`", length = 125)
    private String pst;
    @Column(name = "`measurement`", length = 125)
    private String measurement;
    @Column(name = "`time`")
    private Date time;
    @Column(name = "`value`")
    private Float value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getThingId() {
        return thingId;
    }

    public void setThingId(String thingId) {
        this.thingId = thingId;
    }

    public String getPst() {
        return pst;
    }

    public void setPst(String pst) {
        this.pst = pst;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }
}