package edu.wsu.cs320.gui.Customizer;

import edu.wsu.cs320.gui.control.GuiResponse;
import edu.wsu.cs320.gui.control.ResponsiveGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Customizer implements ResponsiveGUI<Customizer.CustomizerCode> {
    private JPanel mainPanel;
    private JPanel previewPanel;
    private JPanel buttonPanel;
    private JButton backButton;
    private JButton changeImageButton;
    private CompletableFuture<GuiResponse<CustomizerCode>> pendingResponse;

    public enum CustomizerCode {
        BACK, CHANGE_IMAGE
    }

    public Customizer() {
        backButton.addActionListener(e -> completeResponse(CustomizerCode.BACK));
        changeImageButton.addActionListener(e -> {
            completeResponse(CustomizerCode.CHANGE_IMAGE);
        });
    }

    private void completeResponse(CustomizerCode code) {
        if (pendingResponse == null) return;
        pendingResponse.complete(new GuiResponse<CustomizerCode>(GuiResponse.ResponseCode.OK, code));
    }

    @Override
    public JPanel getGuiPanel() {
        return mainPanel;
    }

    @Override
    public GuiResponse<CustomizerCode> getResponse() {
        pendingResponse = new CompletableFuture<>();
        GuiResponse<CustomizerCode> result = new GuiResponse<>(GuiResponse.ResponseCode.INCOMPLETE_DATA, null);
        try {
            result = pendingResponse.get();
        } catch (InterruptedException | ExecutionException e) {
            pendingResponse.complete(new GuiResponse<>(GuiResponse.ResponseCode.CANCELLED, null));
        }
        return result;
    }

    @Override
    public void onWindowClose() {
        if (pendingResponse == null) return;
        pendingResponse.complete(new GuiResponse<CustomizerCode>(GuiResponse.ResponseCode.WINDOW_CLOSED, null));
    }
}
