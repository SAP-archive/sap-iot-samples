  METHOD /iwbep/if_mgw_appl_srv_runtime~execute_action.

*|Structure
    DATA:
          ls_msg    TYPE /scwm/s_odata_message.
*|Reference Object
    DATA:
          lx_bus_exp  TYPE REF TO /iwbep/cx_mgw_busi_exception.

*|  Try
    TRY.
*|      Init Methods
        me->init( ).
*|      Validate Action
        me->validate_action(
          EXPORTING
            iv_actionname = iv_action_name
            it_parameter = it_parameter
          CHANGING
            er_data = er_data ).

*|      Check Action Name
        CASE iv_action_name.
*|        Load
          WHEN 'load'  ##NO_TEXT.
            me->load(
              EXPORTING
                it_parameter = it_parameter
              CHANGING
                er_data       = er_data
                   ).
*|        Unload
          WHEN 'unload' ##NO_TEXT.
            me->unload(
              EXPORTING
                it_parameter = it_parameter
              CHANGING
                er_data       = er_data
                   ).
        ENDCASE.

*|    Catch
      CATCH zcx_hu_api INTO DATA(lx_hu_api).
        me->check_service_error( lx_hu_api ).
      CATCH cx_sy_ref_is_initial INTO DATA(lx_ref).


    ENDTRY.

  ENDMETHOD.