package org.synyx.urlaubsverwaltung.core.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Settings to sync absences with a Google calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Embeddable
public class GoogleCalendarSettings {

    @Column(name = "calendar_google_active")
    private boolean active = false;

    @Column(name = "calendar_google_calendar")
    private String calendar = "Urlaubsverwaltung";

    @Column(name = "calendar_google_application")
    private String application;

    @Column(name = "calendar_google_dataStoreDir")
    private String dataStoreDirectory;

    @Column(name = "calendar_google_clientSecret")
    private String clientSecret;

    public boolean isActive() {

        return active;
    }


    public void setActive(boolean active) {

        this.active = active;
    }


    public String getCalendar() {

        return calendar;
    }


    public void setCalendar(String calendar) {

        this.calendar = calendar;
    }


    public String getApplication() {

        return application;
    }


    public void setApplication(String application) {

        this.application = application;
    }


    public String getDataStoreDirectory() {

        return dataStoreDirectory;
    }


    public void setDataStoreDirectory(String dataStoreDirectory) {

        this.dataStoreDirectory = dataStoreDirectory;
    }


    public String getClientSecret() {

        return clientSecret;
    }


    public void setClientSecret(String clientSecret) {

        this.clientSecret = clientSecret;
    }
}
