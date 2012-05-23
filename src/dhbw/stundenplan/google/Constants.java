// Copyright 2011 Google Inc. All Rights Reserved.

package dhbw.stundenplan.google;

/**
 * Constants used by the Meeting Scheduler application.
 * 
 * @author Alain Vongsouvanh (alainv@google.com)
 */
public class Constants {
  /**
   * Should be used by all log statements
   */
  public static final String TAG = "Meeting Scheduler";
  public static final String VERSION = "1.0";

  /**
   * onActivityResult request codes:
   */
  public static final int GET_LOGIN = 0;
  public static final int AUTHENTICATED = 1;
  public static final int CREATE_EVENT = 2;

  /**
   * The type of account that we can use for API operations.
   */
  public static final String ACCOUNT_TYPE = "com.google";

  /**
   * The name of the service to authorize for.
   */
  public static final String OAUTH_SCOPE = "oauth2:https://www.googleapis.com/auth/calendar";

  /**
   * Preference keys.
   */
  public static final String SELECTED_ACCOUNT_PREFERENCE = "selected_account_preference";
  public static final String MEETING_LENGTH_PREFERENCE = "meeting_length_preference";
  public static final String TIME_SPAN_PREFERENCE = "time_span_preference";
  public static final String SKIP_WEEKENDS_PREFERENCE = "skip_weekends_preference";
  public static final String USE_WORKING_HOURS_PREFERENCE = "use_working_hours_preference";
  public static final String WORKING_HOURS_START_PREFERENCE = "working_hours_start_preference";
  public static final String WORKING_HOURS_END_PREFERENCE = "working_hours_end_preference";
}
