/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.contact.security.rest;

import org.jboss.quickstarts.wfk.contact.Contact;
import org.jboss.quickstarts.wfk.contact.ContactService;
import org.jboss.quickstarts.wfk.contact.security.model.UserRegistration;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <p>A RESTful endpoint to create new users.</p>
 *
 * @author Pedro Igor
 */
@Stateless
@Path("/security/registration")
public class UserRegistrationService {
    @Inject
    private Logger log;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private ContactService contactService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(UserRegistration userRegistration) {
        User newUser = new User(userRegistration.getEmail());

        newUser.setFirstName(userRegistration.getFirstName());
        newUser.setLastName(userRegistration.getLastName());

        if (BasicModel.getUser(this.identityManager, newUser.getLoginName()) != null) {
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("email", "The email is already registered.");
            return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
        } else {
            this.identityManager.add(newUser);

            Password password = new Password(userRegistration.getPassword());

            this.identityManager.updateCredential(newUser, password);

            Response.ResponseBuilder builder;
            try {
                Contact contact = copyToContact(userRegistration);

                contactService.create(contact);
                builder = Response.ok().entity(newUser);
            } catch (Exception e) {
                log.info("Exception - " + e.toString());
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", e.getMessage());
                builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
            }

            return builder.build();
        }
    }

    private Contact copyToContact(UserRegistration userRegistration) {
        Contact contact = new Contact();
        contact.setFirstName(userRegistration.getFirstName());
        contact.setLastName(userRegistration.getLastName());
        contact.setBirthDate(userRegistration.getBirthDate());
        contact.setEmail(userRegistration.getEmail());
        contact.setPhoneNumber(userRegistration.getPhoneNumber());
        return contact;
    }
}
