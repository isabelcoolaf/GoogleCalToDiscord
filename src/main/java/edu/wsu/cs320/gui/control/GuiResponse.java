package edu.wsu.cs320.gui.control;

/** This class is used to let external code intelligently interact with data received from a */
public class GuiResponse<T> {
    /** The data attached to the GuiResponse. In the case of an error, this will be null. */
    public T data;
    /**
     * The status of the response.
     *
     * @see ResponseCode
     */
    public ResponseCode status;

    /** Defines the status of a request. */
    public enum ResponseCode {
        /** The request was successful, and the data is complete. */
        OK,
        /** The request was successful, but not all the data is filled in. */
        INCOMPLETE_DATA,
        /** The request failed because the window was closed by the user. */
        WINDOW_CLOSED,
        /** Some external circumstance caused the request to fail. */
        CANCELLED
    }

    /**
     * @param status The status of the response.
     * @param data   The data attached to the response. Should be null if there is an error.
     * @see ResponseCode
     */
    public GuiResponse(ResponseCode status, T data) {
        this.status = status;
        this.data = data;
    }

    @Override
    public String toString() {
        return "GuiResponse(status: " + status + ", data:" + data + ")";
    }
}
