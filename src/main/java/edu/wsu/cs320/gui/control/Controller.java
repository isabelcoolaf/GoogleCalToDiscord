package edu.wsu.cs320.gui.control;

public class Controller {

    public enum StateEnum{
        AUTH,
        SELECT,
        CUSTOMIZE
    }

    private StateEnum state;

    public Controller() {

    }
}
