package org.synyx.urlaubsverwaltung.core.application.service.exception;

/**
 * Thrown in case the person of an application for leave tries to remind multiple times the privileged users to decide
 * about the application for leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class RemindAlreadySentException extends Exception {

    public RemindAlreadySentException(String message) {

        super(message);
    }
}
