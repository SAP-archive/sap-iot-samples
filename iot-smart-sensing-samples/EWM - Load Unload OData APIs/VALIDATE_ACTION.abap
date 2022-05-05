  METHOD validate_action.

*|Variable
    DATA: lv_lgnum   TYPE /scwm/lgnum,
          lv_huident TYPE /scwm/de_huident.
*|Structure
    DATA:
          ls_return_msg     TYPE /scwm/s_odata_message.
*|Internal Table
    DATA:
      lt_guid_hu TYPE /scwm/tt_guid_hu,
      lt_huhdr   TYPE /scwm/tt_huhdr_int,
      lt_huitm   TYPE /scwm/tt_huitm_int.

    CLEAR: lv_lgnum, lv_huident.

    lv_lgnum = me->get_key( it_key_tab = it_parameter iv_name = 'lgnum' ).

    lv_huident = me->get_key( it_key_tab = it_parameter iv_name = 'huident' ).

    IF lv_lgnum IS INITIAL OR
       lv_huident IS INITIAL.
*|    Inserted Entries not correct
      MESSAGE e002(zewm_hu_api) INTO DATA(lv_msg).
*|    Add Message
      mo_log->add_message( ).

      " Return message
      ls_return_msg-msgid = sy-msgid.
      ls_return_msg-msgno = sy-msgno.
      ls_return_msg-msgty = sy-msgty.
      ls_return_msg-message = lv_msg.
      ls_return_msg-msg_success = abap_true.

      CALL METHOD me->copy_data_to_ref
        EXPORTING
          is_data = ls_return_msg
        CHANGING
          cr_data = er_data.

      RAISE EXCEPTION TYPE zcx_hu_api.
    ENDIF.

    IF lv_huident IS NOT INITIAL.

      " conversion input
      CALL FUNCTION 'CONVERSION_EXIT_HUID_INPUT'
        EXPORTING
          input  = lv_huident
        IMPORTING
          output = lv_huident.

      DATA(lr_huident) = VALUE rseloption( ( sign = wmegc_sign_inclusive option = wmegc_option_eq low = lv_huident ) ).

      " Get HU data
      CALL FUNCTION '/SCWM/HU_SELECT_GEN'
        EXPORTING
          iv_lgnum     = lv_lgnum
          ir_huident   = lr_huident
        IMPORTING
          et_guid_hu   = lt_guid_hu
          et_huhdr     = lt_huhdr
          et_huitm     = lt_huitm
        EXCEPTIONS
          wrong_input  = 1
          not_possible = 2
          error        = 3
          OTHERS       = 4.

*|    Check if an error occured
      IF sy-subrc <> 0.
*|      Set Message
        MESSAGE ID sy-msgid TYPE sy-msgty NUMBER sy-msgno
          WITH sy-msgv1 sy-msgv2 sy-msgv3 sy-msgv4 INTO lv_msg.
*|      Add Message
        mo_log->add_message( ).

        " Return message
        ls_return_msg-msgid = sy-msgid.
        ls_return_msg-msgno = sy-msgno.
        ls_return_msg-msgty = sy-msgty.
        ls_return_msg-message = lv_msg.
        ls_return_msg-msg_success = abap_true.

        CALL METHOD me->copy_data_to_ref
          EXPORTING
            is_data = ls_return_msg
          CHANGING
            cr_data = er_data.

        RAISE EXCEPTION TYPE zcx_hu_api.
      ENDIF.
*|    Check if No HU Ident found
      IF lt_huhdr IS INITIAL.
*|      HU &1 from Warehouse Number &2 not found
        MESSAGE e001(zewm_hu_api) WITH lv_lgnum lv_huident INTO lv_msg.
*|      Add Message
        mo_log->add_message( ).

        " Return message
        ls_return_msg-msgid = sy-msgid.
        ls_return_msg-msgno = sy-msgno.
        ls_return_msg-msgty = sy-msgty.
        ls_return_msg-message = lv_msg.
        ls_return_msg-msg_success = abap_true.

        CALL METHOD me->copy_data_to_ref
          EXPORTING
            is_data = ls_return_msg
          CHANGING
            cr_data = er_data.

        RAISE EXCEPTION TYPE zcx_hu_api.
      ENDIF.
    ENDIF.

  ENDMETHOD.