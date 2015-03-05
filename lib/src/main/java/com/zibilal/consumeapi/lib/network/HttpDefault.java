package com.zibilal.consumeapi.lib.network;

/**
 * Created by bmuhamm on 3/24/14.
 */
public class HttpDefault {
    public static final int		mIntCompressed			=	1;
    public static final String 	HEADER_USER_AGENT 		= "User-Agent",
            HEADER_USER_AGENT_VALUE = "makandimana/Android (makandimana on Android)",
            HEADER_ACCEPT 			= "Accept",
            HEADER_ACCEPT_VALUE 	= "text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            HEADER_CONTENT_LENGTH 	= "Content-Length",
            HEADER_CONTENT_TYPE_MULTIPART_FORM_DATA	=	"multipart/form-data",
            HEADER_CONTENT_TYPE 	= "Content-Type",
            HEADER_AUTHENTICATION 	= "Authorization",
            HEADER_SESSION 			= "Cookie",
            HEADER_REFERER 			= "Referer",
            DEFAULT_CONTENT_TYPE 	= "application/x-www-form-urlencoded";
    public static final String 	TAG_CALLER_ID 			= "callerId",
            TAG_ACTION_ID 			= "actionId",
            TAG_URL 				= "url",
            TAG_CONTENT 			= "content",
            TAG_FILE				= "file",
            TAG_CONTENTTYPE 		= "contentType",
            TAG_METHOD 				= "method",
            TAG_LISTENER 			= "listener",
            TAG_RESPONSE 			= "HTTP_RESPONSE",
            TAG_RESPONSE_CODE 		= "HTTP_ERROR_CODE",
            TAG_PAGE				= "page",
            TAG_SAVE				= "save",
            TAG_MODE				= "mode",
            TAG_MODIFIED			= "modified",
            TAG_RELOAD				= "reload",
            HTTP_OK 				= "OK",
            HTTP_METHOD_GET 		= "GET",
            HTTP_METHOD_POST 		= "POST",
            HTTP_SEND_INFO			= "SEND_INFO",
            CALLER_ID 				= "HTTP_CONNECTOR";
}
