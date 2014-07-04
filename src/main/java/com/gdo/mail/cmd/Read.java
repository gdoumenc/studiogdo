/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;

import com.gdo.mail.model.RecipientStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

/**
 * @author gdo <param0>Title</param0> <param1>From</param1> <param2>To</param2>
 *         <param3>CC</param3> <param4>Content</param4>
 */
public class Read extends AtomicActionStcl {

	public Read(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// clear to slot
		PSlot<StclContext, PStcl> to = target.getSlot(stclContext, "To");
		to.getSlot().unplugAll(stclContext, to);

		String file = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(file))
			return error(cmdContext, self, 1, "no name defined to rename file");

		// read all lines
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			int i = 0;
			while ((line = in.readLine()) != null) {
				try {
					i++;
					String[] adds = line.split("\t");
					int j = 1;
					while (j < adds.length && StringUtils.isEmpty(adds[j]))
						j++;
					String add = adds[j];
					InternetAddress a = new InternetAddress(add);
					target.newPStencil(stclContext, "To", Key.NO_KEY, RecipientStcl.class, a);
					getLog().error(stclContext, "done " + i);
				} catch (Exception e) {
					getLog().error(stclContext, "error on line " + i);
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success(cmdContext, self);
	}
}