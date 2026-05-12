package com.nci.fiuza.domain;

import com.nci.fiuza.util.DurationUtils;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
public class Service {

    @Id //primary key on the db
    @GeneratedValue(strategy = GenerationType.IDENTITY) //to generate the ids automatically incremental
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    //10 digits, 2 decimals
    @Column(nullable = false, precision = 10, scale = 2) //10 digits, 2 decimals
    private BigDecimal price;

    @Column(nullable = false)
    private Integer estimatedTimeMinutes;

    @Column(nullable = false)
    private boolean active = true; //default value

    @Column(name = "image_file_name", length = 255)
    private String imageFileName;

    //constructor
    public Service() {
    }

    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getEstimatedTimeMinutesDescription() {
        return DurationUtils.formatMinutes(estimatedTimeMinutes);
    }

}
