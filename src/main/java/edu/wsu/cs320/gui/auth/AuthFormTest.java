package edu.wsu.cs320.gui.auth;

import edu.wsu.cs320.gui.control.GuiResponse;
import org.fest.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Integration test for AuthForm's ability to create correct GuiResponse instances */
public class AuthFormTest {
    private FrameFixture window;
    private AuthForm auth;

    public class GetResponseThread extends Thread {
        public CompletableFuture<GuiResponse<String[]>> result = new CompletableFuture<>();

        @Override
        public void run() {
            result.complete(auth.getResponse());
        }

    }

    // Creates a test window displaying an authForm
    @BeforeEach
    public void setup() {
        JFrame dummyFrame = new JFrame();
        auth = new AuthForm();
        dummyFrame.setContentPane(auth.getGuiPanel());
        dummyFrame.setVisible(true);
        dummyFrame.setSize(600, 600);
        dummyFrame.setLocationRelativeTo(null);
        dummyFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dummyFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                auth.onWindowClose();
            }
        });
        window = new FrameFixture(dummyFrame);
        window.requireVisible();
    }

    @AfterEach
    public void finish() {
        window.cleanUp();
    }

    public void setText(String googleId, String googleSecret, String discordId, String discordToken) {
        if (!window.textBox("googleIdField").text().equals(googleId)) {
            window.textBox("googleIdField").setText(googleId);
        }
        if (!window.textBox("googleSecretField").text().equals(googleSecret)) {
            window.textBox("googleSecretField").setText(googleSecret);
        }
        if (!window.textBox("discordIdField").text().equals(discordId)) {
            window.textBox("discordIdField").setText(discordId);
        }
        if (!window.textBox("discordTokenField").text().equals(discordToken)) {
            window.textBox("discordTokenField").setText(discordToken);
        }
    }

    // Integration test between AuthForm and GuiResponse for incomplete input
    @Test
    public void incompleteResponseTest() throws InterruptedException {
        GetResponseThread thread = new GetResponseThread();
        // only GoogleIdField
        thread.start();
        setText("something", "", "", "");
        window.button("saveButton").click();
        assertResponseIncomplete(thread);
        // only googleSecretField
        thread = new GetResponseThread();
        thread.start();
        setText("", "something", "", "");
        window.button("saveButton").click();
        assertResponseIncomplete(thread);
        // only discordIdField
        thread = new GetResponseThread();
        thread.start();
        setText("", "", "something", "");
        window.button("saveButton").click();
        assertResponseIncomplete(thread);
        // only discordTokenField
        thread = new GetResponseThread();
        thread.start();
        setText("", "", "", "something");
        window.button("saveButton").click();
        assertResponseIncomplete(thread);
    }

    // Integration test between AuthForm and GuiResponse for complete input
    @Test
    public void completeResponseTest() {
        GetResponseThread thread = new GetResponseThread();
        thread.start();
        setText("something", "something", "something", "something");
        window.button("saveButton").click();
        assertResponseComplete(thread);
    }

    // Integration test between AuthForm and GuiResponse for window close event
    @Test
    public void closeWindowTest() {
        GetResponseThread thread = new GetResponseThread();
        thread.start();
        window.close();
        assertWindowClosed(thread);
    }

    private void assertWindowClosed(GetResponseThread thread) {
        GuiResponse<String[]> response;
        try {
            response = thread.result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("There was an uncaught runtime error.");
        }
        if (response.status != GuiResponse.ResponseCode.WINDOW_CLOSED) {
            throw new AssertionError("This response should be window_closed, but isn't.");
        }
        if (response.data != null) {
            throw new AssertionError("The data was marked as window_closed, but still contained data.");
        }
    }


    public void assertResponseIncomplete(GetResponseThread t) {
        GuiResponse<String[]> response;
        try {
            response = t.result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("There was an uncaught runtime error.");
        }
        if (response.status != GuiResponse.ResponseCode.INCOMPLETE_DATA) {
            throw new AssertionError("This data should be incomplete, but isn't.");
        }
        if (response.data != null) {
            throw new AssertionError("The data was marked as incomplete, but still contained data.");
        }
    }

    public void assertResponseComplete(GetResponseThread t) {
        GuiResponse<String[]> response;
        try {
            response = t.result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("There was an uncaught runtime error.");
        }
        if (response.status != GuiResponse.ResponseCode.OK) {
            throw new AssertionError("Response should be complete, but isn't");
        }
        if (response.data == null) {
            throw new AssertionError("Response marked as complete, but data is null");

        }
    }
}