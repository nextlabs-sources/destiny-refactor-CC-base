package com.nextlabs.pf.destiny.importexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

public class ImportFileParser {
	private static final String MAPPING_XML_FILENAME = "/com/nextlabs/pf/destiny/importexport/mapping/mapping.xml";
	
	/**
	 * This class parses takes in an importFile and returns a parsed ExportFile
	 * object with the contents of the importFile.
	 * 
	 * @param importFile,
	 *            the file to be imported
	 * @return parsedFile, the (ExportFile) object containing collections of the
	 *         import data.
	 * @throws IOException
	 * @throws MappingException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	static public ExportFile parseFile(File importFile) throws ImportException {
		try {
			Mapping map = new Mapping();
			
			InputStream importFileIs = new ImportFileParser().getClass().getResourceAsStream(MAPPING_XML_FILENAME);
			map.loadMapping(new InputSource(importFileIs));

			FileInputStream xmlInputStream = new FileInputStream(importFile);
			Reader xmlReader = new InputStreamReader(xmlInputStream, "UTF-8");
			
			Unmarshaller unmar = new Unmarshaller(ExportFile.class);
			unmar.setIgnoreExtraElements(true);
			unmar.setMapping(map);
			ExportFile parsedFile = (ExportFile) unmar.unmarshal(xmlReader);
			
			return parsedFile;
		} catch (MappingException e) {
			throw new ImportException(e);
		} catch (FileNotFoundException e) {
			throw new ImportException(e);
		} catch (IOException e) {
			throw new ImportException(e);
		} catch (ValidationException e) {
			throw new ImportException(e);
		} catch (MarshalException e) {
			throw new ImportException(e);
		}
	}
}