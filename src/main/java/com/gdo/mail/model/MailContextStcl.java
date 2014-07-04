/**
 * Copyright GDO - 2003
 */
package com.gdo.mail.model;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.lang3.StringUtils;

import com.gdo.context.model.ContextStcl;
import com.gdo.mail.cmd.TestMailConnection;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class MailContextStcl extends FolderStcl {

	public interface Slot extends ContextStcl.Slot, FolderStcl.Slot {
		String PROTOCOL = "Protocol";
		String HOST = "Host";
		String PORT = "Port";
		String USER = "User";
		String PASSWD = "Passwd";
		String AUTH = "Auth";
		String START_TLS = "StartTLS";

		String MAILBOX = "Mailbox";
		String DIR = "Dir";
	}

	public interface Command extends ContextStcl.Command, FolderStcl.Command {
		String TEST_MAIL_CONNECTION = "TestMailConnection";
	}

	public MailContextStcl(StclContext stclContext) {
		super(stclContext, null);

		// SLOT PART

		singleSlot(Slot.CONTEXT);
		propSlot(Slot.PATH);

		propSlot(Slot.PROTOCOL);
		propSlot(Slot.HOST);
		propSlot(Slot.PORT);
		propSlot(Slot.AUTH, false);
		propSlot(Slot.USER);
		propSlot(Slot.PASSWD);
		propSlot(Slot.START_TLS, false);

		propSlot(Slot.MAILBOX);
		delegateSlot(Slot.DIR, Slot.MAILBOX);

		// COMMAND PART

		command(Command.TEST_CONNEXION, TestMailConnection.class);
		command(Command.TEST_MAIL_CONNECTION, TestMailConnection.class);
	}

	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {
		super.afterCompleted(stclContext, self);

		// put self as context for folder interface
		self.plug(stclContext, self, Slot.CONTEXT);
	}

	@Override
	public Folder getFolder(StclContext stclContext, PStcl self) {
		try {
			Properties props = System.getProperties();
			Session session = Session.getInstance(props, null);
			String protocol = self.getString(stclContext, Slot.PROTOCOL, "");
			if (StringUtils.isNotEmpty(protocol)) {
				Store store = session.getStore(protocol);
				String host = self.getString(stclContext, Slot.HOST, "");
				String user = self.getString(stclContext, Slot.USER, "");
				String password = self.getString(stclContext, Slot.PASSWD, "");
				store.connect(host, user, password);
				String mailbox = self.getString(stclContext, Slot.MAILBOX, "INBOX");
				return store.getDefaultFolder().getFolder(mailbox);
			}
		} catch (Exception e) {
			logWarn(stclContext, "cannot get mail folder (%s)", e);
		}
		return null;
	}

	/**
	 * @return A session to the smtp server.
	 */
	public Session connectSMTP(StclContext stclContext, PStcl self) {
		Authenticator auth = null;

		// gets SMTP session parameters
		Properties props = System.getProperties();
		props.put("mail.encoding", "UTF-8");
		String protocol = self.getString(stclContext, MailContextStcl.Slot.PROTOCOL, "");
		if (StringUtils.isBlank(protocol)) {
			protocol = "smtp";
		}
		props.put("mail.transport.protocol", protocol);
		String host = self.getString(stclContext, MailContextStcl.Slot.HOST, "");
		if (StringUtils.isBlank(host)) {
			host = "localhost";
		}
		props.put("mail.smtp.host", host);
		String port = self.getString(stclContext, MailContextStcl.Slot.PORT, "25");
		if ("0".equals(port)) {
			port = "25";
		}
		props.put("mail.smtp.port", port);
		if (self.getBoolean(stclContext, Slot.AUTH)) {
			props.put("mail.smtp.auth", "true");
			String user = self.getString(stclContext, MailContextStcl.Slot.USER, "");
			String passwd = self.getString(stclContext, MailContextStcl.Slot.PASSWD, "");
			if (StringUtils.isNotBlank(user))
				props.put("mail.smtp.user", user);
			if (StringUtils.isNotBlank(passwd))
				props.put("mail.smtp.password", passwd);
			auth = new MailAuthenticator(user, passwd);
		}
		boolean starttls = self.getBoolean(stclContext, Slot.START_TLS);
		props.put("mail.smtp.starttls.enable", Boolean.toString(starttls));

		// creates session
		if (auth != null) {
			return Session.getInstance(props, auth);
		}
		return Session.getInstance(props);
	}

	public class MailAuthenticator extends Authenticator {
		String _user;
		String _passwd;

		MailAuthenticator(String user, String passwd) {
			this._user = user;
			this._passwd = passwd;
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(this._user, this._passwd);
		}
	}

}