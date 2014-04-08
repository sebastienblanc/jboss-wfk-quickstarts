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

import org.apache.deltaspike.security.api.authorization.Secures;
import org.jboss.quickstarts.wfk.contact.security.annotation.AllowedRoles;
import org.jboss.quickstarts.wfk.contact.security.annotation.UserLoggedIn;
import org.jboss.quickstarts.wfk.contact.security.model.ApplicationRole;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import static org.picketlink.idm.model.basic.BasicModel.getRole;

/**
 * <p>This class centralizes all authorization services for this application.</p>
 *
 * @author Pedro Igor
 *
 */
@ApplicationScoped
public class AuthorizationManager {

    @Inject
    private Instance<Identity> identityInstance;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    /**
     * <p>
     *  This authorization method provides the validation logic for resources annotated with the security annotation {@link org.jboss.quickstarts.wfk.contact.security.annotation.AllowedRoles}.
     * </p>
     * <p>
     *  Note that this method is also annotated with {@link Secures}, which is an annotation from Apache DeltaSpike.
     *  This annotation tells the @{link SecurityInterceptor} that this method must be called before the execution of
     *  methods annotated with {@link org.jboss.quickstarts.wfk.contact.security.annotation.AllowedRoles} in order to perform authorization checks.
     * </p>
     *
     * @param invocationContext
     * @param manager
     * @return true if the user can execute the method or class
     * @throws Exception
     */
    @Secures
    @AllowedRoles
    public boolean checkDeclaredRoles(InvocationContext invocationContext, BeanManager manager) throws Exception {
        if (!isLoggedIn()) {
            return false;
        }

        // administrators can access everything
        if (hasRole(ApplicationRole.ADMIN.name())) {
            return true;
        }

        Object targetBean = invocationContext.getTarget();
        AllowedRoles declareRoles = targetBean.getClass().getAnnotation(AllowedRoles.class);

        if (declareRoles == null) {
            declareRoles = invocationContext.getMethod().getAnnotation(AllowedRoles.class);
        }

        ApplicationRole[] requiredRoles = declareRoles.value();

        if (requiredRoles.length == 0) {
            throw new IllegalArgumentException("@AllowedRoles does not define any role.");
        }

        for (ApplicationRole requiredRole: requiredRoles) {
            if (hasRole(requiredRole.name())) {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>Authorization check for {@link org.jboss.quickstarts.wfk.contact.security.annotation.UserLoggedIn} annotation.</p>
     *
     * @return
     */
    @Secures
    @UserLoggedIn
    public boolean isLoggedIn() {
        return this.identityInstance.get().isLoggedIn();
    }

    public boolean hasRole(String roleName) {
        Account account = getIdentity().getAccount();
        Role role = getRole(this.identityManager, roleName);

        return BasicModel.hasRole(this.relationshipManager, account, role);
    }

    private Identity getIdentity() {
        return this.identityInstance.get();
    }
}