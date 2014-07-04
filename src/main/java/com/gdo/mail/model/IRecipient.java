/**
 * Copyright GDO - 2005
 */
package com.gdo.mail.model;

import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public interface IRecipient {

	Result getInternetAddress(StclContext stclContext, PStcl self);

}
