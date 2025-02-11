package edu.wsu.cs320.googleapi;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class GoogleCalendarServiceHandler {

    private final Calendar service;

    public GoogleCalendarServiceHandler(UserCredentials credentials) {
        // The API library sends a warning to console if I don't set the Application Name, for some reason. It's not required?
        this.service = new Calendar.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google Calendar to Discord")
                .build();
    }

    public List<CalendarListEntry> getCalendarList() throws IOException {
        // https://developers.google.com/calendar/api/v3/reference/calendarList/list
        // 250 is max page size
        Calendar.CalendarList.List request = service.calendarList().list()
                .setMaxResults(250);
        CalendarList response = request.execute();
        List<CalendarListEntry> calendarList = response.getItems();

        while (response.getNextPageToken() != null && !response.getNextPageToken().isEmpty()) {
            request = request.setPageToken(response.getNextPageToken());
            response = request.execute();
            calendarList.addAll(response.getItems());
        }
        return calendarList;
    }

    // Thanks Google for naming two classes "Calendar".
    public com.google.api.services.calendar.model.Calendar createCalendar(String name) throws IOException {
        // https://developers.google.com/calendar/api/v3/reference/calendars/insert
        return service.calendars().insert(new com.google.api.services.calendar.model.Calendar().setSummary(name)).execute();
    }

    // Returns the list of events on the Calendar for the next month.
    public List<Event> getUpcomingEvents(String calendarID) throws IOException {
        // https://developers.google.com/calendar/api/v3/reference/events/list
        // 1 month is the maximum reminder time, so we want to do that to make sure we can send reminders
        // 2500 is max page size
        Calendar.Events.List request = service.events().list(calendarID)
                .setMaxResults(2500)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setTimeMin(new DateTime(Date.from(Instant.now())))
                .setTimeMax(new DateTime(Date.from(Instant.now().plus(30, ChronoUnit.DAYS))));
        Events response = request.execute();
        List<Event> eventList = response.getItems();

        while (response.getNextPageToken() != null && !response.getNextPageToken().isEmpty()) {
            request = request.setPageToken(response.getNextPageToken());
            response = request.execute();
            eventList.addAll(response.getItems());
        }
        return eventList;
    }

}
