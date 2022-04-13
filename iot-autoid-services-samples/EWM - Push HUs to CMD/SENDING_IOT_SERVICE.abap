  METHOD sending_iot_service.

*|  Reference Object
    DATA: lo_rest_client TYPE REF TO cl_rest_http_client.
    DATA: lo_request     TYPE REF TO if_rest_entity.
    DATA: lo_response    TYPE REF TO if_rest_entity.

*|  Create By Destination
    cl_http_client=>create_by_destination(
      EXPORTING
        destination                = is_iot_service-dest_name
      IMPORTING
        client                     = DATA(lo_http_client)
      EXCEPTIONS
        argument_not_found         = 1                " Verbindungsparameter (Destination) nicht verfügbar
        destination_not_found      = 2                " Destination wurde nicht gefunden
        destination_no_authority   = 3                " Keine Berechtigung zur Verwendung der HTTP-Destination
        plugin_not_active          = 4                " HTTP/HTTPS-Kommunikation ist nicht verfügbar
        internal_error             = 5                " Interner Fehler (z.B. Name zu groß)
        oa2c_set_token_error       = 6                " Allgemeiner Fehler beim Setzen des Oauth-Tokens
        oa2c_missing_authorization = 7
        oa2c_invalid_config        = 8
        oa2c_invalid_parameters    = 9
        oa2c_invalid_scope         = 10
        oa2c_invalid_grant         = 11
        OTHERS                     = 12
    ).

*|  Check if an error occured
    IF sy-subrc <> 0.
*|    Set Message
      MESSAGE ID sy-msgid TYPE sy-msgty NUMBER sy-msgno
        WITH sy-msgv1 sy-msgv2 sy-msgv3 sy-msgv4 INTO mv_message.
*|    Return
      RETURN.
    ENDIF.
*
*|  Turn off Logon PopUp
    lo_http_client->propertytype_logon_popup = 0.

    CALL METHOD lo_http_client->request->set_method
      EXPORTING
        method = if_http_request=>co_request_method_post.
*|  Try
    TRY.
*|      Create
        CALL METHOD cl_oauth2_client=>create
          EXPORTING
            i_profile        = is_iot_service-o2ac_profile
*           i_configuration  =
          RECEIVING
            ro_oauth2_client = DATA(lo_oa2c_client).
      CATCH cx_oa2c INTO DATA(lx_oa2c).
        DATA(lv_text) = lx_oa2c->get_text( ).
        RETURN.
    ENDTRY.


    TRY.

        CALL METHOD lo_oa2c_client->set_token
          EXPORTING
            io_http_client = lo_http_client
            i_param_kind   = mc_param_kind.

      CATCH cx_oa2c INTO lx_oa2c.

        TRY.
            CALL METHOD lo_oa2c_client->execute_cc_flow.
          CATCH cx_oa2c INTO lx_oa2c.
            lv_text = lx_oa2c->get_text( ).
            RETURN.
        ENDTRY.


        TRY.
            CALL METHOD lo_oa2c_client->set_token
              EXPORTING
                io_http_client = lo_http_client
                i_param_kind   = mc_param_kind.
          CATCH cx_oa2c INTO lx_oa2c.
            lv_text = lx_oa2c->get_text( ).
            RETURN.
        ENDTRY.

    ENDTRY.

    DATA(lv_uri) = VALUE String( ).
    lv_uri = is_iot_service-path.

*|  Set Path
    cl_http_utility=>set_request_uri(
      EXPORTING
      request    = lo_http_client->request                 " HTTP Framework (iHTTP) HTTP Request
      uri        = lv_uri
    ).

    lo_http_client->refresh_cookie(
    EXCEPTIONS
      http_action_failed     = 1                " Die Ausführung der Methode ist misslungen
      http_processing_failed = 2                " Bearbeitung der Methode misslungen
      OTHERS                 = 3
    ).
*|  Check if Error Occured
    IF sy-subrc <> 0.
*|    Set Message
      MESSAGE ID sy-msgid TYPE sy-msgty NUMBER sy-msgno
        WITH sy-msgv1 sy-msgv2 sy-msgv3 sy-msgv4 INTO mv_message.
*|    Return
      RETURN.
    ENDIF.

*|  Create Object Rest
    CREATE OBJECT lo_rest_client
      EXPORTING
        io_http_client = lo_http_client.

*|  Set Version of Protocol
    lo_http_client->request->set_version( if_http_request=>co_protocol_version_1_0 ).

    IF lo_http_client IS BOUND AND lo_rest_client IS BOUND.

      DATA(lv_body) = /ui2/cl_json=>serialize( data = is_hu_announce_iot
*                                               pretty_name = /ui2/cl_json=>pretty_mode-camel_case
                                               name_mappings = zewm_cl_iot_announce_srvc=>get_name_mapping_announce( ) ).
*|    Set Payload or body ( json or xml)
      lo_request = lo_rest_client->if_rest_client~create_request_entity( ).
      lo_request->set_content_type( iv_media_type = if_rest_media_type=>gc_appl_json ).
      lo_request->set_string_data( lv_body ).

*|    POST
      lo_rest_client->if_rest_resource~post( lo_request ).
*|    Collect response
      lo_response = lo_rest_client->if_rest_client~get_response_entity( ).
      DATA(lv_status) = lo_response->get_header_field( '~status_code' ).
      DATA(lv_http_status) = lo_response->get_header_field( '~status_code' ).
      DATA(lv_reason) = lo_response->get_header_field( '~status_reason' ).
      DATA(lv_content_length) = lo_response->get_header_field( 'content-length' ).
      DATA(lv_location) = lo_response->get_header_field( 'location' ).
      DATA(lv_content_type) = lo_response->get_header_field( 'content-type' ).
      DATA(lv_response) = lo_response->get_string_data( ).
    ENDIF.

    IF lv_status(1) NE '2'.
      RAISE EXCEPTION TYPE zcx_hu_api.
    ENDIF.

  ENDMETHOD.