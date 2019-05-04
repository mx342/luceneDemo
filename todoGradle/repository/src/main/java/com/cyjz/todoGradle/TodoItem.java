package com.cyjz.todoGradle;

public class TodoItem {

    private String name;

    private boolean hasDone;

    public TodoItem(){

    }
    public TodoItem(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHasDone() {
        return hasDone;
    }

    public void setHasDone(boolean hasDone) {
        this.hasDone = hasDone;
    }

    @Override
    public String toString() {
        return "name ："+ name + (hasDone? "hasDone" : "need to done");

    }
}
