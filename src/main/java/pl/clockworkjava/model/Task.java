package pl.clockworkjava.model;

import java.time.LocalDateTime;

public class Task {
    String name;
    LocalDateTime dateTime;
    String oldName;

    public Task(String name){
        this.name=name;
    }


    public Task(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

}
