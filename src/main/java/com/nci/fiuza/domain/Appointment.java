package com.nci.fiuza.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

//adding a constraint to prevent double booking (same date and time) directly on the database
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id //primary key on the db
    @GeneratedValue(strategy = GenerationType.IDENTITY) //to generate the ids automatically incremental
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status; //BOOKED / CANCELLED / COMPLETED

    @Column(length = 500)
    private String notes;

    //RELATIONSHIPS
    //many appointments belong to one customer (user)
    @ManyToOne(optional = false, fetch = FetchType.LAZY) //instead of the default EAGER, so it won't load the objects (customer) from the db until it is actually used (getCustomer())
    @JoinColumn(name = "customer_id", nullable = false) //this will generate column customer_id, FK to users.id
    private User customer;

    //many appointments belong to one service
    @ManyToOne(optional = false, fetch = FetchType.LAZY) //instead of the default EAGER, so it won't load the objects (service) from the db until it is actually used (getService())
    @JoinColumn(name = "service_id", nullable = false) //this will generate column service_id, FK to services.id
    private Service service;

    public Appointment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    //return appointment time slot, instead of just time (08:00), it returns time + service duration (08:00 - 10:00)
    public String getTimeSlot(){
        return getTime() + " - " + getTime().plusMinutes(getService().getEstimatedTimeMinutes());
    }

}
