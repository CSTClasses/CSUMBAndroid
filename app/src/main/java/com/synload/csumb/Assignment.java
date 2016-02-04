package com.synload.csumb;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nathaniel on 2/3/2016.
 */
public class Assignment implements Serializable {
    public String title;
    public String clazz;
    public String description;
    public Date due;
    public String id;
    public Assignment(String title, String description, String clazz, Date due, String id){
        this.title = title;
        this.clazz = clazz;
        this.description = description;
        this.due = due;
        this.id = id;
    }
}
