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
package org.jboss.quickstarts.wfk.contact.security.rest;

import org.jboss.quickstarts.wfk.contact.security.annotation.UserLoggedIn;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>This is a simple RESTful service that returns a HTTP Status Code 200 after a successful authentication.</p>
 *
 * <p>Before reaching this service, the {@link org.picketlink.authentication.web.AuthenticationFilter}, configured in <code>web.xml</code>,
 * kicks in to authenticate the user.</p>
 *
 * <p>The {@link org.jboss.quickstarts.wfk.contact.security.annotation.UserLoggedIn} enforce that only previously authenticated users are allowed to invoke this service.</p>
 *
 * @author Pedro Igor
 */
@Path("/security/login")
public class LoginService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @UserLoggedIn
    public Response login() {
        return Response.ok().build();
    }

}
