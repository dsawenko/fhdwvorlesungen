package de.sawenko.fhdw.vorlesungen.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.text.ICalReader;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.util.DateTimeComponents;
import de.sawenko.fhdw.vorlesungen.main.VorlesungenSpeechlet;
import de.sawenko.fhdw.vorlesungen.model.Lecturer;
import de.sawenko.fhdw.vorlesungen.model.Module;
import de.sawenko.fhdw.vorlesungen.model.Vorlesung;
import de.sawenko.fhdw.vorlesungen.storage.VorlesungenDao;

public class Downloader {
	private static List<VEvent> events = new ArrayList<>();
	private static List<Vorlesung> vorlesungen = new ArrayList<>();
	

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
	public static void getEventsFromFHDW(String course, Calendar calendar, VorlesungenDao dao) {
		InputStreamReader inputStream = null;
		ICalReader reader = null;
		ArrayList<VEvent> events = new ArrayList<>();
		ArrayList<VEvent> resultEvents = new ArrayList<>();
		Downloader.events.clear();
		Downloader.vorlesungen.clear();
		events.clear();
		resultEvents.clear();
		try {
			URL url = new URL(URL_PREFIX + course.toLowerCase() + ".ics");
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
		workWithEvents(dao);

	}

	private static void workWithEvents(VorlesungenDao dao) {

		for (VEvent event : events) {
			String summary = event.getSummary().getValue();
			if (summary == null) {
				return;
			}

			String[] parts = summary.split(" ");
			
			if (parts.length == 3) {
				DateStart dateStart = event.getDateStart();
				DateEnd dateEnd = event.getDateEnd();
				Module module = dao.getModule(parts[0]);
				Lecturer lecturer = dao.getLecturer(parts[1]);
				String room = parts[2].substring(1);
				
				//if (module != null && lecturer != null)
					vorlesungen.add(new Vorlesung(dateStart, dateEnd, module, lecturer, room, summary));
			} else if (parts.length == 4) {
				DateStart dateStart = event.getDateStart();
				DateEnd dateEnd = event.getDateEnd();
				Module module = dao.getModule(parts[0] +" " + parts[1]);
				Lecturer lecturer = dao.getLecturer(parts[2]);
				String room = parts[3].substring(1);
				
				//if (module != null && lecturer != null)
					vorlesungen.add(new Vorlesung(dateStart, dateEnd, module, lecturer, room, summary));
			}
			
			
		}

		sortVorlesungen();
	}

	private static void sortVorlesungen() {
        List<Vorlesung> sorted = new ArrayList<>();
        Vorlesung first;

        while (vorlesungen.size() != 0) {
            first = vorlesungen.get(0);
            for (Vorlesung v : vorlesungen) {
                if (v.isBefore(first)) {
                    first = v;
                }
            }
            vorlesungen.remove(first);
            sorted.add(first);

        }
        vorlesungen = sorted;
    }
	
	public static List<Vorlesung> getVorlesungen() {
		return vorlesungen;
	}

}
