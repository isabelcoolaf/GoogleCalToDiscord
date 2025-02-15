package edu.wsu.cs320.gui.control;

import javax.swing.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import com.google.api.services.calendar.model.Calendar;

public interface ResponsiveGUI {
    
    JPanel guiGuiPanel();

    GuiResponse getResponse();


}
