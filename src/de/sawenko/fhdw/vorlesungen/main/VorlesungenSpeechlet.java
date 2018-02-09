/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package de.sawenko.fhdw.vorlesungen.main;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.speechlet.services.DirectiveEnvelope;
import com.amazon.speech.speechlet.services.DirectiveEnvelopeHeader;
import com.amazon.speech.speechlet.services.DirectiveService;
import com.amazon.speech.speechlet.services.SpeakDirective;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import de.sawenko.fhdw.vorlesungen.model.Vorlesung;
import de.sawenko.fhdw.vorlesungen.storage.VorlesungenDao;
import de.sawenko.fhdw.vorlesungen.storage.VorlesungenDynamoDbClient;
import de.sawenko.fhdw.vorlesungen.util.Downloader;

/**
 * Build Skript: mvn assembly:assembly -DdescriptorId=jar-with-dependencies
 * package
 * 
 * @author Daniel Sawenko
 *
 */
public class VorlesungenSpeechlet implements SpeechletV2 {
	private static final Logger log = LoggerFactory.getLogger(VorlesungenSpeechlet.class);

	/**
	 * Constant defining number of events to be read at one time.
	 */
	private static final int PAGINATION_SIZE = 3;

	/**
	 * Length of the delimiter between individual events.
	 */
	private static final int DELIMITER_SIZE = 2;

	/**
	 * Constant defining session attribute key for the event index.
	 */
	private static final String SESSION_INDEX = "index";

	/**
	 * Constant defining session attribute key for the event text key for date of
	 * events.
	 */
	private static final String SESSION_TEXT = "text";

	/**
	 * Constant defining session attribute key for the intent slot key for the date
	 * of events.
	 */
	private static final String SLOT_DAY = "day";

	/**
	 * Array of month names.
	 */
	private static final String[] MONTH_NAMES = { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August",
			"September", "Oktober", "November", "Dezember" };

	/**
	 * Service to send progressive response directives.
	 */
	private DirectiveService directiveService;

	private AmazonDynamoDBClient amazonDynamoDBClient;
	private VorlesungenDao dao;

	/**
	 * Constructs an instance of {@link VorlesungenSpeechlet}.
	 *
	 * @param directiveService
	 *            implementation of directive service
	 */
	public VorlesungenSpeechlet(DirectiveService directiveService) {
		this.directiveService = directiveService;
	}

	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
		SessionStartedRequest request = requestEnvelope.getRequest();
		Session session = requestEnvelope.getSession();

		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		// any initialization logic goes here

		if (amazonDynamoDBClient == null) {
			amazonDynamoDBClient = new AmazonDynamoDBClient().withRegion(Regions.EU_WEST_1);
		}

		dao = new VorlesungenDao(new VorlesungenDynamoDbClient(amazonDynamoDBClient));
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		LaunchRequest request = requestEnvelope.getRequest();
		Session session = requestEnvelope.getSession();

		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		return getWelcomeResponse(session);
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		log.info("onIntent requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
				requestEnvelope.getSession().getSessionId());

		String intentName = requestEnvelope.getRequest().getIntent().getName();
		Session session = requestEnvelope.getSession();
		String course = dao.getCourse(session);

		if ("VorlesungIntent".equals(intentName)) {
			if (course == null || course.equals(""))
				return createNewUser(session);
			else
				return handleVorlesungenRequest(requestEnvelope, course);
		} else if ("AMAZON.HelpIntent".equals(intentName)) {
			// Create the plain text output.
			String speechOutput = "Ich kann dir deine Vorlesungen für einen bestimmten Tag nennen. "
					+ " Zum Beispiel könntest du mich fragen, ob du morgen eine Vorlesung hast."
					+ "Sag einfach: Frag Fuchs, welche Vorlesungen ich morgen habe.";

			String repromptText = "Für welchen Tag möchtest du deine Vorlesungen erfahren?";

			return newAskResponse(speechOutput, false, repromptText, false);
		} else if ("AMAZON.StopIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Tschüss");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else if ("AMAZON.CancelIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Tschüss");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else {
			String outputSpeech = "Entschuldige, das habe ich nicht verstanden.";
			String repromptText = "Für welchen Tag möchtest du deine Vorlesungen erfahren?";

			return newAskResponse(outputSpeech, true, repromptText, true);
		}
	}

	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
		SessionEndedRequest request = requestEnvelope.getRequest();
		Session session = requestEnvelope.getSession();

		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		// any session cleanup logic would go here
	}

	/**
	 * Function to handle the onLaunch skill behavior.
	 *
	 * @return SpeechletResponse object with voice/card response to return to the
	 *         user
	 */
	private SpeechletResponse getWelcomeResponse(Session session) {

		String course = dao.getCourse(session);

		if (course == null || course.equals(""))
			return createNewUser(session);

		String speechOutput = "Willkommen beim FHDW Fuchs! Wie kann ich dir weiterhelfen?";
		// If the user either does not reply to the welcome message or says something
		// that is not
		// understood, they will be prompted again with this text.
		String repromptText = "Ich kann dir deine Vorlesungen für einen bestimmten Tag nennen. "
				+ " Zum Beispiel könntest du mich fragen, ob du morgen eine Vorlesung hast."
				+ "Sag einfach: Frag Fuchs, welche Vorlesungen ich morgen habe.";

		return newAskResponse(speechOutput, false, repromptText, false);
	}

	private SpeechletResponse createNewUser(Session session) {
		// Neuen User anlegen
		String speechOutput;
		String accessCode = String.valueOf(dao.createNewUser(session));
		
		StringBuilder accessCodeOutputBuilder = new StringBuilder();
		for (Character c : accessCode.toCharArray())
			accessCodeOutputBuilder.append(c + "<break time=\"300ms\"/> ");
		String accessCodeOutput = accessCodeOutputBuilder.toString();
		
		speechOutput = "<speak><p>Willkommen beim FHDW Fuchs! "
				+ "Ich kann dir deine Vorlesungen für einen bestimmten Tag nennen. </p>"
				+ "<p>Um deinen Vorlesungsplan abrufen zu können, benötige ich dein Studiengangskürzel. </p>"
				+ "<p>Gehe hierzu auf <prosody volume=\"x-loud\" rate=\"80%\">http://localhost/alexa</prosody> und gebe den folgenden Zugangscode ein:</p>"
				+ "<p><prosody volume=\"x-loud\" rate=\"80%\">" + accessCodeOutput + "</prosody></p>"
				+ "<p>Ich wiederhole: <prosody volume=\"x-loud\" rate=\"80%\">http://localhost/alexa</prosody> und gebe den folgenden Zugangscode ein:</p>"
				+ "<p><prosody volume=\"x-loud\" rate=\"80%\">" + accessCodeOutput + "</prosody></p></speak>"; 
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml(speechOutput);
		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	/**
	 * Function to accept an intent containing a Day slot (date object) and return
	 * the Calendar representation of that slot	 value. If the user provides a date,
	 * then use that, otherwise use today. The date is in server time, not in the
	 * user's time zone. So "today" for the user may actually be tomorrow.
	 *
	 * @param intent
	 *            the intent object containing the day slot
	 * @return the Calendar representation of that date
	 */
	private Calendar getCalendar(Intent intent) {
		Slot daySlot = intent.getSlot(SLOT_DAY);
		Date date;
		Calendar calendar = Calendar.getInstance();
		if (daySlot != null && daySlot.getValue() != null) { // ToDo: Prüfen
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d");
			try {
				date = dateFormat.parse(daySlot.getValue());
			} catch (ParseException e) {
				date = new Date();
			}
		} else {
			date = new Date();
		}
		calendar.setTime(date);
		return calendar;
	}

	/**
	 * Prepares the speech to reply to the user. Obtain events from FHDW for the
	 * date specified by the user (or for today's date, if no date is specified),
	 * and return those events in both speech and SimpleCard format.
	 *
	 * @param requestEnvelope
	 *            the intent request envelope to handle
	 * @return SpeechletResponse object with voice/card response to return to the
	 *         user
	 */
	private SpeechletResponse handleVorlesungenRequest(SpeechletRequestEnvelope<IntentRequest> requestEnvelope,
			String course) {
		IntentRequest request = requestEnvelope.getRequest();
		Session session = requestEnvelope.getSession();
		SystemState systemState = getSystemState(requestEnvelope.getContext());
		String apiEndpoint = systemState.getApiEndpoint();

		Calendar calendar = getCalendar(request.getIntent());
		String month = MONTH_NAMES[calendar.get(Calendar.MONTH)];
		String date = Integer.toString(calendar.get(Calendar.DATE));

		String speechPrefixContent = "<p>Vorlesungen am " + date + ". " + month + "</p> ";
		String cardPrefixContent = "Vorlesungen am " + month + " " + date + ", ";
		String cardTitle = "Vorlesung am " + month + " " + date;

		// Dispatch a progressive response to engage the user while fetching events
		// dispatchProgressiveResponse(request.getRequestId(), "Searching", systemState,
		// apiEndpoint);

		Downloader.getEventsFromFHDW(course, calendar, dao);

		String speechOutput = "";
		if (Downloader.getVorlesungen().isEmpty()) {
			speechOutput = "<p>Am " + date + ". " + month + " hast du keine Vorlesung.</p> ";

			// Create the plain text output
			SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
			outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else {
			StringBuilder speechOutputBuilder = new StringBuilder();
			speechOutputBuilder.append(speechPrefixContent);
			StringBuilder cardOutputBuilder = new StringBuilder();
			cardOutputBuilder.append(cardPrefixContent);
			for (Vorlesung v : Downloader.getVorlesungen()) {
				String summary = v.toString();
				
				speechOutputBuilder.append("<p>");
				speechOutputBuilder.append(summary);
				speechOutputBuilder.append(" von " + v.getTime());
				speechOutputBuilder.append(" bei " + v.getLecturer());
				speechOutputBuilder.append("</p> ");
				cardOutputBuilder.append(summary);
				cardOutputBuilder.append(" von " + v.getTime());
				cardOutputBuilder.append(" bei " + v.getLecturer());
				cardOutputBuilder.append("\n");

			}
			speechOutput = speechOutputBuilder.toString();

			// Create the Simple card content.
			SimpleCard card = new SimpleCard();
			card.setTitle(cardTitle);
			card.setContent(cardOutputBuilder.toString());

			SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
			outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
			SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech);
			response.setCard(card);
			log.debug(response.toString());
			return response;
		}
	}

	/**
	 * Prepares the speech to reply to the user. Obtains the list of events as well
	 * as the current index from the session attributes. After getting the next set
	 * of events, increment the index and store it back in session attributes. This
	 * allows us to obtain new events without making repeated network calls, by
	 * storing values (events, index) during the interaction with the user.
	 *
	 * @param session
	 *            object containing session attributes with events list and index
	 * @return SpeechletResponse object with voice/card response to return to the
	 *         user
	 */
	/*
	 * private SpeechletResponse handleNextEventRequest(Session session) { String
	 * cardTitle = "More events on this day in history"; ArrayList<VEvent> events =
	 * (ArrayList<VEvent>) session.getAttribute(SESSION_TEXT); int index = (Integer)
	 * session.getAttribute(SESSION_INDEX); String speechOutput = ""; String
	 * cardOutput = ""; if (events == null) { speechOutput =
	 * "With History Buff, you can get historical events for any day of the year." +
	 * " For example, you could say today, or August thirtieth." +
	 * " Now, which day do you want?"; } else if (index >= events.size()) {
	 * speechOutput =
	 * "There are no more events for this date. Try another date by saying, " +
	 * " get events for august thirtieth."; } else { StringBuilder
	 * speechOutputBuilder = new StringBuilder(); StringBuilder cardOutputBuilder =
	 * new StringBuilder(); for (int i = 0; i < PAGINATION_SIZE && index <
	 * events.size(); i++) { speechOutputBuilder.append("<p>");
	 * speechOutputBuilder.append(events.get(index).getSummary().getValue());
	 * speechOutputBuilder.append("</p> ");
	 * cardOutputBuilder.append(events.get(index).getSummary().getValue());
	 * cardOutputBuilder.append(" "); index++; } if (index < events.size()) {
	 * speechOutputBuilder.append(" Wanna go deeper in history?");
	 * cardOutputBuilder.append(" Wanna go deeper in history?"); }
	 * session.setAttribute(SESSION_INDEX, index); speechOutput =
	 * speechOutputBuilder.toString(); cardOutput = cardOutputBuilder.toString(); }
	 * String repromptText =
	 * "Do you want to know more about what happened on this date?";
	 * 
	 * // Create the Simple card content. SimpleCard card = new SimpleCard();
	 * card.setTitle(cardTitle); card.setContent(cardOutput.toString());
	 * 
	 * SpeechletResponse response = newAskResponse("<speak>" + speechOutput +
	 * "</speak>", true, repromptText, false); response.setCard(card); return
	 * response; }
	 */

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 *
	 * @param stringOutput
	 *            the output to be spoken
	 * @param isOutputSsml
	 *            whether the output text is of type SSML
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is misunderstood.
	 * @param isRepromptSsml
	 *            whether the reprompt text is of type SSML
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml, String repromptText,
			boolean isRepromptSsml) {
		OutputSpeech outputSpeech, repromptOutputSpeech;
		if (isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
		}

		if (isRepromptSsml) {
			repromptOutputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
		} else {
			repromptOutputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
		}
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

	/**
	 * Dispatches a progressive response.
	 *
	 * @param requestId
	 *            the unique request identifier
	 * @param text
	 *            the text of the progressive response to send
	 * @param systemState
	 *            the SystemState object
	 * @param apiEndpoint
	 *            the Alexa API endpoint
	 */
	private void dispatchProgressiveResponse(String requestId, String text, SystemState systemState,
			String apiEndpoint) {
		DirectiveEnvelopeHeader header = DirectiveEnvelopeHeader.builder().withRequestId(requestId).build();
		SpeakDirective directive = SpeakDirective.builder().withSpeech(text).build();
		DirectiveEnvelope directiveEnvelope = DirectiveEnvelope.builder().withHeader(header).withDirective(directive)
				.build();

		if (systemState.getApiAccessToken() != null && !systemState.getApiAccessToken().isEmpty()) {
			String token = systemState.getApiAccessToken();
			try {
				directiveService.enqueue(directiveEnvelope, apiEndpoint, token);
			} catch (Exception e) {
				log.error("Failed to dispatch a progressive response", e);
			}
		}
	}

	/**
	 * Helper method that retrieves the system state from the request context.
	 * 
	 * @param context
	 *            request context.
	 * @return SystemState the systemState
	 */
	private SystemState getSystemState(Context context) {
		return context.getState(SystemInterface.class, SystemState.class);
	}

}
