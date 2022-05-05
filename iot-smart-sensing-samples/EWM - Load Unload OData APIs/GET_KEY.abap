  METHOD get_key.
    DATA ls_pair TYPE /iwbep/s_mgw_name_value_pair.
    READ TABLE it_key_tab WITH KEY name = iv_name INTO ls_pair.
    IF sy-subrc EQ 0.
      value = ls_pair-value.
      ev_value_exist = abap_true.
    ELSE.
      ev_value_exist = abap_false.
    ENDIF.
  ENDMETHOD.