package com.kmji.nghbr.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name="APP_EVENT")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Event {

    @Id
    @Column(name="ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name="TITLE", nullable=false)
    private String title;

    @Column(name="DESCRIPTION", nullable=true, length = 100000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "HOST_ID")
    private User host;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SURBURB_ID")
    private Suburb suburb;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="START_DATE")
    private java.util.Date start;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="END_DATE")
    private java.util.Date end;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade=CascadeType.ALL, mappedBy="event")
    private List<Attendee> attendees;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getHost() {
        return host;
    }

    public void setOwner(User host) {
        this.host = host;
    }

    public java.util.Date getStart() {
        return start;
    }

    public void setStart(java.util.Date start) {
        this.start = start;
    }

    public java.util.Date getEnd() {
        return end;
    }

    public void setEnd(java.util.Date end) {
        this.end = end;
    }

    public Suburb getSuburb() { return suburb; }
    public void setSuburb(Suburb suburb) { this.suburb = suburb; }

    public List<Attendee> getAttendees() {return attendees;}
    public void setAttendees(List<Attendee> attendees) {this.attendees = attendees;}

    public String getJSONString() {
        return "{" + "id:" + id + ",url:" + "'/events/" + id + "',title:"  + "'"+ title  + "'" + ", start:" + start.getTime() + ", end:" + end.getTime() + ",host:"  + "'"+ host.getFirstName() + " " + host.getLastName()  + "'" + "}";
    }

}
