package org.synyx.urlaubsverwaltung.core.startup;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteStatus;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Locale;

import javax.annotation.PostConstruct;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class TestDataCreationService {

    private static final Logger LOG = Logger.getLogger(TestDataCreationService.class);

    private static final String IN_MEMORY_DATABASE = "h2";
    private static final String USER = "test";

    private static final Boolean ACTIVE = true;
    private static final Boolean INACTIVE = false;

    @Autowired
    private PersonInteractionService personInteractionService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private SickNoteService sickNoteService;

    private Person user;
    private Person boss;
    private Person office;

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        String dbType = System.getProperties().getProperty("db");

        if (dbType == null) {
            dbType = "h2";
        }

        LOG.info("Using database type = " + dbType);

        if (dbType.equals(IN_MEMORY_DATABASE)) {
            LOG.info("Test data will be created...");

            user = createTestPerson(USER, "Marlene", "Muster", "mmuster@muster.de", ACTIVE, Role.USER, Role.BOSS,
                    Role.OFFICE);

            boss = createTestPerson("max", "Max", "Mustermann", "maxMuster@muster.de", ACTIVE, Role.USER, Role.BOSS);

            office = createTestPerson("klaus", "Klaus", "Müller", "müller@muster.de", ACTIVE, Role.USER, Role.BOSS,
                    Role.OFFICE);

            createTestPerson("horst", "Horst", "Dieter", "hdieter@muster.de", INACTIVE, Role.INACTIVE);

            createTestData(user);
            createTestData(boss);
        } else {
            LOG.info("No test data is created.");
        }
    }


    private Person createTestPerson(String login, String firstName, String lastName, String email, boolean active,
        Role... roles) throws NoSuchAlgorithmException {

        Person person = new Person();

        int currentYear = DateMidnight.now().getYear();
        Locale locale = Locale.GERMAN;

        PersonForm personForm = new PersonForm();
        personForm.setLoginName(login);
        personForm.setLastName(lastName);
        personForm.setFirstName(firstName);
        personForm.setEmail(email);
        personForm.setActive(active);
        personForm.setYear(String.valueOf(currentYear));
        personForm.setAnnualVacationDays("28.5");
        personForm.setRemainingVacationDays("5");
        personForm.setRemainingVacationDaysExpire(true);
        personForm.setValidFrom(new DateMidnight(currentYear, 1, 1));
        personForm.setWorkingDays(Arrays.asList(Day.MONDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek(),
                Day.WEDNESDAY.getDayOfWeek(), Day.THURSDAY.getDayOfWeek(), Day.FRIDAY.getDayOfWeek()));
        personForm.setPermissions(Arrays.asList(roles));

        personInteractionService.createOrUpdate(person, personForm, locale);

        return person;
    }


    private void createTestData(Person person) {

        DateMidnight now = DateMidnight.now();

        // FUTURE APPLICATIONS FOR LEAVE
        createWaitingApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.plusDays(10), now.plusDays(16));
        createWaitingApplication(person, VacationType.OVERTIME, DayLength.FULL, now.plusDays(1), now.plusDays(2));
        createWaitingApplication(person, VacationType.SPECIALLEAVE, DayLength.FULL, now.plusDays(4), now.plusDays(6));

        // PAST APPLICATIONS FOR LEAVE
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(20), now.minusDays(13));
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.MORNING, now.minusDays(5), now.minusDays(5));

        createRejectedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(33), now.minusDays(30));

        createCancelledApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(11), now.minusDays(10));

        // SICK NOTES
        createSickNote(person, now.minusDays(10), now.minusDays(10), false);
        createSickNote(person, now.minusDays(30), now.minusDays(25), true);
    }


    private Application createWaitingApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setVacationType(vacationType);
        application.setHowLong(dayLength);
        application.setComment("Ich hätte gerne Urlaub");

        applicationInteractionService.apply(application, person);

        return application;
    }


    private Application createAllowedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        Comment comment = new Comment();
        comment.setReason("Ist ok");

        applicationInteractionService.allow(application, boss, comment);

        return application;
    }


    private Application createRejectedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        Comment comment = new Comment();
        comment.setReason("Leider nicht möglich");

        applicationInteractionService.reject(application, boss, comment);

        return application;
    }


    private Application createCancelledApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createAllowedApplication(person, vacationType, dayLength, startDate, endDate);

        Comment comment = new Comment();
        comment.setReason("Urlaub wurde doch nicht genommen");

        applicationInteractionService.cancel(application, office, comment);

        return application;
    }


    private SickNote createSickNote(Person person, DateMidnight startDate, DateMidnight endDate, boolean withAUB) {

        SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setActive(ACTIVE);

        if (withAUB) {
            sickNote.setAubPresent(true);
            sickNote.setAubStartDate(startDate);
            sickNote.setAubEndDate(endDate);
        }

        sickNoteService.touch(sickNote, SickNoteStatus.CREATED, office);

        return sickNote;
    }
}