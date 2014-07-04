/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.mail.cmd.AddAttachment;
import com.gdo.mail.cmd.IsMailValid;
import com.gdo.mail.cmd.MultiSendMail;
import com.gdo.mail.cmd.SendComposed;
import com.gdo.mail.cmd.SendMail;
import com.gdo.project.cmd.CreateAtomic;
import com.gdo.project.model._CommandThread;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.sun.mail.smtp.SMTPMessage;

// the mail itself is  mail send listener.
public class MailStcl extends Stcl implements IMail, IMailSendListener {

	// true to force sending
	protected static final boolean SENDING = true;

	public interface Slot extends Stcl.Slot {
		String MAIL_CONTEXT = "MailContext";

		String FROM = "From";
		String FROM_NAME = "FromName";
		String TO = "To";
		String CC = "CC";
		String BCC = "BCC";

		String TITLE = "Title";
		String CONTENT_TYPE = "ContentType";
		String ENCODING_TYPE = "EncodingType";
		String CONTENT = "Content";

		String ATTACHMENTS = "Attachments";

		String SENT_DATE = "SentDate";
	}

	public interface Command extends Stcl.Command {
		String IS_VALID = "IsValid";
		String SEND = "Send";
		String MULTI_SEND = "MultiSend"; // sends to several emails

		String SEND_COMPOSED = "SendComposed"; // send in 2 steps (show, send)
		String ADD_TO_RECIPIENT = "AddToRecipient";
		String ADD_ATTACHMENT = "AddAttachment";
	}

	public interface Index {
		int SENT = 1;
		int ERROR = 2;
	}

	// stencil listening to the mail send
	private PStcl _listener;

	// specific formatter added to the mail
	private ContentFormatter _contentFormatter = null;

	public MailStcl(StclContext stclContext) {
		super(stclContext);

		// SLOT PART

		singleSlot(Slot.MAIL_CONTEXT);

		singleSlot(Slot.FROM);
		propSlot(Slot.FROM_NAME);
		multiSlot(Slot.TO);
		multiSlot(Slot.CC);
		multiSlot(Slot.BCC);

		propSlot(Slot.TITLE);
		propSlot(Slot.CONTENT);
		propSlot(Slot.CONTENT_TYPE, "text/plain");
		propSlot(Slot.ENCODING_TYPE, "utf-8");

		multiSlot(Slot.ATTACHMENTS);

		// COMMAND PART
		command(Command.IS_VALID, IsMailValid.class);
		command(Command.SEND, SendMail.class);
		command(Command.MULTI_SEND, MultiSendMail.class);

		command(Command.SEND_COMPOSED, SendComposed.class, "simple");
		command(Command.ADD_TO_RECIPIENT, CreateAtomic.class, RecipientStcl.class.getName(), "Target/To", "int", "1");
		command(Command.ADD_ATTACHMENT, AddAttachment.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.mail.model.IMail#addSendListener(com.gdo.project.StclContext,
	 * com.gdo.project.PStcl, com.gdo.project.PStcl)
	 */
	@Override
	public void addSendListener(StclContext stclContext, PStcl listener, PStcl self) {

		// checks listener implements the IMailSendListener interface
		if (listener.getReleasedStencil(stclContext) instanceof IMailSendListener) {
			this._listener = listener;
		} else {
			logWarn(stclContext, "the mail listener %s must implement the IMailSendListener interface for %s", listener, self);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdo.mail.model.IMail#setContentFormatter(com.gdo.project.StclContext,
	 * com.gdo.mail.model.IMail.ContentFormatter, com.gdo.project.PStcl)
	 */
	@Override
	public void setContentFormatter(StclContext stclContext, ContentFormatter formatter, PStcl self) {
		this._contentFormatter = formatter;
	}

	/*
	 * IMailSendListener interface
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdo.mail.model.IMailSendListener#beforeSend(com.gdo.project.StclContext
	 * , com.gdo.project.PStcl, com.gdo.project.PStcl, com.gdo.project.PStcl)
	 */
	@Override
	public Result beforeSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
		if (StencilUtils.isNotNull(this._listener)) {
			StclContext stclContext = cmdContext.getStencilContext();
			IMailSendListener listener = (IMailSendListener) this._listener.getReleasedStencil(stclContext);
			return listener.beforeSend(cmdContext, self, recipient, this._listener);
		}
		return Result.success();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdo.mail.model.IMailSendListener#afterSend(com.gdo.project.StclContext,
	 * com.gdo.project.PStcl, com.gdo.project.PStcl, com.gdo.project.PStcl)
	 */
	@Override
	public Result afterSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
		if (StencilUtils.isNotNull(this._listener)) {
			StclContext stclContext = cmdContext.getStencilContext();
			IMailSendListener listener = (IMailSendListener) this._listener.getReleasedStencil(stclContext);
			return listener.afterSend(cmdContext, self, recipient, this._listener);
		}
		return Result.success();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.mail.model.IMailSendListener#afterError(com.gdo.stencils.cmd.
	 * CommandContext, com.gdo.project.PStcl, com.gdo.project.PStcl,
	 * java.lang.String, com.gdo.project.PStcl)
	 */
	@Override
	public Result afterError(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, String reason, PStcl self) {
		if (StencilUtils.isNotNull(this._listener)) {
			StclContext stclContext = cmdContext.getStencilContext();
			IMailSendListener listener = (IMailSendListener) this._listener.getReleasedStencil(stclContext);
			return listener.afterError(cmdContext, self, recipient, reason, this._listener);
		}
		return Result.success();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.mail.model.IMailSendListener#beforeFirst(com.gdo.stencils.cmd.
	 * CommandContext, com.gdo.project.PStcl, com.gdo.project.PStcl,
	 * com.gdo.project.PStcl)
	 */
	@Override
	public Result beforeFirst(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl self) {
		if (StencilUtils.isNotNull(this._listener)) {
			StclContext stclContext = cmdContext.getStencilContext();
			IMailSendListener listener = (IMailSendListener) this._listener.getReleasedStencil(stclContext);
			return listener.beforeFirst(cmdContext, self, this._listener);
		}
		return Result.success();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.mail.model.IMailSendListener#afterLast(com.gdo.stencils.cmd.
	 * CommandContext, com.gdo.project.PStcl, com.gdo.project.PStcl,
	 * com.gdo.project.PStcl)
	 */
	@Override
	public Result afterLast(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl self) {
		if (StencilUtils.isNotNull(this._listener)) {
			StclContext stclContext = cmdContext.getStencilContext();
			IMailSendListener listener = (IMailSendListener) this._listener.getReleasedStencil(stclContext);
			return listener.afterLast(cmdContext, self, this._listener);
		}
		return Result.success();
	}

	protected int getWaitingTime(StclContext stclContext, PStcl self) {
		return 3000;
	}

	/*
	 * IMail interface
	 */

	/* (non-Javadoc)
	 * @see com.gdo.mail.model.IMail#send(com.gdo.stencils.cmd.CommandContext, java.util.Properties, com.gdo.project.PStcl)
	 */
	@Override
	public Result send(CommandContext<StclContext, PStcl> cmdContext, Properties props, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		List<InternetAddress> addrs = null;

		// checks context
		PStcl mailContext = self.getStencil(stclContext, Slot.MAIL_CONTEXT);
		if (StencilUtils.isNull(mailContext)) {
			return Result.error(getClass().getName(), "No mail context");
		}
		MailContextStcl stencil = mailContext.getReleasedStencil(stclContext);
		Session session = stencil.connectSMTP(stclContext, mailContext);
		if (session == null) {
			return Result.error(getClass().getName(), "Cannot connect to SMTP for session");
		}

		try {

			// performs before sending
			addrs = getInternetAddressesFromProperty(props, CommandContext.PARAM(3));
			if (addrs == null) {
				for (PStcl rec : self.getStencils(stclContext, Slot.TO)) {
					Result result = beforeSend(cmdContext, self, rec, self);
					if (result.isNotSuccess())
						return result;
				}
			}

			// gets mime message
			MimeMessage mimeMsg = getMimeMessageWithoutTo(stclContext, session, props, self);
			if (mimeMsg == null) {
				return Result.error(getClass().getName(), "Cannot create mime message");
			}

			// adds all to recipients to the mail
			String toList = "";
			if (addrs == null) {
				for (PStcl recipient : self.getStencils(stclContext, Slot.TO)) {

					// gets internet address
					if (StencilUtils.isNull(recipient)) {
						logWarn(stclContext, "No mail recipient for to (%s)", recipient.getNullReason());
						continue;
					}
					IRecipient rec = (IRecipient) recipient.getReleasedStencil(stclContext);
					Result result = rec.getInternetAddress(stclContext, recipient);
					if (result.isNotSuccess()) {
						afterError(cmdContext, self, recipient, result.getMessage(), self);
						continue;
					}

					// usefull????
					InternetAddress add = result.getSuccessValue();
					if (add == null) {
						logWarn(stclContext, "Cannot create internet address");
						continue;
					}

					// adds it to the mail
					mimeMsg.addRecipient(Message.RecipientType.TO, add);
					toList += add.getAddress();
				}
			} else {
				for (InternetAddress add : addrs) {
					mimeMsg.addRecipient(Message.RecipientType.TO, add);
					toList += add.getAddress();
				}
			}

			// sends the message and return result
			if (SENDING) {
				Transport.send(mimeMsg);
			}
			logWarn(stclContext, "message sent to %s", toList);

			// performs after sending
			if (addrs == null) {
				for (PStcl rec : self.getStencils(stclContext, Slot.TO)) {
					afterSend(cmdContext, self, rec, self);
				}
			}
		} catch (Exception e) {
			String msg = logError(stclContext, "Cannot send message : %s", e);

			// performs after error sending
			if (addrs == null) {
				for (PStcl rec : self.getStencils(stclContext, Slot.TO)) {
					afterError(cmdContext, self, rec, msg, self);
				}
			}

			return Result.error(msg);
		}

		// returns result
		return Result.success("Mail sent");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.mail.model.IMail#multiSend(com.gdo.project.StclContext,
	 * java.util.Properties, com.gdo.project.PStcl)
	 */
	@Override
	public Result multiSend(CommandContext<StclContext, PStcl> cmdContext, Properties props, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		try {

			// opens SMTP session
			PStcl mailContext = self.getStencil(stclContext, Slot.MAIL_CONTEXT);
			if (StencilUtils.isNull(mailContext)) {
				String msg = logWarn(stclContext, "No mail context for mail %s", self);
				return Result.error(msg);
			}
			MailContextStcl stencil = mailContext.getReleasedStencil(stclContext);
			Session session = stencil.connectSMTP(stclContext, mailContext);
			if (session == null) {
				String msg = logWarn(stclContext, "Cannot connect to SMTP server for mail %s", self);
				return Result.error(msg);
			}

			// sends the message to each to recipients and return results
			int counter = 0;
			List<InternetAddress> addrs = getInternetAddressesFromProperty(props, CommandContext.PARAM(3));
			if (addrs != null) {
				for (InternetAddress address : addrs) {

					// gets mime message with content
					MimeMessage mimeMsg = getMimeMessageWithoutTo(stclContext, session, props, self);
					if (mimeMsg == null) {
						String msg = logWarn(stclContext, "cannot get mime message for mail %s with to address %s", self, address);
						return Result.error(msg);
					}

					// sends message
					Result result = sendMessageTo(stclContext, session, mimeMsg, address, self);
					if (result.isSuccess()) {
						counter++;
					}

					// waiting time
					Thread.sleep(getWaitingTime(stclContext, self));
				}

				// returns result
				String msg = String.format("Nombre de messages envoyés : %s", counter);
				return Result.success(msg);
			}

			// sends mails using threads
			StencilIterator<StclContext, PStcl> to = getTo(stclContext, self);
			startSendingThread(cmdContext, mailContext, props, to, self);

			// returns result
			String msg = String.format("Nombre de messages en cours d'envoi : %s", to.size());
			return Result.success(msg);
		} catch (Exception e) {
			String msg = logWarn(stclContext, "exception in multiSend %s", e);
			return Result.error(msg);
		}
	}

	public StencilIterator<StclContext, PStcl> getTo(StclContext stclContext, PStcl self) {
		return self.getStencils(stclContext, Slot.TO);
	}

	public String getContent(StclContext stclContext, Properties props, PStcl self) {
		String content = self.getExpandedString(stclContext, Slot.CONTENT, "");

		// add also content formatter
		if (this._contentFormatter != null) {
			content = this._contentFormatter.getContent(stclContext, content, self);
		}

		return content;
	}

	protected void startSendingThread(CommandContext<StclContext, PStcl> cmdContext, PStcl mailContext, Properties props, StencilIterator<StclContext, PStcl> to, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		MailContextStcl stencil = mailContext.getReleasedStencil(stclContext);
		Session session = stencil.connectSMTP(stclContext, mailContext);
		new MultiSendThread(cmdContext, session, props, to, self);
	}

	protected SMTPMessage createMimeMessage(Session session) throws MessagingException {
		return new SMTPMessage(session);
	}

	protected MimeMessage getMimeMessageWithoutTo(StclContext stclContext, Session session, Properties props, PStcl self) throws Exception {

		// creates message
		SMTPMessage smtp_message = createMimeMessage(session);
		if (smtp_message == null) {
			logError(stclContext, "Cannot create SMTP message from session");
			return null;
		}

		// sets title
		String title = props.getProperty(CommandContext.PARAM(1));
		if (title == null) {
			title = self.getExpandedString(stclContext, Slot.TITLE, "");
		}
		if (StringUtils.isNotBlank(title)) {
			smtp_message.setSubject(title);
		}

		// sets from
		String from = props.getProperty(CommandContext.PARAM(2));
		if (from == null) {
			String fromPath = PathUtils.compose(Slot.FROM, RecipientStcl.Slot.ADDRESS);
			from = self.getExpandedString(stclContext, fromPath, "");
		}
		if (StringUtils.isBlank(from)) {
			logError(stclContext, "The message has no from address");
			return null;
		}
		InternetAddress intFrom = new InternetAddress(from);

		// sets from name
		String fromName = self.getString(stclContext, Slot.FROM_NAME, null);
		if (StringUtils.isNotBlank(fromName)) {
			intFrom.setPersonal(fromName);
		}

		smtp_message.setFrom(intFrom);
		smtp_message.setSender(intFrom);

		// sets cc
		List<InternetAddress> cc = getInternetAddressesFromProperty(props, CommandContext.PARAM(4));
		if (cc == null) {
			cc = getInternetAddressesFromSlot(stclContext, Slot.CC, self);
		}
		for (InternetAddress addr : cc) {
			smtp_message.addRecipient(Message.RecipientType.CC, addr);
		}

		// sets bcc
		try {
			List<InternetAddress> bcc = getInternetAddressesFromProperty(props, CommandContext.PARAM(6));
			if (bcc == null)
				bcc = getInternetAddressesFromSlot(stclContext, Slot.BCC, self);
			for (InternetAddress addr : bcc) {
				smtp_message.addRecipient(Message.RecipientType.BCC, addr);
			}
		} catch (Exception e) {
		}

		// GDO NOTIFICATION
		// mimeMsg.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS |
		// SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_DELAY);
		// mimeMsg.setReturnOption(SMTPMessage.RETURN_FULL);

		// sets content
		String content = props.getProperty(CommandContext.PARAM(5));
		if (content == null) {
			content = getContent(stclContext, props, self);
		}
		content = content.replaceAll("[&][#]172;", "&#160;");
		content = self.format(stclContext, content);

		// add mime attached resources
		StencilIterator<StclContext, PStcl> iter = getStencils(stclContext, Slot.ATTACHMENTS, self);
		if (iter.hasNext()) {
			Multipart multiPart = new MimeMultipart();
			smtp_message.setContent(multiPart);

			// create multipart message
			MimeBodyPart body = new MimeBodyPart();
			body.setHeader("Content-Transfer-Encoding", "quoted-printable");
			body.setHeader("charset", "utf-8");
			body.setText(content, "utf-8", "html");
			body.addHeaderLine("Content-Type: text/html; charset=\"utf-8\"");
			body.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
			multiPart.addBodyPart(body);

			// adds all attachments
			while (iter.hasNext()) {

				// gets attachment as a data source
				PStcl att = iter.next();
				if (!(att.getStencil(stclContext) instanceof DataSource))
					continue;
				DataSource source = (DataSource) att.getStencil(stclContext);

				// creates attachement
				MimeBodyPart filePart = new MimeBodyPart();
				multiPart.addBodyPart(filePart);

				// set attachement name
				String name = att.getExpandedString(stclContext, AttachmentStcl.Slot.NAME, "");
				filePart.setFileName(name);

				// set attachement content
				int type = att.getInt(stclContext, AttachmentStcl.Slot.TYPE, 0);
				switch (type) {
				case 1:
					filePart.setDataHandler(new DataHandler(source));
					break;
				case 2:
					filePart.setDataHandler(new DataHandler(source));
					String id = String.format("<%s>", filePart.getFileName());
					filePart.setHeader("Content-ID", id);
					break;
				default:
					if (getLog().isWarnEnabled()) {
						String msg = String.format("undefined type %s for addAttachment", type);
						getLog().warn(stclContext, msg);
					}
				}

				/*
				 * MimeMultipart pp = new MimeMultipart("alternative"); MimeBodyPart pdf
				 * = new MimeBodyPart(); pdf.setContent(pdf_file, "application/pdf");
				 * pp.addBodyPart(pdf);
				 */

				// Fetch the image and associate to part
				/*
				 * BodyPart messageBodyPart = new MimeBodyPart(); DataSource fds = new
				 * FileDataSource(file); messageBodyPart.setDataHandler(new
				 * DataHandler(fds)); messageBodyPart.setHeader("Content-ID", "<img>");
				 */
			}

			// else simple message
		} else {
			String domain = StringUtils.substringAfterLast(from, "@");
			smtp_message.setHeader("Content-Transfer-Encoding", "quoted-printable");
			smtp_message.setHeader("charset", "utf-8");
			smtp_message.setHeader("Message-ID", "BO@" + domain);
			smtp_message.setText(content, "utf-8", "html");
		}
		return smtp_message;
	}

	private List<InternetAddress> getInternetAddressesFromSlot(StclContext stclContext, String slot, PStcl self) throws Exception {
		List<InternetAddress> addrs = new ArrayList<InternetAddress>();
		StencilIterator<StclContext, PStcl> iter = self.getStencils(stclContext, slot);
		for (PStcl stcl : iter) {
			if (!StencilUtils.isNull(stcl)) {
				IRecipient rec = (IRecipient) stcl.getReleasedStencil(stclContext);
				Result result = rec.getInternetAddress(stclContext, stcl);
				if (result.isSuccess())
					addrs.add((InternetAddress) result.getSuccessValue());
			}
		}
		return addrs;
	}

	// prop may be empty to have no address (null take from slot)
	private List<InternetAddress> getInternetAddressesFromProperty(Properties props, String key) throws AddressException {
		String prop = props.getProperty(key);
		if (prop != null) {
			List<InternetAddress> addrs = new ArrayList<InternetAddress>();
			for (String str : StringHelper.splitShortString(prop, PathUtils.MULTI)) {
				addrs.add(new InternetAddress(str));
			}
			return addrs;
		}
		return null;
	}

	/**
	 * Sends the message thru the mail transport session.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param email
	 *          the mime message to be sent.
	 * @param to
	 *          the to address.
	 * @return the sending status.
	 */
	protected Result sendMessageTo(StclContext stclContext, Session session, MimeMessage email, InternetAddress to, PStcl mail) throws MessagingException {
		try {

			// sets to recipient
			email.setRecipient(Message.RecipientType.TO, to);

			// sends the message
			if (SENDING) {
				Transport.send(email);
			}
			logWarn(stclContext, "message sent to %s", to.toString());

			return Result.success();
		} catch (Exception e) {
			String msg = logWarn(stclContext, "Error when sending message to %s : %s", to, e);
			return Result.error(msg);
		}
	}

	private class MultiSendThread extends _CommandThread {

		private Session _session;
		private Properties _props;
		private StencilIterator<StclContext, PStcl> _to;

		public MultiSendThread(CommandContext<StclContext, PStcl> cmdContext, Session session, Properties props, StencilIterator<StclContext, PStcl> to, PStcl self) {
			super(cmdContext, self);
			this._session = session;
			this._props = props;
			this._to = to;
		}

		@Override
		public void run() {
			CommandContext<StclContext, PStcl> cmdContext = getCommandContext();
			StclContext stclContext = getStencilContext();
			PStcl mail = getReference();
			try {
				logWarn(stclContext, "start sending emailing");

				// beforeFirst call
				Result result = beforeFirst(cmdContext, mail, mail);
				if (result.isNotSuccess()) {
					logWarn(stclContext, "error in before first; %s", mail);
				}

				// gets recipients from TO slot
				while (this._to.hasNext()) {
					PStcl recipient = this._to.next();

					// checks recipient address is valid
					if (StencilUtils.isNull(recipient)) {
						logWarn(stclContext, "No recipient for email %s", mail);
						continue;
					}
					String address = recipient.getString(stclContext, RecipientStcl.Slot.ADDRESS, "");
					if (StringUtils.isBlank(address)) {
						String msg = String.format("Empty address");
						afterError(cmdContext, mail, recipient, msg, mail);
						continue;
					}
					InternetAddress internetAddress;
					try {
						internetAddress = new InternetAddress(address);
					} catch (AddressException e) {
						String msg = String.format("Wrong address %s", address);
						afterError(cmdContext, mail, recipient, msg, mail);
						continue;
					}

					try {

						// sets new to recipient
						mail.clearSlot(stclContext, Slot.TO);
						mail.plug(stclContext, recipient, Slot.TO);

						// performs before sending action
						result = beforeSend(cmdContext, mail, recipient, mail);
						if (result.isNotSuccess()) {
							logWarn(stclContext, "no sending email %s with to address %s because beforeSend refused it", mail, address);
							continue;
						}

						// gets mime message with content
						MimeMessage mimeMsg = getMimeMessageWithoutTo(stclContext, this._session, this._props, mail);
						if (mimeMsg == null) {
							logWarn(stclContext, "cannot get mime message for email %s with to address %s", mail, address);
							continue;
						}

						// sends message
						result = sendMessageTo(stclContext, this._session, mimeMsg, internetAddress, mail);

						// performs after or error sending action
						if (result.isSuccess()) {
							afterSend(cmdContext, mail, recipient, mail);
						} else {
							afterError(cmdContext, mail, recipient, result.getMessage(), mail);
						}

						// waits 3 sec before next send
						Thread.sleep(getWaitingTime(stclContext, mail));

					} catch (Exception e) {
						afterError(cmdContext, mail, recipient, e.toString(), mail);
					}
				}

				// afterLast call
				result = afterLast(cmdContext, mail, mail);
				if (result.isNotSuccess()) {
					logWarn(stclContext, "error in after last; %s", mail);
				}

				logWarn(stclContext, "stop sending emailing");
			} catch (Exception e) {
				logError(stclContext, "error in emailing : %s", e);
				e.printStackTrace();
				afterLast(cmdContext, mail, mail);
			}
		}
	}

	//
	// LOG PART
	//

	private static final StencilLog LOG = new StencilLog(MailStcl.class);

	@Override
	protected StencilLog getLog() {
		return LOG;
	}

}