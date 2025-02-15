package edu.wsu.cs320.gui.control;

public class GuiResponse<T> {
    public enum ResponseCode {
        OK,
        INCOMPLETE_DATA,
        WINDOW_CLOSED,
        CANCELLED
    }

    public GuiResponse(ResponseCode status, T data) {
        this.status = status;
        this.data = data;
    }


    public T data;
    public ResponseCode status;
}
