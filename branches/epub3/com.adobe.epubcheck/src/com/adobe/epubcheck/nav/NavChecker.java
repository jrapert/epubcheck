/*
 * Copyright (c) 2011 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.nav;

import java.io.IOException;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.ops.OPSHandler30;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class NavChecker implements ContentChecker, DocumentValidator {

	static XMLValidator navValidator_30_RNC = new XMLValidator(
			"schema/30/epub-nav-30.rnc");

	static XMLValidator navValidator_30_ISOSCH = new XMLValidator(
			"schema/30/epub-nav-30-PREP.sch");

	static XMLValidator xhtmlValidator_30_ISOSCH = new XMLValidator(
			"schema/30/epub-xhtml-30-PREP.sch");

	OCFPackage ocf;

	Report report;

	String path;

	XRefChecker xrefChecker;

	String properties;

	String mimeType;

	GenericResourceProvider resourceProvider;

	public NavChecker(GenericResourceProvider resourceProvider, Report report,
			String path, String mimeType, EPUBVersion version) {
		if (version == EPUBVersion.VERSION_2)
			report.error(path, 0, 0, Messages.NAV_NOT_SUPPORTED);
		this.report = report;
		this.path = path;
		this.resourceProvider = resourceProvider;
		this.properties = "singleFileValidation";
		this.mimeType = mimeType;
	}

	public NavChecker(OCFPackage ocf, Report report, String path,
			String mimeType, String properties, EPUBVersion version) {
		if (version == EPUBVersion.VERSION_2)
			report.error(path, 0, 0, Messages.NAV_NOT_SUPPORTED);
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.resourceProvider = ocf;
		this.properties = properties;
		this.mimeType = mimeType;
	}

	public void runChecks() {
		if (!ocf.hasEntry(path))
			report.error(null, 0, 0, String.format(Messages.MISSING_FILE, path));
		else if (!ocf.canDecrypt(path))
			report.error(null, 0, 0, "Nav file " + path
					+ " cannot be decrypted");
		else {
			validate();
		}
	}

	public boolean validate() {
		int errors = report.getErrorCount();
		int warnings = report.getWarningCount();
		try {
			XMLParser navParser = new XMLParser(
					resourceProvider.getInputStream(path), path,
					"application/xhtml+xml", report);

			XMLHandler navHandler = new OPSHandler30(path, mimeType,
					properties, xrefChecker, navParser, report);
			navParser.addXMLHandler(navHandler);
			navParser.addValidator(navValidator_30_RNC);
			navParser.addValidator(xhtmlValidator_30_ISOSCH);
			navParser.addValidator(navValidator_30_ISOSCH);
			navParser.process();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return errors == report.getErrorCount()
				&& warnings == report.getWarningCount();
	}
}
