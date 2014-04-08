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

var currentUser;

APPMODULE.namespace('APPMODULE.security.loadCurrentUser');
APPMODULE.namespace('APPMODULE.security.submitSignIn');
APPMODULE.namespace('APPMODULE.security.submitSignUp');

APPMODULE.namespace('APPMODULE.security.restSignUpEndpoint');
APPMODULE.namespace('APPMODULE.security.restLogoutEndpoint');
APPMODULE.namespace('APPMODULE.security.restCurrentUserEndpoint');
APPMODULE.namespace('APPMODULE.security.restListUsersEndpoint');
APPMODULE.namespace('APPMODULE.security.restListRolesEndpoint');
APPMODULE.namespace('APPMODULE.security.restAssignRoleEndpoint');

APPMODULE.security.restSignUpEndpoint = 'rest/security/registration';
APPMODULE.security.restLogoutEndpoint = 'rest/security/logout';
APPMODULE.security.restCurrentUserEndpoint = 'rest/security/user/info';
APPMODULE.security.restListUsersEndpoint = 'rest/security/user';
APPMODULE.security.restListRolesEndpoint = 'rest/security/role';
APPMODULE.security.restAssignRoleEndpoint = 'rest/security/role/assign';

$(document).ready(function() {
    $.ajaxSetup({
        error: function (x, status, error) {
            if (x.status == 403) {
                $.mobile.changePage("#access-denied-dialog");
            } else if (x.status == 401) {
                $.mobile.changePage("#signin-page");
            }
        }
    });

    //Initialize all the AJAX form events.
    var initSecurity = function () {
        //Fetches the initial member data
        APPMODULE.security.submitSignIn();
        APPMODULE.security.submitSignUp();
        APPMODULE.security.submitAssignRole();
    };

    $('#logout-page').on( "pagebeforeshow", function(e) {
        var jqxhr = $.ajax({
            url: APPMODULE.security.restLogoutEndpoint,
            type: "POST"
        }).done(function(data, textStatus, jqXHR) {
            $.mobile.changePage('#signin-page');
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        });
    });

    $('#role-assignment-page').on( "pagebeforeshow", function(e) {
        if(e.handled !== true) {
            $('#role-assignment-users-select')
                .find('option')
                .remove()
                .end()
                .append('<option>Select an user</option>')
                .val('');
            $.ajax({
                url: APPMODULE.security.restListUsersEndpoint,
                cache: false,
                type: "GET"
            }).done(function(data, textStatus, jqXHR) {
                $.each(data, function(index, user) {
                    $("<option>").attr({"value":user.loginName}).html(user.loginName).appendTo("#role-assignment-users-select");
                });
                $("#role-assignment-users-select").selectmenu("refresh");
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.log("Could not query users - " +
                    " - jqXHR = " + jqXHR.status +
                    " - textStatus = " + textStatus +
                    " - errorThrown = " + errorThrown);
            });

            $('#role-assignment-role-select')
                .find('option')
                .remove()
                .end()
                .append('<option>Select a role</option>')
                .val('');
            $.ajax({
                url: APPMODULE.security.restListRolesEndpoint,
                cache: false,
                type: "GET"
            }).done(function(data, textStatus, jqXHR) {
                $.each(data, function(index, role) {
                    $("<option>").attr({"value":role}).html(role).appendTo("#role-assignment-role-select");
                });
                $("#role-assignment-role-select").selectmenu("refresh");
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.log("Could not query roles - " +
                    " - jqXHR = " + jqXHR.status +
                    " - textStatus = " + textStatus +
                    " - errorThrown = " + errorThrown);
            });
            e.handled = true;
        }
    });

    /**
     * Attempts to sign up using a JAX-RS POST.
     */
    APPMODULE.security.submitSignUp = function() {
        $("#signup-form").submit(function(event) {
            APPMODULE.validation.signUpFormValidator.form();
            if (APPMODULE.validation.signUpFormValidator.valid()){
                event.preventDefault();

                var serializedForm = $("#signup-form").serializeObject();
                var userData = JSON.stringify(serializedForm);

                var jqxhr = $.ajax({
                    url: APPMODULE.security.restSignUpEndpoint,
                    contentType: "application/json",
                    dataType: "json",
                    data: userData,
                    type: "POST"
                }).done(function(data, textStatus, jqXHR) {
                    APPMODULE.validation.formEmail = null;

                    $('#signup-form')[0].reset();
                    $('.invalid').remove();

                    $.mobile.changePage("#signin-page");
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    $('.invalid').remove();

                    if ((jqXHR.status === 409) || (jqXHR.status === 400)) {
                        var newUser = $("#signup-form")[0];
                        var errorMsg = $.parseJSON(jqXHR.responseText);

                        $.each(errorMsg, function(index, val) {
                            if (index === 'email'){
                                $.each(newUser, function(index, val){
                                    if (val.name == "email"){
                                        APPMODULE.validation.formEmail = val.value;
                                        return false;
                                    }
                                });
                            }
                        });

                        APPMODULE.validation.displayServerSideErrors("#signup-form", errorMsg);
                    } else {
                        var errorMsg = $.parseJSON(jqXHR.responseText);

                        APPMODULE.validation.displayServerSideErrors("#signup-form", errorMsg);
                    }
                });
            }
        });
    };

    /**
     * Attempts to sign in using a JAX-RS POST.
     */
    APPMODULE.security.submitSignIn = function() {
        $("#signin-form").submit(function(event) {
            APPMODULE.validation.signInFormValidator.form();

            if (APPMODULE.validation.signInFormValidator.valid()){
                event.preventDefault();

                var serializedForm = $("#signin-form").serializeObject();
                var userData = JSON.stringify(serializedForm);

                var jqxhr = $.ajax({
                    url: APPMODULE.security.restCurrentUserEndpoint,
                    contentType: "application/json",
                    dataType: "json",
                    headers: {
                        "Authorization": 'Basic ' + btoa(serializedForm.loginName + ":" + serializedForm.password)
                    },
                    type: "GET"
                }).done(function(data, textStatus, jqXHR) {
                    APPMODULE.validation.formEmail = null;

                    $('#signin-form')[0].reset();
                    $('.invalid').remove();

                    APPMODULE.security.loadCurrentUser();

                    $.mobile.changePage("#contacts-list-page");
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    if (jqXHR.status === 401) {
                        $.mobile.changePage("#invalid-credentials-dialog");
                    }
                });
            }
        });
    };

    /**
     * Attempts to role assignment using a JAX-RS POST.
     */
    APPMODULE.security.submitAssignRole = function() {
        $("#role-assignment-form").submit(function(event) {
            APPMODULE.validation.assignRoleFormValidator.form();

            if (APPMODULE.validation.assignRoleFormValidator.valid()){
                event.preventDefault();

                var serializedForm = $("#role-assignment-form").serializeObject();

                var jqxhr = $.ajax({
                    url: APPMODULE.security.restAssignRoleEndpoint + "/" + serializedForm.userName + "/" + serializedForm.roleName,
                    type: "POST"
                }).done(function(data, textStatus, jqXHR) {
                    $.mobile.changePage("#role-assignment-dialog");
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    alert('Could not assign role, please select an user and a role.');
                });
            }
        });
    };

    /**
     * Attempts to load information about the current user.
     */
    APPMODULE.security.loadCurrentUser = function() {
        var jqxhr = $.ajax({
            url: APPMODULE.security.restCurrentUserEndpoint,
            contentType: "application/json",
            dataType: "json",
            type: "GET",
            async: false
        }).done(function(data, textStatus, jqXHR) {
            currentUser = data;
        }).fail(function(jqXHR, textStatus, errorThrown) {
            currentUser = null;
        });
    };

    initSecurity();
});