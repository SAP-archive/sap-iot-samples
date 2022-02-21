  METHOD load.

*|Variable
    DATA:
      lv_lgnum   TYPE /scwm/lgnum,
      lv_huident TYPE /scwm/de_huident.
*|Structure
    DATA:
         ls_return_msg     TYPE /scwm/s_odata_message.
*|Reference Object
    DATA:
          lo_hu TYPE REF TO /scwm/if_api_handling_unit.

    CLEAR: er_data.

    lv_lgnum = me->get_key( it_key_tab = it_parameter iv_name = 'lgnum' ).

    lv_huident = me->get_key( it_key_tab = it_parameter iv_name = 'huident' ).

    " conversion input
    CALL FUNCTION 'CONVERSION_EXIT_HUID_INPUT'
      EXPORTING
        input  = lv_huident
      IMPORTING
        output = lv_huident.

*/scwm/cl_load=>obd_hu(
*  EXPORTING
**    io_log     =
**    iv_cancel  = abap_false
**    iv_create  = abap_false
*    it_huident =
**    it_item    =
**  IMPORTING
**    ev_error   =
**    ev_post    =
**    et_return  =
*).
**CATCH /scwm/cx_sr_error.

    /scwm/cl_api_factory=>get_service(
      IMPORTING
        eo_api = lo_hu ).
    TRY.
        lo_hu->load(
          EXPORTING
            it_huident = VALUE #( ( huident = lv_huident ) )
            iv_whno    = lv_lgnum
          IMPORTING
            eo_message = DATA(lo_message)  " Messages from API
        ).

*|      Check if Message is not Bound and has no Error
        IF lo_message IS NOT BOUND OR lo_message->check( ) = abap_false.

*|        Save
          lo_hu->save(
            IMPORTING
              eo_message = lo_message     " Messages from API
          ).

*|        HU &1 successfull Loaded
          MESSAGE s004(zewm_hu_api) WITH lv_huident INTO data(lv_msg).
*|        Add Message
          mo_log->add_message( ).

          " Return message
          ls_return_msg-msgid = sy-msgid.
          ls_return_msg-msgno = sy-msgno.
          ls_return_msg-msgty = sy-msgty.
          ls_return_msg-message = lv_msg.
          ls_return_msg-msg_success = abap_false.

*|      Get Messages
        ELSE.

*|        Get Most Important Message
          CALL METHOD lo_message->get_most_important_message
            RECEIVING
              rs_message = DATA(ls_message).

          /scwm/cl_wi=>get_msg_fields( EXPORTING is_message = ls_message
                                       IMPORTING es_symsg = DATA(ls_symsg) ).

          CALL FUNCTION 'MESSAGE_TEXT_BUILD'
            EXPORTING
              msgid               = ls_symsg-msgid
              msgnr               = ls_symsg-msgno
              msgv1               = ls_symsg-msgv1
              msgv2               = ls_symsg-msgv2
              msgv3               = ls_symsg-msgv3
              msgv4               = ls_symsg-msgv4
            IMPORTING
              message_text_output = ls_return_msg-message.

          MOVE-CORRESPONDING ls_symsg TO ls_return_msg.


          copy_data_to_ref( EXPORTING is_data = ls_return_msg
                             CHANGING cr_data = er_data ).
*|        Get All Messages
          CALL METHOD lo_message->get_messages
            IMPORTING
              et_bapiret = DATA(lt_bapiret).

          mo_log->add_log(
            EXPORTING
              it_prot = lt_bapiret
          ).


          RAISE EXCEPTION TYPE zcx_hu_api.
        ENDIF.

        CALL METHOD me->copy_data_to_ref
          EXPORTING
            is_data = ls_return_msg
          CHANGING
            cr_data = er_data.

*|    Catch
      CATCH /scwm/cx_api_faulty_call INTO DATA(lx_faulty). " Faulty Call of EWM API

*|      Raise Exception
        RAISE EXCEPTION TYPE zcx_hu_api.
    ENDTRY.


  ENDMETHOD.