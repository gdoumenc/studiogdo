/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;

import com.gdo.project.slot.StringProxySlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.iterator.ListIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.sun.mail.imap.IMAPMessage;

public class IMAPMailStcl extends MailStcl {

    private IMAPMessage _msg;
    private List<Writer> _list = new ArrayList<Writer>();

    public interface Slot extends MailStcl.Slot {
        String SENT_DATE = "SentDate";
    }

    public interface Command extends MailStcl.Command {
        String DELETE = "Delete";
    }

    public IMAPMailStcl(StclContext stclContext) {
        super(stclContext);
    }

    public IMAPMailStcl(StclContext stclContext, IMAPMessage msg) {
        super(stclContext);
        _msg = msg;
        try {
            // String enc = msg.getEncoding();

            String subject = msg.getSubject();
            new TitleSlot(stclContext, subject);

            Address[] adds = msg.getFrom();
            new FromSlot(stclContext, adds);

            Address sender = msg.getSender();
            if (sender != null)
                new FromNameSlot(stclContext, sender.toString());
            else
                new FromNameSlot(stclContext, "");

            String format = "d-M-yy h:m";
            DateFormat dateFormat = new SimpleDateFormat(format);
            String date = dateFormat.format(msg.getSentDate());
            new DateSlot(stclContext, date);

            String type = msg.getContentType();
            new ContentTypeSlot(stclContext, type);

            String content = null;
            if (type.startsWith("multipart")) {
                boolean found = false;
                MimeMultipart parts = (MimeMultipart) msg.getContent();
                for (int i = 0; i < parts.getCount(); i++) {
                    BodyPart body = parts.getBodyPart(i);
                    String t = body.getContentType();
                    if (t.indexOf("text/") != -1) {
                        found = true;
                        StringWriter w = new StringWriter();
                        IOUtils.copy(body.getInputStream(), w);
                        content = w.toString();
                    }
                }
                if (!found)
                    content = "";
            } else {
                content = msg.getContent().toString();
            }
            content = content.replaceAll("\r\n\r\n\r\n", "<br/>");
            content = content.replaceAll("\r\n", " ");
            content = content.replaceAll("\\\\", "");
            // content = new String(content.getBytes(), "utf-8");
            new ContentSlot(stclContext, content);

            // attachement
            if (type.startsWith("multipart")) {
                MimeMultipart parts = (MimeMultipart) msg.getContent();
                for (int i = 0; i < parts.getCount(); i++) {
                    BodyPart body = parts.getBodyPart(i);
                    String t = body.getContentType();
                    if (t.indexOf("text/") != -1) {
                        continue;
                    }
                    /*
                     * if (t.indexOf("image/") != -1) { StringWriter w = new
                     * StringWriter(); IOUtils.copy(body.getInputStream(), w);
                     * _list.add(w); continue; }
                     */
                }
            }
        } catch (Exception e) {
            getLog().warn(stclContext, e);
        }
    }

    @Override
    public void afterCompleted(StclContext stclContext, PStcl self) {
        super.afterCompleted(stclContext, self);
        for (Writer w : _list) {
            self(stclContext, null).newPStencil(stclContext, Slot.ATTACHMENTS, new Key<Integer>(1), IMAPResourceStcl.class, w);
        }
    }

    public void delete() throws MessagingException {
        Folder folder = _msg.getFolder();
        folder.open(Folder.READ_WRITE);
        int number = _msg.getMessageNumber();
        IMAPMessage msg = (IMAPMessage) folder.getMessage(number);
        msg.setFlag(Flags.Flag.DELETED, true);
        folder.close(true);
        folder.getStore().close();
    }

    private class TitleSlot extends StringProxySlot {
        public TitleSlot(StclContext stclContext, String subject) {
            super(stclContext, IMAPMailStcl.this, Slot.TITLE, subject);
        }
    }

    private class FromSlot extends MultiCalculatedSlot<StclContext, PStcl> {
        private Address[] _adds;
        private List<PStcl> _from;

        public FromSlot(StclContext stclContext, Address[] adds) {
            super(stclContext, IMAPMailStcl.this, Slot.FROM, PSlot.ANY);
            _adds = adds;
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            if (_from == null) {
                StclFactory factory = (StclFactory) stclContext.getStencilFactory();
                _from = new ArrayList<PStcl>();
                if (_adds != null) {
                    for (Address add : _adds) {
                        PStcl stcl = factory.createPStencil(stclContext, self, Key.NO_KEY, FromRecipientStcl.class, add.toString());
                        _from.add(stcl);
                    }
                }
            }
            return new ListIterator<StclContext, PStcl>(_from);
        }
    }

    private class FromNameSlot extends StringProxySlot {
        public FromNameSlot(StclContext stclContext, String from) {
            super(stclContext, IMAPMailStcl.this, Slot.FROM_NAME, from);
        }
    }

    private class DateSlot extends StringProxySlot {
        public DateSlot(StclContext stclContext, String date) {
            super(stclContext, IMAPMailStcl.this, Slot.SENT_DATE, date);
        }
    }

    private class ContentTypeSlot extends StringProxySlot {
        public ContentTypeSlot(StclContext stclContext, String type) {
            super(stclContext, IMAPMailStcl.this, Slot.CONTENT_TYPE, type);
        }
    }

    private class ContentSlot extends StringProxySlot {
        public ContentSlot(StclContext stclContext, String content) {
            super(stclContext, IMAPMailStcl.this, Slot.CONTENT, content);
        }
    }

    private class FromRecipientStcl extends Stcl {
        public FromRecipientStcl(StclContext stclContext, String address) {
            super(stclContext);
            new AddressSlot(stclContext, address);
        }

        private class AddressSlot extends StringProxySlot {
            public AddressSlot(StclContext stclContext, String address) {
                super(stclContext, FromRecipientStcl.this, RecipientStcl.Slot.ADDRESS, address);
            }
        }
    }
}