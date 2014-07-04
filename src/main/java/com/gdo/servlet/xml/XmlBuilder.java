package com.gdo.servlet.xml;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gdo.servlet.RpcArgs;
import com.gdo.servlet.RpcWrapper;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlStringWriter;

public class XmlBuilder {

	public String stencils(StclContext stclContext, RpcArgs args, StencilIterator<StclContext, PStcl> iter) throws IOException {
		XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));

		writer.startElement("result");
		addStatus(writer, iter.getStatus());
		writer.startElement("stencils");
		writer.writeAttribute("size", iter.size());
		for (PStcl stcl : iter) {
			if (StencilUtils.isNotNull(stcl)) {
				writer.startElement("stencil");
				writer.writeAttribute("uid", stcl.getUId(stclContext));
				writer.writeAttribute("key", stcl.getKey().toString());
				args.writeAttributes(stclContext, stcl, true, writer);
				writer.endElement("stencil");
			} else {
				List<PStcl> list = stcl.getStencilOtherPluggedReferences(stclContext);
				for (PStcl s : list) {
					RpcWrapper.logWarn(stclContext, "reference %s (%s) for path %s", s.toString(), s.getContainingSlot(), args.getPath());
				}
			}
		}
		writer.endElement("stencils");
		writer.endElement("result");

		return writer.getString();
	}

	public String get(StclContext stclContext, RpcArgs args, PStcl stcl, String value, String type, Result result) throws IOException {
		XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
		writer.startElement("result");
		args.writeAttributes(stclContext, stcl, true, writer);
		addStatus(writer, result);
		writer.startElement("value");
		if (StringUtils.isNotEmpty(type)) {
			writer.writeAttribute("type", type);
		}
		if (StringUtils.isNotEmpty(value)) {
			writer.writeCDATAElement("data", value);
		}
		writer.endElement("value");
		writer.endElement("result");

		return writer.getString();
	}

	/**
	 * Adds status info to XML answer.
	 * 
	 * @param writer
	 *          the XML answer writer.
	 * @param status
	 *          the status to add.
	 */
	private void addStatus(XmlStringWriter writer, Result status) throws IOException {

		// if status null (on iterator or stencil)
		if (status == null) {
			writer.startElement("status");
			writer.writeAttribute("level", Byte.toString(CommandStatus.SUCCESS));
			writer.endElement("status");
			return;
		}

		// writes status
		writer.startElement("status");
		writer.writeAttribute("level", Byte.toString(status.getStatus()));
		for (CommandStatus.ResultInfo comp : status.getInfos(CommandStatus.SUCCESS)) {
			if (comp != null) {
				writer.startElement("ok");
				writer.writeAttribute("cmdName", comp.getPrefix());
				writer.writeAttribute("index", comp.getIndex());
				if (comp.getValue() != null) {
					writer.writeCDATA(comp.getValue().toString());
				}
				writer.endElement("ok");
			}
		}
		for (CommandStatus.ResultInfo comp : status.getInfos(CommandStatus.WARNING)) {
			if (comp != null) {
				writer.startElement("warn");
				writer.writeAttribute("cmdName", comp.getPrefix());
				writer.writeAttribute("index", comp.getIndex());
				if (comp.getValue() != null) {
					writer.writeCDATA(comp.getValue().toString());
				}
				writer.endElement("warn");
			}
		}
		for (CommandStatus.ResultInfo comp : status.getInfos(CommandStatus.ERROR)) {
			if (comp != null) {
				writer.startElement("error");
				writer.writeAttribute("cmdName", comp.getPrefix());
				writer.writeAttribute("index", comp.getIndex());
				if (comp.getValue() != null) {
					writer.writeCDATA(comp.getValue().toString());
				}
				writer.endElement("error");
			}
		}
		writer.endElement("status");
	}

}
