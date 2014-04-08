/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.quickstarts.wfk.contact.security;

import org.jboss.quickstarts.wfk.contact.security.model.ApplicationRole;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import static org.picketlink.idm.model.basic.BasicModel.getRole;
import static org.picketlink.idm.model.basic.BasicModel.getUser;
import static org.picketlink.idm.model.basic.BasicModel.grantRole;

/**
 * <p>Initializes the identity stores with some default users and roles.</p>
 *
 * @author Pedro Igor
 */
@Startup
@Singleton
public class IDMInitializer {

    @Inject
    private PartitionManager partitionManager;

    @PostConstruct
    public void createUsers() {
        createUser("john", "john", ApplicationRole.USER);
        createUser("duke", "duke", ApplicationRole.MAINTAINER);
        createUser("admin", "admin", ApplicationRole.ADMIN);
    }

    private void createUser(String loginName, String password, ApplicationRole userRole) {
        IdentityManager identityManager = this.partitionManager.createIdentityManager();

        // user already exists
        if (getUser(identityManager, loginName) != null) {
            return;
        }

        User user = new User(loginName);

        identityManager.add(user);

        Password credential = new Password(password);

        identityManager.updateCredential(user, credential);

        Role role = getRole(identityManager, userRole.name());

        if (role == null) {
            role = new Role(userRole.name());
            identityManager.add(role);
        }

        RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

        grantRole(relationshipManager, user, role);
    }

}
