package com.rylow.eispbustracker_parentsedition.network;

/**
 * Created by s.bakhti on 30.3.2016.
 */
public class TransmissionCodes {

    public static final int USER_LOGIN = 101;
    public static final int USER_LOGIN_REPLY_SUCCESS = 102;
    public static final int USER_LOGIN_REPLY_FAIL = 103;


    public static final int REQUEST_CHILD_LIST = 204;
    public static final int RESPONSE_CHILD_LIST = 205;
    public static final int REQUEST_LAST_KNOWN_GPS = 206;
    public static final int RESPONSE_LAST_KNOWN_GPS = 207;
    public static final int REQUEST_RIDE_IN_TRANSIT_STATUS = 208;
    public static final int RESPONSE_RIDE_IN_TRANSIT_STATUS = 209;
    public static final int REQUEST_RIDE_STATUS_ALL_ATTACHED_KIDS = 210;
    public static final int RESPONSE_RIDE_STATUS_ALL_ATTACHED_KIDS = 211;
}
