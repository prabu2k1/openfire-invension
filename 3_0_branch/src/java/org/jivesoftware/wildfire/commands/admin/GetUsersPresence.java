/**
 * $Revision: $
 * $Date: $
 *
 * Copyright (C) 2006 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution.
 */

package org.jivesoftware.wildfire.commands.admin;

import org.dom4j.Element;
import org.jivesoftware.wildfire.ClientSession;
import org.jivesoftware.wildfire.SessionManager;
import org.jivesoftware.wildfire.commands.AdHocCommand;
import org.jivesoftware.wildfire.commands.SessionData;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Command that allows to retrieve the presence of all active users.
 *
 * @author Gaston Dombiak
 *
 * TODO Use i18n
 */
public class GetUsersPresence extends AdHocCommand {

    protected void addStageInformation(SessionData data, Element command) {
        DataForm form = new DataForm(DataForm.Type.form);
        form.setTitle("Requesting Presence of Active Users");
        form.addInstruction("Fill out this form to request the active users presence of this service.");

        FormField field = form.addField();
        field.setType(FormField.Type.hidden);
        field.setVariable("FORM_TYPE");
        field.addValue("http://jabber.org/protocol/admin");

        field = form.addField();
        field.setType(FormField.Type.list_single);
        field.setLabel("Maximum number of items to return");
        field.setVariable("max_items");
        field.addOption("25", "25");
        field.addOption("50", "50");
        field.addOption("75", "75");
        field.addOption("100", "100");
        field.addOption("150", "150");
        field.addOption("200", "200");
        field.addOption("None", "none");

        // Add the form to the command
        command.add(form.getElement());
    }

    public void execute(SessionData data, Element command) {
        String max_items = data.getData().get("max_items").get(0);
        int maxItems = -1;
        if (max_items != null && "none".equals(maxItems)) {
            try {
                maxItems = Integer.parseInt(max_items);
            }
            catch (NumberFormatException e) {
                // Do nothing. Assume that all users are being requested
            }
        }

        DataForm form = new DataForm(DataForm.Type.result);

        FormField field = form.addField();
        field.setType(FormField.Type.hidden);
        field.setVariable("FORM_TYPE");
        field.addValue("http://jabber.org/protocol/admin");

        field = form.addField();
        field.setLabel("The presences of active users");
        field.setVariable("activeuserpresences");

        // Get list of users (i.e. bareJIDs) that are connected to the server
        Collection<ClientSession> sessions = SessionManager.getInstance().getSessions();
        int index = 1;
        for (ClientSession session : sessions) {
            if (session.getPresence().isAvailable()) {
                field.addValue(session.getPresence().toXML());
            }
            if (maxItems > 0 && index >= maxItems) {
                break;
            }
        }
        command.add(form.getElement());
    }

    public String getCode() {
        return "http://jabber.org/protocol/admin#get-active-presences";
    }

    public String getDefaultLabel() {
        return "Get Presence of Active Users";
    }

    protected List<Action> getActions(SessionData data) {
        return Arrays.asList(Action.complete);
    }

    protected Action getExecuteAction(SessionData data) {
        return Action.complete;
    }

    public int getMaxStages(SessionData data) {
        return 1;
    }
}