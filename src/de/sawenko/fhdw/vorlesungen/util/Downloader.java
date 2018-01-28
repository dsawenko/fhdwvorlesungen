package de.sawenko.fhdw.vorlesungen.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.text.ICalReader;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.util.DateTimeComponents;
import de.sawenko.fhdw.vorlesungen.model.Module;
import de.sawenko.fhdw.vorlesungen.model.Vorlesung;

public class Downloader {
	public static List<VEvent> events = new ArrayList<>();
	public static List<Module> modules = new ArrayList<>();
	public static List<Vorlesung> vorlesungen = new ArrayList<>();

	/**
	 * URL prefix to download ics from FHDW Intranet.
	 */
	private static final String URL_PREFIX = "http://intranet.bib.de/ical/";

	/**
	 * Download calendar ics file for a specific course, and return a list of the
	 * events.
	 *
	 * @param course
	 *            the course to get events for, example: ifbw415a
	 * @return list of events for that course
	 */
	public static void getEventsFromFHDW(String course, Calendar calendar) {
		InputStreamReader inputStream = null;
		ICalReader reader = null;
		ArrayList<VEvent> events = new ArrayList<>();
		ArrayList<VEvent> resultEvents = new ArrayList<>();
		Downloader.events.clear();
		Downloader.modules.clear();
		Downloader.vorlesungen.clear();
		events.clear();
		resultEvents.clear();
		try {
			URL url = new URL(URL_PREFIX + course + ".ics");
			inputStream = new InputStreamReader(url.openStream(), Charset.forName("US-ASCII"));

			reader = new ICalReader(inputStream);
			ICalendar ical;

			while ((ical = reader.readNext()) != null) {
				events.addAll(ical.getEvents());
			}

		} catch (IOException e) {
			events.clear();
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(reader);
		}

		for (VEvent event : events) {
			DateTimeComponents dateTimeComponents = event.getDateStart().getValue().getRawComponents();
			int eYear = dateTimeComponents.getYear();
			int eMonth = dateTimeComponents.getMonth();
			int eDate = dateTimeComponents.getDate();

			// month ist von 0-11 und eMonth von 1-12 ... meh
			if (eYear == calendar.get(Calendar.YEAR) && eMonth == (calendar.get(Calendar.MONTH) + 1)
					&& eDate == calendar.get(Calendar.DATE)) {
				resultEvents.add(event);
			}
		}
		Downloader.events = resultEvents;

	}

	public static void createAllModules() {

		for (VEvent event : events) {
			String summary = event.getSummary().getValue();
			if (summary == null) {
				return;
			}
			String[] parts = summary.split(" ");
			if (summary.contains("Prüfungsphase")) {
				getOrCreateModule(summary, "");
			} else if (parts.length != 3) {
				getOrCreateModule(summary, "");
			} else {
				getOrCreateModule(parts[0], parts[1]);
			}

			getFullInfo(event);
		}

	}

	private static Module getOrCreateModule(String abbreviation, String abbreviationProfessor) {
		for (Module m : modules) {
			if (m.getAbbreviation().contains(abbreviation))
				return m;
		}

		modules.add(new Module(abbreviation, abbreviationProfessor));
		return modules.get(modules.size() - 1);
	}

	private static void getFullInfo(VEvent event) {
		String summary = event.getSummary().getValue();
		if (summary == null) {
			return;
		}

		String[] parts = summary.split(" ");
		DateStart dateStart = event.getDateStart();
		DateEnd dateEnd = event.getDateEnd();
		if (summary.contains("Prüfungsphase")) {
			vorlesungen.add(new Vorlesung(dateStart, dateEnd, getOrCreateModule(summary, ""), "", summary));

		} else if (parts.length != 3) {
			vorlesungen.add(new Vorlesung(dateStart, dateEnd, getOrCreateModule(summary, ""), "", summary));
		} else {
			String room = parts[2].substring(1);
			vorlesungen.add(new Vorlesung(dateStart, dateEnd, getOrCreateModule(parts[0], parts[1]), room, summary));
		}

	}
}
