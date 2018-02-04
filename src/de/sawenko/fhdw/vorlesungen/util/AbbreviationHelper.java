package de.sawenko.fhdw.vorlesungen.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.sawenko.fhdw.vorlesungen.model.Lecturer;
import de.sawenko.fhdw.vorlesungen.model.Module;

public class AbbreviationHelper {



	private final Map<String, Lecturer> lecturerMap;
	private final Map<String, Module> moduleMap;

	public AbbreviationHelper() {
		ClassLoader classLoader = getClass().getClassLoader();
		lecturerMap = getLecturerMapFromCSV(classLoader.getResource("abbreviation/FHDW_Mitarbeiter.csv").getPath());
		moduleMap = getModuleMapFromCSV(classLoader.getResource("abbreviation/FHDW_Module.csv").getPath());
	}
	
	/**
	 * Gibt für ein Dozentenkürzel den entsprechenden Dozenten zurück.
	 * @param abbreviation
	 * @return
	 */
	public Lecturer getLecturer(String abbreviation) {
		return lecturerMap.get(abbreviation);
	}

	/**
	 * Gibt für ein Modulkürzel das entsprechende Modul zurück.
	 * @param abbreviation
	 * @return
	 */
	public Module getModule(String abbreviation) {
		return moduleMap.get(abbreviation);
	}

	private Map<String, Lecturer> getLecturerMapFromCSV(final String filePath) {
		try {
			Stream<String> lines = Files.lines(Paths.get(filePath));
			Map<String, Lecturer> resultMap = lines.map(line -> line.split(";")).collect(Collectors
					.toMap(line -> line[0], line -> new Lecturer(line[0], line[1], line[2], line[3], line[4])));

			lines.close();

			return resultMap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	private Map<String, Module> getModuleMapFromCSV(final String filePath) {
		try {
			Stream<String> lines = Files.lines(Paths.get(filePath));
			Map<String, Module> resultMap = lines.map(line -> line.split(";"))
					.collect(Collectors.toMap(line -> line[0], line -> new Module(line[0], line[1])));

			lines.close();

			return resultMap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
}
