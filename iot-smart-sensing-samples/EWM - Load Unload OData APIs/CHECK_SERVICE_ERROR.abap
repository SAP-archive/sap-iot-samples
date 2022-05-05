  METHOD check_service_error.
    DATA: lx_bus_exp  TYPE REF TO /iwbep/cx_mgw_busi_exception,
          lo_msg      TYPE REF TO /scwm/cl_log,
          lt_msg      TYPE /scwm/cl_outb_pack_msg=>tt_outb_pack_msg,
          lv_is_error TYPE abap_bool.

    FIELD-SYMBOLS: <ls_msg> TYPE LINE OF /scwm/cl_outb_pack_msg=>tt_outb_pack_msg.
    CREATE OBJECT lx_bus_exp.

    DATA(lt_bapiret) = mo_log->get_prot( ).

    LOOP AT lt_bapiret ASSIGNING FIELD-SYMBOL(<fs_bapiret>) WHERE type = 'E' OR type = 'A' OR type = 'X'.

      lx_bus_exp->get_msg_container( )->add_message(
        EXPORTING
          iv_msg_type               =  <fs_bapiret>-type  " Message Type
          iv_msg_id                 =  <fs_bapiret>-id  " Message Class
          iv_msg_number             =  <fs_bapiret>-number  " Message Number
          iv_msg_v1                 =  <fs_bapiret>-message_v1   " Message Variable
          iv_msg_v2                 =  <fs_bapiret>-message_v2    " Message Variable
          iv_msg_v3                 =  <fs_bapiret>-message_v3    " Message Variable
          iv_msg_v4                 =  <fs_bapiret>-message_v4    " Message Variable
      ).

    ENDLOOP.

    RAISE EXCEPTION lx_bus_exp.

  ENDMETHOD.