  METHOD announce_hu_to_iot.

    DATA: lv_msg_txt   TYPE string.                         "#EC NEEDED

    DATA: lv_qname  TYPE trfcqnam,
          lv_guid   TYPE guid,

          lv_xhu    TYPE boole_d,
          lv_status TYPE /scdl/dl_status_type.

* Query
    DATA: lt_docid        TYPE /scwm/dlv_docid_item_tab,
          ls_docid        TYPE /scwm/dlv_docid_item_str,
          ls_include_data TYPE /scwm/dlv_query_incl_str_prd,
          ls_read_options TYPE /scwm/dlv_query_contr_str,
          ls_whr_headers  TYPE /scwm/dlv_header_out_prd_str,
          lt_whr_headers  TYPE /scwm/dlv_header_out_prd_tab,
          lt_whr_items    TYPE /scwm/dlv_item_out_prd_tab,
          lt_huhdr        TYPE /scwm/tt_huhdr_int,
          lt_huitm        TYPE /scwm/tt_huitm_int,
          ls_status       TYPE /scdl/dl_status_str.

    DATA: lo_prd             TYPE REF TO /scwm/cl_dlv_management_prd.

    DATA: lv_docid           TYPE char100.

    FIELD-SYMBOLS: <hu>      TYPE /scwm/s_huhdr_int.

    ls_docid = is_docid.
    APPEND ls_docid TO lt_docid.

    ls_read_options-data_retrival_only      = abap_true.
    ls_read_options-mix_in_object_instances =
      /scwm/if_dl_c=>sc_mix_in_load_instance.

    ls_include_data-head_status     = abap_true.
    ls_include_data-head_status_dyn = abap_true.

    TRY.
        CALL FUNCTION '/SCWM/WHR_QUERY'
          EXPORTING
            it_docid        = lt_docid
            iv_doccat_whr   = ls_docid-doccat
            is_read_options = ls_read_options
            is_include_data = ls_include_data
          IMPORTING
            et_whr_headers  = lt_whr_headers
            et_whr_items    = lt_whr_items
            et_huhdr        = lt_huhdr
            et_huitm        = lt_huitm.

      CATCH /scdl/cx_delivery.
*     Handled by the following IF statement
    ENDTRY.

    IF lt_huhdr IS INITIAL.
      RETURN.
    ENDIF.

    CALL METHOD zewm_cl_iot_announce_srvc=>announce_pdi_hu
      EXPORTING
        iv_lgnum     = /scwm/cl_tm=>sv_lgnum
        iv_flt_val   = ip_flt_val
        it_whr_items = lt_whr_items
        it_huhdr     = lt_huhdr
        it_huitm     = lt_huitm
      RECEIVING
        rp_status    = rp_status.

  ENDMETHOD.